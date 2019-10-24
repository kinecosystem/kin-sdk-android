package kin.sdk.internal.services

import kin.base.AssetTypeNative
import kin.base.KeyPair
import kin.base.Memo
import kin.base.Network
import kin.base.PaymentOperation
import kin.base.Server
import kin.base.Transaction.Builder
import kin.base.responses.AccountResponse
import kin.base.responses.HttpResponseException
import kin.base.responses.SubmitTransactionResponse
import kin.sdk.exception.AccountNotFoundException
import kin.sdk.exception.IllegalAmountException
import kin.sdk.exception.InsufficientFeeException
import kin.sdk.exception.InsufficientKinException
import kin.sdk.exception.OperationFailedException
import kin.sdk.exception.TransactionFailedException
import kin.sdk.internal.utils.bytesToHex
import kin.sdk.models.Transaction
import kin.sdk.models.TransactionId
import kin.sdk.models.WhitelistableTransaction
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.math.BigDecimal
import java.util.ArrayList

interface TransactionSender {

    @Throws(OperationFailedException::class)
    fun buildTransaction(
        from: KeyPair,
        publicAddress: String,
        amount: BigDecimal,
        fee: Int
    ): Transaction

    @Throws(OperationFailedException::class)
    fun buildTransaction(
        from: KeyPair,
        publicAddress: String,
        amount: BigDecimal,
        fee: Int,
        memo: String?
    ): Transaction

    @Throws(OperationFailedException::class)
    fun sendTransaction(transaction: Transaction): TransactionId

    @Throws(OperationFailedException::class)
    fun sendWhitelistTransaction(whitelist: String): TransactionId
}

