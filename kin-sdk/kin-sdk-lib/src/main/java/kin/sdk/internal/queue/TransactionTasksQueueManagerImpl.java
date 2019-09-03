package kin.sdk.internal.queue;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import kin.sdk.TransactionInterceptor;
import kin.sdk.internal.events.EventsManager;
import kin.sdk.queue.PendingPayment;
import kin.sdk.transactiondata.PaymentTransactionParams;

/**
 * Class which manage the transaction task queue.
 */
class TransactionTasksQueueManagerImpl implements TransactionTasksQueueManager, TaskFinishListener {

    private final TasksQueue taskQueue;
    private final EventsManager eventsManager;
    private final int maxNumOfPayments;
    private Queue<List<PendingPayment>> batchPendingPaymentsQueue;
    private PaymentTransactionParams currentTransactionParams;
    private TransactionInterceptor currentTransactionParamsInterceptor;
    private boolean transactionInProgress = false;

    TransactionTasksQueueManagerImpl(TasksQueue taskQueue, EventsManager eventsManager,
                                     int maxNumOfPayments) {
        this.taskQueue = taskQueue;
        this.taskQueue.setTaskFinishListener(this);
        this.eventsManager = eventsManager;
        this.maxNumOfPayments = maxNumOfPayments;
        batchPendingPaymentsQueue = new ArrayDeque<>();
    }

    @Override
    public synchronized void enqueue(List<PendingPayment> pendingPayments) {
        mergePayments(pendingPayments);
        sendTask();
    }

    @Override
    public synchronized void enqueue(PaymentTransactionParams paymentTransactionParams,
                                     TransactionInterceptor transactionInterceptor) {
        if (currentTransactionParams != null) {
            // TODO: 2019-08-26 throw some exception, will decide later
        } else {
            currentTransactionParams = paymentTransactionParams;
            currentTransactionParamsInterceptor = transactionInterceptor;
            sendTask();
        }
    }

    @Override
    public synchronized boolean transactionInProgress() {
        return transactionInProgress;
    }

    @Override
    public synchronized void onTaskFinish(final SendTransactionTask task) {
        transactionInProgress = false;
        sendTask();
    }

    /**
     * Taking the new pending payment and instead of just adding it to the queue, we will
     * merge/combine this list with the with whatever we can in the queue of batched payments list
     * In this way instead sending many transactions we will have less transactions to send.
     * For example, if currently our list consist of a list of 2 items, the first with 100 batch
     * payments and the second with 40 and our new pending payments consist of 70 items.
     * Then our new queue of list of batch payments will now have the first item still 100,
     * second item 100 and the third item with 10.
     *
     * @param pendingPayments the new pending payments list that needed to be enqueued and batched.
     */
    private void mergePayments(List<PendingPayment> pendingPayments) {
        // the current number of pending payments that need to be batched
        int numOfRestOfPendingPaymentsToBatch = pendingPayments.size();
        // the index in the pending payments list in which to start take elements to next batch
        // payments list.
        int pendingPaymentIndexToTakeFrom = 0;

        for (List<PendingPayment> batchPayments : batchPendingPaymentsQueue) {
            // the current number of pending payment the can be entered to the current batch
            // payment list
            int numOfEntriesAvailableInBatchList =
                    Math.abs(maxNumOfPayments - batchPayments.size());
            if (numOfEntriesAvailableInBatchList == 0) {
                // no room to add pending payments so go to next batch payments
                continue;
            } else {
                // If there are more pending payments then the number of payments to fill then take
                // a sublist of them and fill and take the rest and fill the next batch payments,
                // otherwise just add all of them to the current batch payments
                if (numOfEntriesAvailableInBatchList >= numOfRestOfPendingPaymentsToBatch) {
                    batchPayments.addAll(pendingPayments);
                    return;
                } else {
                    // update the rest of pending payments that still need to be batched.
                    numOfRestOfPendingPaymentsToBatch = -numOfEntriesAvailableInBatchList;
                    List<PendingPayment> subPendingPayments =
                            pendingPayments.subList(pendingPaymentIndexToTakeFrom,
                                    pendingPaymentIndexToTakeFrom + numOfEntriesAvailableInBatchList);
                    // update the index
                    pendingPaymentIndexToTakeFrom = +numOfEntriesAvailableInBatchList;
                    batchPayments.addAll(subPendingPayments);
                }
            }
        }
        // add to the queue in case that there are no batchPayments
        batchPendingPaymentsQueue.add(pendingPayments.subList(pendingPaymentIndexToTakeFrom,
                pendingPaymentIndexToTakeFrom + numOfRestOfPendingPaymentsToBatch));
    }

    /**
     * Sending the next task to the task queue if there is no transaction in progress.
     * If there is a transaction param task then send it first, otherwise send the next task in
     * the queue.
     * If no task in the queue then we don't do anything.
     */
    private void sendTask() {
        // Check if there is a task running and if not then schedule a task
        if (!transactionInProgress()) {
            if (currentTransactionParams != null) {
                sendTransactionParamsTask();
            } else if (batchPendingPaymentsQueue.size() > 0) {
                sendPendingPaymentsTask();
            }
        }
    }

    private void sendTransactionParamsTask() {
        PaymentTransactionParams toSend = currentTransactionParams;
        currentTransactionParams = null;
        TransactionInterceptor transactionInterceptor = currentTransactionParamsInterceptor;
        currentTransactionParamsInterceptor = null;
        taskQueue.scheduleTransactionParamsTask(toSend, transactionInterceptor);
        transactionInProgress = true;
    }

    private void sendPendingPaymentsTask() {
        taskQueue.schedulePendingPaymentsTask(batchPendingPaymentsQueue.poll());
        transactionInProgress = true;
    }
}
