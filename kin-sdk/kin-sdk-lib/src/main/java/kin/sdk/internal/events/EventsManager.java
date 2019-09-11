package kin.sdk.internal.events;

import java.util.List;

import kin.sdk.TransactionId;
import kin.sdk.exception.KinException;
import kin.sdk.queue.PaymentQueue;
import kin.sdk.queue.PendingPayment;
import kin.sdk.transactiondata.BatchPaymentTransaction;

public interface EventsManager {

    /**
     * set the event listener
     *
     * @param eventListener the event listener to set
     */
    void setEventListener(PaymentQueue.EventListener eventListener);

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
     *                    <p> See {@link BatchPaymentTransaction} and {@link PendingPayment} for
     *                    more
     *                    information</p>
     */
    void onTransactionSend(BatchPaymentTransaction transaction, List<PendingPayment> payments);

    /**
     * Invoked when a Transaction just before the transaction will be send.
     *
     * @param transaction the transaction to send
     * @param payments    the list of payments that the transaction consist of.
     *                    <p> See {@link BatchPaymentTransaction} and {@link PendingPayment} for
     *                    more
     *                    information</p>
     */
    void onTransactionSendSuccess(TransactionId transactionId, List<PendingPayment> payments);

    /**
     * Invoked when a Transaction just before the transaction will be send.
     *
     * @param transaction the transaction to send
     * @param payments    the list of payments that the transaction consist of.
     * @param exception   the exception the occurred.
     *                    <p> See {@link BatchPaymentTransaction} and {@link PendingPayment} for
     *                    more
     *                    information</p>
     */
    void onTransactionSendFailed(List<PendingPayment> payments, KinException exception);

}