internal class TransactionSenderImpl(
    private val server: Server /* horizon server*/,
    private val appId: String?
) : TransactionSender {

    companion object {
        private const val MEMO_BYTES_LENGTH_LIMIT = 21 //Memo length limitation is 28 bytes. 7/28 are reserved to include the appId and some characters.
        private const val MAX_NUM_OF_DECIMAL_PLACES = 4
        private const val MEMO_APP_ID_VERSION_PREFIX = "1"
        private const val MEMO_DELIMITER = "-"
        private const val INSUFFICIENT_KIN_RESULT_CODE = "op_underfunded"
        private const val INSUFFICIENT_FEE_RESULT_CODE = "tx_insufficient_fee"
        private const val INSUFFICIENT_BALANCE_RESULT_CODE = "tx_insufficient_balance"

        private fun isInsufficientKinException(transactionException: TransactionFailedException): Boolean {
            val resultCodes = transactionException.operationsResultCodes
            val transactionResultCode = transactionException.transactionResultCode
            return resultCodes != null && resultCodes.isNotEmpty() && INSUFFICIENT_KIN_RESULT_CODE == resultCodes[0] || !transactionResultCode.isNullOrEmpty() && INSUFFICIENT_BALANCE_RESULT_CODE == transactionResultCode
        }

        private fun isInsufficientFeeException(transactionException: TransactionFailedException): Boolean {
            val transactionResultCode = transactionException.transactionResultCode
            return !transactionResultCode.isNullOrEmpty() && INSUFFICIENT_FEE_RESULT_CODE == transactionResultCode
        }
    }

    @Throws(OperationFailedException::class)
    override fun buildTransaction(
        from: KeyPair, publicAddress: String,
        amount: BigDecimal,
        fee: Int
    ): Transaction {
        return buildTransaction(from, publicAddress, amount, fee, null)
    }

    @Throws(OperationFailedException::class)
    override fun buildTransaction(
        from: KeyPair,
        publicAddress: String,
        amount: BigDecimal,
        fee: Int,
        memo: String?
    ): Transaction {
        var memo = memo
        checkParams(from, publicAddress, amount, fee, memo)
        if (appId != null && appId != "") {
            memo = addAppIdToMemo(memo)
        }

        val addressee = generateAddresseeKeyPair(publicAddress)
        val sourceAccount = loadAccount(from)
        verifyAddresseeAccount(generateAddresseeKeyPair(addressee.accountId))
        val stellarTransaction = buildStellarTransaction(from, amount, addressee, sourceAccount, fee, memo)
        val id = TransactionId(stellarTransaction.hash().bytesToHex())
        val whitelistableTransaction = WhitelistableTransaction(stellarTransaction.toEnvelopeXdrBase64(), Network.current().networkPassphrase)
        return Transaction(addressee, from, amount, fee, memo, id, stellarTransaction, whitelistableTransaction)
    }

    @Throws(OperationFailedException::class)
    override fun sendTransaction(transaction: Transaction): TransactionId {
        return sendTransaction(transaction.stellarTransaction)
    }

    @Throws(OperationFailedException::class)
    override fun sendWhitelistTransaction(whitelist: String): TransactionId {
        try {
            val transaction = kin.base.Transaction.fromEnvelopeXdr(whitelist)
            return sendTransaction(transaction)
        } catch (e: IOException) {
            throw OperationFailedException("whitelist transaction data invalid", e)
        }
    }

    private fun addAppIdToMemo(memo: String?): String {
        val formattedMemo = memo
            ?.trim { it <= ' ' } ?: "" // remove leading and trailing whitespaces.

        return StringBuilder()
            .append(MEMO_APP_ID_VERSION_PREFIX)
            .append(MEMO_DELIMITER)
            .append(appId)
            .append(MEMO_DELIMITER)
            .append(formattedMemo)
            .toString()
    }

    @Throws(OperationFailedException::class)
    private fun checkParams(from: KeyPair, publicAddress: String, amount: BigDecimal,
                            fee: Int, memo: String?) {
        validateAmountDecimalPoint(amount)
        checkForNegativeFee(fee)
        checkAddressNotEmpty(publicAddress)
        checkForNegativeAmount(amount)
        checkMemo(memo)
    }

    @Throws(OperationFailedException::class)
    private fun validateAmountDecimalPoint(amount: BigDecimal) {
        val amountWithoutTrailingZeros = amount.stripTrailingZeros()
        val numOfDecimalPlaces = amountWithoutTrailingZeros.scale()
        if (numOfDecimalPlaces > MAX_NUM_OF_DECIMAL_PLACES) {
            throw IllegalAmountException("amount can't have more then 5 digits after the decimal point")
        }
    }

    private fun checkAddressNotEmpty(publicAddress: String) {
        require(publicAddress.isNotEmpty()) { "Addressee not valid - public address can't be null or empty" }
    }

    private fun checkForNegativeAmount(amount: BigDecimal) {
        require(amount.signum() != -1) { "Amount can't be negative" }
    }

    private fun checkForNegativeFee(fee: Int) {
        require(fee >= 0) { "Fee can't be negative" }
    }

    private fun checkMemo(memo: String?) {
        try {
            require(!(memo != null && memo.toByteArray(charset("UTF-8")).size > MEMO_BYTES_LENGTH_LIMIT)) { "Memo cannot be longer that $MEMO_BYTES_LENGTH_LIMIT bytes(UTF-8 characters)" }
        } catch (e: UnsupportedEncodingException) {
            throw IllegalArgumentException("Memo text have unsupported characters encoding")
        }
    }

    @Throws(OperationFailedException::class)
    private fun generateAddresseeKeyPair(publicAddress: String): KeyPair {
        try {
            return KeyPair.fromAccountId(publicAddress)
        } catch (e: Exception) {
            throw OperationFailedException("Invalid addressee public address format", e)
        }
    }

    private fun buildStellarTransaction(
        from: KeyPair,
        amount: BigDecimal,
        addressee: KeyPair,
        sourceAccount: AccountResponse,
        fee: Int,
        memo: String?
    ): kin.base.Transaction {
        return Builder(sourceAccount)
            .addOperation(
                PaymentOperation.Builder(addressee, AssetTypeNative(), amount.toString()).build()
            )
            .addFee(fee)
            .also {
                if (memo != null) {
                    it.addMemo(Memo.text(memo))
                }
            }
            .build()
            .also { it.sign(from) }
    }

    @Throws(OperationFailedException::class)
    private fun verifyAddresseeAccount(addressee: KeyPair) {
        loadAccount(addressee)
    }

    @Throws(OperationFailedException::class)
    private fun loadAccount(from: KeyPair): AccountResponse {
        val sourceAccount: AccountResponse?
        try {
            sourceAccount = server.accounts().account(from)
        } catch (httpError: HttpResponseException) {
            if (httpError.statusCode == 404) {
                throw AccountNotFoundException(from.accountId)
            } else {
                throw OperationFailedException(httpError)
            }
        } catch (e: IOException) {
            throw OperationFailedException(e)
        }

        if (sourceAccount == null) {
            throw OperationFailedException("can't retrieve data for account " + from.accountId)
        }
        return sourceAccount
    }

    @Throws(OperationFailedException::class)
    private fun sendTransaction(transaction: kin.base.Transaction): TransactionId {
        try {
            val response = server.submitTransaction(transaction)
                ?: throw OperationFailedException("can't get transaction response")
            return if (response.isSuccess) {
                TransactionId(response.hash)
            } else {
                createFailureException(response)
            }
        } catch (e: IOException) {
            throw OperationFailedException(e)
        }
    }

    @Throws(TransactionFailedException::class, InsufficientKinException::class, InsufficientFeeException::class)
    private fun createFailureException(response: SubmitTransactionResponse): TransactionId {
        var operationsResultCodes: ArrayList<String>? = null
        var transactionResultCode: String? = null
        if (response.extras != null && response.extras.resultCodes != null) {
            val resultCodes = response.extras.resultCodes
            operationsResultCodes = resultCodes.operationsResultCodes
            transactionResultCode = resultCodes.transactionResultCode
        }
        val transactionException = TransactionFailedException(transactionResultCode, operationsResultCodes)
        when {
            Companion.isInsufficientKinException(transactionException) -> throw InsufficientKinException()
            Companion.isInsufficientFeeException(transactionException) -> throw InsufficientFeeException()
            else -> throw transactionException
        }
    }
}
