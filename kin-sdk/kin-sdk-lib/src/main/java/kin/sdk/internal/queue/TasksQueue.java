package kin.sdk.internal.queue;

import java.util.List;

import kin.sdk.TransactionInterceptor;
import kin.sdk.queue.PendingPayment;
import kin.sdk.transactiondata.TransactionParams;

public interface TasksQueue {

    /**
     * Scheduling a batch payment transaction task to run as soon as possible.
     * Task are running sequentially so if there are tasks in the queue then they will run before
     * this task.
     * If this task has high priority then it will enter to the top of the queue, meaning that it
     * will be executed next.
     *
     * @param batchPendingPayments the list of batched pending payments from which we will create
     *                            a batch transaction.
     */
    void schedulePendingPaymentsTask(List<PendingPayment> batchPendingPayments);

    /**
     * Scheduling a payment transaction params task to run as soon as possible.
     * Task are running sequentially so if there are tasks in the queue then they will run before
     * this task.
     * If this task has high priority then it will enter to the top of the queue, meaning that it
     * will be executed next.
     *
     * @param paymentTransactionParams is the object from which we will create the payment
     *                                 transaction
     */
    void scheduleTransactionParamsTask(TransactionParams paymentTransactionParams);

    /**
     * Stop all tasks in the queue.
     * <p><b>Note that after calling this method then this task queue is not usable anymore and
     * any call to scheduleTask wont work.
     * You will need to create a new TasksQueue.</b></p>
     */
    void stopTaskQueue(); // TODO: 2019-08-27 should we even try to stop the queue or it will
    // always live? i think it should live as long as needed

    /**
     * Setter for the task finish listener.
     *
     * @param taskFinishListener is listener
     */
    void setTaskFinishListener(TaskFinishListener taskFinishListener);

    /**
     * Setter for the fee
     * If this fee is below the minimum then the transaction will fail.
     * If no fee will be set then the minimum blockchain fee wil be used.
     *
     * @param fee the amount of fee(in Quarks) for each payment (1 Quark = 0.00001 KIN).
     */
    void setFee(int fee);

    /**
     * Set a transaction interceptor.
     *
     * @param transactionInterceptor is the TransactionInterceptor to set.
     *                               <p> See {@link TransactionInterceptor} </p>
     */
    void setTransactionInterceptor(TransactionInterceptor transactionInterceptor);
}
