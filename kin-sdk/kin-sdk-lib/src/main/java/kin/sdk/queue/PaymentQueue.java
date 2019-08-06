package kin.sdk.queue;

import android.support.annotation.NonNull;
import kin.sdk.TransactionId;
import kin.sdk.exception.InsufficientKinException;
import kin.sdk.exception.KinSdkException;
import kin.sdk.transaction_data.BatchPaymentTransaction;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentQueue {

    interface Status {

        /**
         * @return true if there is a transaction in process.
         */
        boolean transactionInProgress();

        /**
         * @return the number of pending payments in the queue
         */
        int pendingPaymentsCount();
    }

    /**
     * Register/unregister for queue events.
     */
    interface EventsListener {

        /**
         * Invoked when a new payment is enqueued
         *
         * @param payment the pending payment which was created when the payment was enqueued
         *                <p> See {@link PendingPayment} </p>
         */
        void onPaymentEnqueued(PendingPayment payment);

        /**
         * Invoked when a Transaction just before the transaction will be send.
         *
         * @param transaction the transaction to send
         * @param payments    the list of payments that the transaction consist of.
         *                    <p> See {@link BatchPaymentTransaction} and {@link PendingPayment} for more
         *                    information</p>
         */
        void onTransactionSend(BatchPaymentTransaction transaction, List<PendingPayment> payments);

        /**
         * Invoked when a Transaction just before the transaction will be send.
         *
         * @param transaction the transaction to send
         * @param payments    the list of payments that the transaction consist of.
         *                    <p> See {@link BatchPaymentTransaction} and {@link PendingPayment} for more
         *                    information</p>
         */
        void onTransactionSendSuccess(BatchPaymentTransaction transaction, List<PendingPayment> payments);

        /**
         * Invoked when a Transaction just before the transaction will be send.
         *
         * @param transaction the transaction to send
         * @param payments    the list of payments that the transaction consist of.
         * @param exception   the exception the occurred.
         *                    <p> See {@link BatchPaymentTransaction} and {@link PendingPayment} for more
         *                    information</p>
         */
        void onTransactionSendFailed(BatchPaymentTransaction transaction, List<PendingPayment> payments,
                                     KinSdkException exception);
    }

    /**
     * provide the generated transaction before sending to the blockchain.
     */
    interface TransactionInterceptor {

        /**
         * Intercept the generated transaction before sending to the blockchain
         * Will be fired when the transaction is ready, and before it will be sending to the blockchain.
         * <p> Note - This method will be called from a bg thread, blocking IO operations are safe to use.</p>
         *
         * @param transactionProcess can be used to access both generated transaction and all of its associated
         *                           PendingPayments.
         *                           <p>Note - It's for the dev to decide whether to call TransactionProcess.send()
         *                           to send using the sdk, w
         *                           with or without a whitelisted payload, or to send it to the blockchain by dev
         *                           server and just return back the transaction id</p>
         * @return the transaction identifier.
         * @throws Exception
         */
        TransactionId interceptTransactionSending(TransactionProcess transactionProcess) throws Exception;
    }

    /**
     * Enqueue new payment
     *
     * @param publicAddress the account address to send the specified kin amount.
     * @param amount        the amount of kin to transfer.
     * @return A pending payment object which represent the new payment that was enqueued
     * @throws InsufficientKinException
     */
    PendingPayment enqueuePayment(@NonNull String publicAddress, @NonNull BigDecimal amount) throws InsufficientKinException;

    /**
     * Enqueue new payment
     *
     * @param publicAddress the account address to send the specified kin amount.
     * @param amount        the amount of kin to transfer.
     * @param metadata      is a Payment key-value metadata.
     *                      it is a general object that can be added to any payment and will be available in queue
     *                      callbacks.
     *                      This object is not being sent through the network and it is only been used locally.
     * @return A pending payment object which represent the new payment that was enqueued
     * @throws InsufficientKinException
     */
    PendingPayment enqueuePayment(@NonNull String publicAddress, @NonNull BigDecimal amount, Object metadata) throws InsufficientKinException;

    /**
     * Set a transaction interceptor.
     * This can be used, for example, for whitelisting and/or transaction verification on the server side.
     *
     * @param interceptor is the TransactionInterceptor to set.
     *                    <p> See {@link TransactionInterceptor} </p>
     */
    void setTransactionInterceptor(TransactionInterceptor interceptor);

    /**
     * @param listener is a queue event listener.
     */
    void addEventsListener(EventsListener listener);

    /**
     * Setter for the fee.
     * If this fee is below the minimum then the transaction will fail.
     *
     * @param fee the amount of fee(in quarks) for each payment
     */
    void setFee(int fee);

    /**
     * provide information on this queue
     *
     * @return the status pf this queue.
     */
    Status status(); // TODO: 2019-08-04 this is a bit weird to return an object which has 2 methods to query some
    // info. ,maybe we should just add those methods

}
