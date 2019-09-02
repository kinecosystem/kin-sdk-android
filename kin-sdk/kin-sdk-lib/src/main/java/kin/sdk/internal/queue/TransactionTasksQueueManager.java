package kin.sdk.internal.queue;

import java.util.List;

import kin.sdk.queue.PendingPayment;
import kin.sdk.transactiondata.PaymentTransactionParams;

public interface TransactionTasksQueueManager {


    /**
     * enqueue pending payments list.
     *
     * @param pendingPayments the list of pending payments to enqueue.
     */
    void enqueue(List<PendingPayment> pendingPayments);

    /**
     * enqueue the transaction parameters object which will later on become a transaction.
     *
     * @param transactionParams the transaction parameters
     */
    void enqueue(PaymentTransactionParams transactionParams); // TODO: 2019-08-26 should we use
    // PaymentTransactionParams or TransactionParams

    /**
     * @return true if currently there is a transaction in progress, meaning it will return true if
     * there is a transaction that was entered to the task queue and until it is finished(success
     * or fail).
     */
    boolean transactionInProgress();
}
