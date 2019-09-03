package kin.sdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;

import kin.sdk.exception.AccountNotFoundException;
import kin.sdk.exception.CryptoException;
import kin.sdk.exception.InsufficientKinException;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.exception.TransactionFailedException;
import kin.sdk.queue.PaymentQueue;
import kin.sdk.transactiondata.PaymentTransaction;
import kin.sdk.transactiondata.TransactionParams;
import kin.utils.Request;

/**
 * Represents an account which holds Kin.
 */
public interface KinAccount {

    /**
     * @return String the public address of the account, or null if deleted
     */
    @Nullable
    String getPublicAddress();

    // TODO: 2019-08-21 finalize the deprecated texts

    /**
     * @deprecated <b>This method was deprecated in version 2.0.0, please use the PaymentQueue,
     * TransactionInterceptor and/or sendTransaction(sync or not) classes</b>
     *
     * Build a Transaction object of the given amount in kin, to the specified public address.
     * <p> See {@link KinAccount#buildTransactionSync(String, BigDecimal, int)} for possibles errors</p>
     *
     * @param publicAddress the account address to send the specified kin amount.
     * @param amount        the amount of kin to transfer.
     * @param fee           the amount of fee(in Quarks) for this transfer (1 Quark = 0.00001 KIN).
     * @return {@code Request<TransactionId>}, TransactionId - the transaction identifier.
     */
    @Deprecated
    Request<PaymentTransaction> buildTransaction(@NonNull String publicAddress, @NonNull BigDecimal amount, int fee);

    /**
     * @deprecated <b>This method was deprecated in version 2.0.0, please use the PaymentQueue,
     * TransactionInterceptor and/or sendTransaction(sync or not) classes</b>
     *
     * Build a Transaction object of the given amount in kin, to the specified public address and with a memo(that
     * can be empty or null).
     * <p> See {@link KinAccount#buildTransactionSync(String, BigDecimal, int, String)} for possibles errors</p>
     *
     * @param publicAddress the account address to send the specified kin amount.
     * @param amount        the amount of kin to transfer.
     * @param fee           the amount of fee(in Quarks) for this transfer (1 Quark = 0.00001 KIN).
     * @param memo          An optional string, can contain a utf-8 string up to 21 bytes in length, included on the
     *                      transaction record.
     * @return {@code Request<TransactionId>}, TransactionId - the transaction identifier
     */
    @Deprecated
    Request<PaymentTransaction> buildTransaction(@NonNull String publicAddress, @NonNull BigDecimal amount, int fee,
                                                 @Nullable String memo);

    /**
     * @deprecated <b>This method was deprecated in version 2.0.0, please use the PaymentQueue,
     * TransactionInterceptor and/or sendTransaction(sync or not) classes</b>
     *
     * Create {@link Request} for signing and sending a transaction
     * <p> See {@link KinAccount#sendTransactionSync(PaymentTransaction)} for possibles errors</p>
     *
     * @param transaction is the transaction object to send.
     * @return {@code Request<TransactionId>}, TransactionId - the transaction identifier.
     */
    @NonNull
    @Deprecated
    Request<TransactionId> sendTransaction(PaymentTransaction transaction);

    /**
     * @deprecated <b>This method was deprecated in version 2.0.0, please use the PaymentQueue,
     * TransactionInterceptor and/or sendTransaction(sync or not) classes</b>
     *
     * Create {@link Request} for signing and sending a transaction from a whitelist.
     * whitelist a transaction means that the user will not pay any fee(if your App is in the Kin whitelist)
     * <p> See {@link KinAccount#sendWhitelistTransactionSync(String)} for possibles errors</p>
     *
     * @param whitelist is the whitelist data (got from the server) which will be used to send the transaction.
     * @return {@code Request<TransactionId>}, TransactionId - the transaction identifier.
     */
    @NonNull
    @Deprecated
    Request<TransactionId> sendWhitelistTransaction(String whitelist);

