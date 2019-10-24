package kin.sdk

import kin.sdk.exception.AccountNotFoundException
import kin.sdk.exception.CryptoException
import kin.sdk.exception.InsufficientKinException
import kin.sdk.exception.OperationFailedException
import kin.sdk.exception.TransactionFailedException
import kin.sdk.internal.services.helpers.EventListener
import kin.sdk.internal.services.helpers.ListenerRegistration
import kin.sdk.models.AccountStatus
import kin.sdk.models.Balance
import kin.sdk.models.PaymentInfo
import kin.sdk.models.Transaction
import kin.sdk.models.TransactionId
import kin.utils.Request
import java.math.BigDecimal

/**
 * Represents an account which holds Kin.
 */
interface KinAccount {

    /**
     * @return String the public address of the account, or null if deleted
     */
    val publicAddress: String?

    /**
     * Create [Request] for getting the current confirmed balance in kin
     *
     *  See [KinAccount.getBalanceSync] for possibles errors
     *
     * @return `Request<Balance>` Balance - the balance in kin
     */
    val balance: Request<Balance>

    /**
     * Get the current confirmed balance in kin
     *
     * **Note:** This method accesses the network, and should not be called on the android main thread.
     *
     * @return the balance in kin
     * @throws AccountNotFoundException if account was not created
     * @throws OperationFailedException any other error
     */
    val balanceSync: Balance

    /**
     * Get current account status on blockchain network.
     *
     * @return account status, either [AccountStatus.NOT_CREATED], or [ ][AccountStatus.CREATED]
     * @throws OperationFailedException any other error
     */
    @get:AccountStatus
    val statusSync: Int

    /**
     * Create [Request] for getting current account status on blockchain network.
     *
     *  See [KinAccount.getStatusSync] for possibles errors
     *
     * @return account status, either [AccountStatus.NOT_CREATED], or [ ][AccountStatus.CREATED]
     */
    val status: Request<Int>

    /**
     * Build a Transaction object of the given amount in kin, to the specified public address.
     *
     *  See [KinAccount.buildTransactionSync] for possibles errors
     *
     * @param publicAddress the account address to send the specified kin amount.
     * @param amount        the amount of kin to transfer.
     * @param fee           the amount of fee(in stroops) for this transfer.
     * @return `Request<TransactionId>`, TransactionId - the transaction identifier.
     */
    fun buildTransaction(publicAddress: String, amount: BigDecimal, fee: Int): Request<Transaction>

    /**
     * Build a Transaction object of the given amount in kin, to the specified public address and with a memo(that can be empty or null).
     *
     *  See [KinAccount.buildTransactionSync] for possibles errors
     *
     * @param publicAddress the account address to send the specified kin amount.
     * @param amount        the amount of kin to transfer.
     * @param fee           the amount of fee(in stroops) for this transfer.
     * @param memo          An optional string, can contain a utf-8 string up to 21 bytes in length, included on the transaction record.
     * @return `Request<TransactionId>`, TransactionId - the transaction identifier
     */
    fun buildTransaction(publicAddress: String, amount: BigDecimal, fee: Int, memo: String?): Request<Transaction>

    /**
     * Create [Request] for signing and sending a transaction
     *
     *  See [KinAccount.sendTransactionSync] for possibles errors
     *
     * @param transaction is the transaction object to send.
     * @return `Request<TransactionId>`, TransactionId - the transaction identifier.
     */
    fun sendTransaction(transaction: Transaction): Request<TransactionId>

    /**
     * Create [Request] for signing and sending a transaction from a whitelist.
     * whitelist a transaction means that the user will not pay any fee(if your App is in the Kin whitelist)
     *
     *  See [KinAccount.sendWhitelistTransactionSync] for possibles errors
     *
     * @param whitelist is the whitelist data (got from the server) which will be used to send the transaction.
     * @return `Request<TransactionId>`, TransactionId - the transaction identifier.
     */
    fun sendWhitelistTransaction(whitelist: String): Request<TransactionId>

