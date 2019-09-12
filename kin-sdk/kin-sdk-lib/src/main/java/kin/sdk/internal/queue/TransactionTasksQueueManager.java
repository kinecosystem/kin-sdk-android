package kin.sdk.internal.queue;

import java.util.List;

import kin.sdk.TransactionInterceptor;
import kin.sdk.queue.PendingPayment;
import kin.sdk.queue.TransactionProcess;
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
     * @param transactionInterceptor an optional interceptor that will be used to intercept
     *      *                        the transaction.
     */
    void enqueue(PaymentTransactionParams transactionParams,
                 TransactionInterceptor<TransactionProcess> transactionInterceptor);

    /**
     * @return true if currently there is a transaction in progress, meaning it will return true if
     * there is a transaction that was entered to the task queue and until it is finished(success
     * or fail).
     */
    boolean transactionInProgress();
}
