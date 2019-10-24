package kin.sdk.internal

import kin.base.KeyPair
import kin.sdk.KinAccount
import kin.sdk.exception.AccountDeletedException
import kin.sdk.exception.CryptoException
import kin.sdk.exception.OperationFailedException
import kin.sdk.internal.services.AccountInfoRetriever
import kin.sdk.internal.services.BlockchainEvents
import kin.sdk.internal.services.TransactionSender
import kin.sdk.internal.services.helpers.EventListener
import kin.sdk.internal.services.helpers.ListenerRegistration
import kin.sdk.internal.utils.BackupRestore
import kin.sdk.internal.utils.BlockchainEventsCreator
import kin.sdk.models.Balance
import kin.sdk.models.PaymentInfo
import kin.sdk.models.Transaction
import kin.sdk.models.TransactionId
import kin.utils.Request
import java.math.BigDecimal
import java.util.concurrent.Callable

internal class KinAccountImpl(
    private val account: KeyPair,
    private val backupRestore: BackupRestore,
    private val transactionSender: TransactionSender,
    private val accountInfoRetriever: AccountInfoRetriever,
    blockchainEventsCreator: BlockchainEventsCreator
) : KinAccount {
    private val blockchainEvents: BlockchainEvents by lazy { blockchainEventsCreator.create(account.accountId) }
    private var isDeleted = false

    override val publicAddress: String?
        get() = if (!isDeleted) {
            account.accountId
        } else null

    override val balanceSync: Balance
        @Throws(OperationFailedException::class)
        get() = checkValidAccount()
            .let { accountInfoRetriever.getBalance(account.accountId) }

    override val statusSync: Int
        @Throws(OperationFailedException::class)
        get() = checkValidAccount()
            .let { accountInfoRetriever.getStatus(account.accountId) }

    override val balance: Request<Balance>
        get() = Request(Callable<Balance> { balanceSync })

    override val status: Request<Int>
        get() = Request(Callable<Int> { statusSync })

    @Throws(OperationFailedException::class)
    override fun buildTransactionSync(
        publicAddress: String,
        amount: BigDecimal,
        fee: Int
    ): Transaction =
        checkValidAccount()
            .let {
                transactionSender.buildTransaction(account, publicAddress, amount, fee)
            }

    @Throws(OperationFailedException::class)
    override fun buildTransactionSync(publicAddress: String, amount: BigDecimal,
                                      fee: Int, memo: String?): Transaction =
        checkValidAccount()
            .let {
                transactionSender.buildTransaction(account, publicAddress, amount, fee, memo)
            }

    @Throws(OperationFailedException::class)
    override fun sendTransactionSync(transaction: Transaction): TransactionId =
        checkValidAccount()
            .let {
                transactionSender.sendTransaction(transaction)
            }

    @Throws(OperationFailedException::class)
    override fun sendWhitelistTransactionSync(whitelist: String): TransactionId =
        checkValidAccount()
            .let { transactionSender.sendWhitelistTransaction(whitelist) }

    override fun addBalanceListener(listener: EventListener<Balance>): ListenerRegistration =
        blockchainEvents.addBalanceListener(listener)

    override fun addPaymentListener(listener: EventListener<PaymentInfo>): ListenerRegistration =
        blockchainEvents.addPaymentListener(listener)

    override fun addAccountCreationListener(listener: EventListener<Void?>): ListenerRegistration =
        blockchainEvents.addAccountCreationListener(listener)

    @Throws(CryptoException::class)
    override fun export(passphrase: String): String =
        backupRestore.exportAccount(account, passphrase).jsonString

    fun markAsDeleted() {
        isDeleted = true
    }

    @Throws(AccountDeletedException::class)
    private fun checkValidAccount() {
        if (isDeleted) {
            throw AccountDeletedException()
        }
    }

    override fun buildTransaction(
        publicAddress: String,
        amount: BigDecimal,
        fee: Int
    ): Request<Transaction> = Request { buildTransactionSync(publicAddress, amount, fee) }

    override fun buildTransaction(
        publicAddress: String,
        amount: BigDecimal,
        fee: Int,
        memo: String?
    ): Request<Transaction> = Request { buildTransactionSync(publicAddress, amount, fee, memo) }

    override fun sendTransaction(transaction: Transaction): Request<TransactionId> =
        Request { sendTransactionSync(transaction) }

    override fun sendWhitelistTransaction(whitelist: String): Request<TransactionId> =
        Request { sendWhitelistTransactionSync(whitelist) }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val account = other as KinAccount?
        return if (publicAddress == null || account!!.publicAddress == null) {
            false
        } else publicAddress == account.publicAddress
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + backupRestore.hashCode()
        result = 31 * result + transactionSender.hashCode()
        result = 31 * result + accountInfoRetriever.hashCode()
        result = 31 * result + blockchainEvents.hashCode()
        result = 31 * result + isDeleted.hashCode()
        return result
    }
}