    /**
     * Build a Transaction object of the given amount in kin, to the specified public address.
     *
     * **Note:** This method accesses the network, and should not be called on the android main thread.
     *
     * @param publicAddress the account address to send the specified kin amount.
     * @param amount        the amount of kin to transfer.
     * @param fee           the amount of fee(in stroops) for this transfer.
     * @return a Transaction object which also includes the transaction id.
     * @throws AccountNotFoundException if the sender or destination account was not created.
     * @throws OperationFailedException other error occurred.
     */
    @Throws(OperationFailedException::class)
    fun buildTransactionSync(publicAddress: String, amount: BigDecimal, fee: Int): Transaction

    /**
     * Build a Transaction object of the given amount in kin, to the specified public address and with a memo(that can be empty or null).
     *
     * **Note:** This method accesses the network, and should not be called on the android main thread.
     *
     * @param publicAddress the account address to send the specified kin amount.
     * @param amount        the amount of kin to transfer.
     * @param fee           the amount of fee(in stroops) for this transfer.
     * @param memo          An optional string, can contain a utf-8 string up to 21 bytes in length, included on the transaction record.
     * @return a Transaction object which also includes the transaction id.
     * @throws AccountNotFoundException if the sender or destination account was not created.
     * @throws OperationFailedException other error occurred.
     */
    @Throws(OperationFailedException::class)
    fun buildTransactionSync(publicAddress: String, amount: BigDecimal, fee: Int, memo: String?): Transaction

    /**
     * send a transaction.
     *
     * **Note:** This method accesses the network, and should not be called on the android main thread.
     *
     * @param transaction is the transaction object to send.
     * @return TransactionId the transaction identifier.
     * @throws AccountNotFoundException   if the sender or destination account was not created.
     * @throws InsufficientKinException   if account balance has not enough kin.
     * @throws TransactionFailedException if transaction failed, contains blockchain failure details.
     * @throws OperationFailedException   other error occurred.
     */
    @Throws(OperationFailedException::class)
    fun sendTransactionSync(transaction: Transaction): TransactionId

    /**
     * send a whitelist transaction.
     * whitelist a transaction means that the user will not pay any fee(if your App is in the Kin whitelist)
     *
     * **Note:** This method accesses the network, and should not be called on the android main thread.
     *
     * @param whitelist is the whitelist data (got from the server) which will be used to send the transaction.
     * @return TransactionId the transaction identifier.
     * @throws AccountNotFoundException   if the sender or destination account was not created.
     * @throws InsufficientKinException   if account balance has not enough kin.
     * @throws TransactionFailedException if transaction failed, contains blockchain failure details.
     * @throws OperationFailedException   other error occurred.
     */
    @Throws(OperationFailedException::class)
    fun sendWhitelistTransactionSync(whitelist: String): TransactionId

    /**
     * Creates and adds listener for balance changes of this account, use returned [ListenerRegistration] to
     * stop listening.
     *
     ***Note:** Events will be fired on background thread.
     *
     * @param listener listener object for payment events
     */
    fun addBalanceListener(listener: EventListener<Balance>): ListenerRegistration

    /**
     * Creates and adds listener for payments concerning this account, use returned [ListenerRegistration] to
     * stop listening.
     *
     ***Note:** Events will be fired on background thread.
     *
     * @param listener listener object for payment events
     */
    fun addPaymentListener(listener: EventListener<PaymentInfo>): ListenerRegistration

    /**
     * Creates and adds listener for account creation event, use returned [ListenerRegistration] to stop
     * listening.
     *
     ***Note:** Events will be fired on background thread.
     *
     * @param listener listener object for payment events
     */
    fun addAccountCreationListener(listener: EventListener<Void?>): ListenerRegistration

    /**
     * Export the account data as a JSON string. The seed is encrypted.
     *
     * @param passphrase The passphrase with which to encrypt the seed
     * @return A JSON representation of the data as a string
     */
    @Throws(CryptoException::class)
    fun export(passphrase: String): String
}