    /**
     * @deprecated <b>This method was deprecated in version 2.0.0, please use the PaymentQueue,
     * TransactionInterceptor and/or sendTransaction(sync or not) classes</b>
     *
     * Build a Transaction object of the given amount in kin, to the specified public address.
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @param publicAddress the account address to send the specified kin amount.
     * @param amount        the amount of kin to transfer.
     * @param fee           the amount of fee(in Quarks) for this transfer (1 Quark = 0.00001 KIN).
     * @return a Transaction object which also includes the transaction id.
     * @throws AccountNotFoundException if the sender or destination account was not created.
     * @throws OperationFailedException other error occurred.
     */
    @Deprecated
    PaymentTransaction buildTransactionSync(@NonNull String publicAddress, @NonNull BigDecimal amount, int fee) throws OperationFailedException;

    /**
     * @deprecated <b>This method was deprecated in version 2.0.0, please use the PaymentQueue,
     * TransactionInterceptor and/or sendTransaction(sync or not) classes</b>
     *
     * Build a Transaction object of the given amount in kin, to the specified public address and with a memo(that
     * can be empty or null).
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @param publicAddress the account address to send the specified kin amount.
     * @param amount        the amount of kin to transfer.
     * @param fee           the amount of fee(in Quarks) for this transfer (1 Quark = 0.00001 KIN).
     * @param memo          An optional string, can contain a utf-8 string up to 21 bytes in length, included on the
     *                      transaction record.
     * @return a Transaction object which also includes the transaction id.
     * @throws AccountNotFoundException if the sender or destination account was not created.
     * @throws OperationFailedException other error occurred.
     */
    @Deprecated
    PaymentTransaction buildTransactionSync(@NonNull String publicAddress, @NonNull BigDecimal amount, int fee,
                                            @Nullable String memo) throws OperationFailedException;

    /**
     * @deprecated <b>This method was deprecated in version 2.0.0, please use the PaymentQueue,
     * TransactionInterceptor and/or sendTransaction(sync or not) classes</b>
     *
     * send a transaction.
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @param transaction is the transaction object to send.
     * @return TransactionId the transaction identifier.
     * @throws AccountNotFoundException   if the sender or destination account was not created.
     * @throws InsufficientKinException   if account balance has not enough kin.
     * @throws TransactionFailedException if transaction failed, contains blockchain failure details.
     * @throws OperationFailedException   other error occurred.
     */
    @NonNull
    @Deprecated
    TransactionId sendTransactionSync(PaymentTransaction transaction) throws OperationFailedException;

    /**
     * @deprecated <b>This method was deprecated in version 2.0.0, please use the PaymentQueue,
     * TransactionInterceptor and/or sendTransaction(sync or not) classes</b>
     *
     * send a whitelist transaction.
     * whitelist a transaction means that the user will not pay any fee(if your App is in the Kin whitelist)
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @param whitelist is the whitelist data (got from the server) which will be used to send the transaction.
     * @return TransactionId the transaction identifier.
     * @throws AccountNotFoundException   if the sender or destination account was not created.
     * @throws InsufficientKinException   if account balance has not enough kin.
     * @throws TransactionFailedException if transaction failed, contains blockchain failure details.
     * @throws OperationFailedException   other error occurred.
     */
    @NonNull
    @Deprecated
    TransactionId sendWhitelistTransactionSync(String whitelist) throws OperationFailedException;

    /**
     * Create {@link Request} for signing and sending a transaction
     * <p> See {@link KinAccount#sendTransaction(TransactionParams, TransactionInterceptor)}
     * for possibles errors</p>
     *
     * @param transactionParams is the transaction parameters which define the transaction object to send.
     * @return {@code Request<TransactionId>}, TransactionId - the transaction identifier.
     */
    Request<TransactionId> sendTransaction(final TransactionParams transactionParams);

    /**
     * Create {@link Request} for signing and sending a transaction
     * for possibles errors</p>
     *
     * @param transactionParams is the transaction parameters which define the transaction object to send.
     * @param interceptor is the interceptor for the transaction before it is being sent.
     * @return {@code Request<TransactionId>}, TransactionId - the transaction identifier.
     */
    Request<TransactionId> sendTransaction(TransactionParams transactionParams,
                                           @Nullable TransactionInterceptor interceptor);

    /**
     * @return the payment queue.
     */
    PaymentQueue paymentQueue();

    /**
     * Create {@link Request} for getting the current pending balance
     * <p>pending balance is a balance that calculates the expected balance including all of the queued transactions
     * .</p>
     * <p> See {@link KinAccount#getPendingBalanceSync()} for possibles errors</p>
     *
     * @return {@code Request<Balance>} Balance - the pending balance in kin
     */
    Request<Balance> getPendingBalance();

    /**
     * Get the current confirmed pending balance in kin
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @return the pending balance in kin
     * @throws AccountNotFoundException if account was not created
     * @throws OperationFailedException any other error
     */
    @NonNull
    Balance getPendingBalanceSync();

    /**
     * Create {@link Request} for getting the current confirmed balance in kin
     * <p> See {@link KinAccount#getBalanceSync()} for possibles errors</p>
     *
     * @return {@code Request<Balance>} Balance - the balance in kin
     */
    @NonNull
    Request<Balance> getBalance();

    /**
     * Get the current confirmed balance in kin
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @return the balance in kin
     * @throws AccountNotFoundException if account was not created
     * @throws OperationFailedException any other error
     */
    @NonNull
    Balance getBalanceSync() throws OperationFailedException;

    /**
     * Get current account status on blockchain network.
     *
     * @return account status, either {@link AccountStatus#NOT_CREATED}, or {@link
     * AccountStatus#CREATED}
     * @throws OperationFailedException any other error
     */
    @AccountStatus
    int getStatusSync() throws OperationFailedException;

    /**
     * Create {@link Request} for getting current account status on blockchain network.
     * <p> See {@link KinAccount#getStatusSync()} for possibles errors</p>
     *
     * @return account status, either {@link AccountStatus#NOT_CREATED}, or {@link
     * AccountStatus#CREATED}
     */
    Request<Integer> getStatus();

    /**
     * Creates and adds listener for balance changes of this account, use returned {@link ListenerRegistration} to
     * stop listening. <p><b>Note:</b> Events will be fired on background thread.</p>
     *
     * @param listener listener object for payment events
     */
    ListenerRegistration addBalanceListener(@NonNull final EventListener<Balance> listener);

    /**
     * Creates and adds listener for payments concerning this account, use returned {@link ListenerRegistration} to
     * stop listening. <p><b>Note:</b> Events will be fired on background thread.</p>
     *
     * @param listener listener object for payment events
     */
    ListenerRegistration addPaymentListener(@NonNull final EventListener<PaymentInfo> listener);

    /**
     * Creates and adds listener for account creation event, use returned {@link ListenerRegistration} to stop
     * listening. <p><b>Note:</b> Events will be fired on background thread.</p>
     *
     * @param listener listener object for payment events
     */
    ListenerRegistration addAccountCreationListener(final EventListener<Void> listener);

    /**
     * Creates and adds listener for pending balance changes of this account, use returned
     * {@link ListenerRegistration} to
     * stop listening. <p><b>Note:</b> Events will be fired on background thread.</p>
     *
     * <p>pending balance is a balance that calculates the expected balance including all of the queued transactions
     * .</p>
     *
     * @param listener listener object for payment events
     */
    ListenerRegistration addPendingBalanceListener(final EventListener<Balance> listener);

    /**
     * Export the account data as a JSON string. The seed is encrypted.
     *
     * @param passphrase The passphrase with which to encrypt the seed
     * @return A JSON representation of the data as a string
     */
    String export(@NonNull String passphrase) throws CryptoException;
}
