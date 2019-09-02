package kin.sdk.internal.queue;

import java.util.ArrayList;
import java.util.List;

import kin.sdk.Balance;
import kin.sdk.internal.data.PendingBalanceUpdater;
import kin.sdk.internal.events.EventsManager;
import kin.sdk.queue.PendingPayment;

class PaymentQueueManagerImpl implements PaymentQueueManager {

    private final TransactionTasksQueueManager txTasksQueueManager;
    private final QueueScheduler queueScheduler;
    private final PendingBalanceUpdater pendingBalanceUpdater;
    private final EventsManager eventsManager;
    private final long delayBetweenPayments;
    private final long queueTimeout;
    private final int maxNumOfPayments;
    private List<PendingPayment> queue;
    private Runnable delayTask;

    PaymentQueueManagerImpl(TransactionTasksQueueManager txTasksQueueManager, QueueScheduler queueScheduler,
                            PendingBalanceUpdater pendingBalanceUpdater, EventsManager eventsManager,
                            long delayBetweenPayments, long queueTimeout, int maxNumOfPayments) {
        this.txTasksQueueManager = txTasksQueueManager;
        this.queueScheduler = queueScheduler;
        this.pendingBalanceUpdater = pendingBalanceUpdater;
        this.eventsManager = eventsManager;
        this.delayBetweenPayments = delayBetweenPayments;
        this.queueTimeout = queueTimeout;
        this.maxNumOfPayments = maxNumOfPayments;
        queue = new ArrayList<>(maxNumOfPayments);
        delayTask = new DelayTask();
    }

    @Override
    public void enqueue(final PendingPayment pendingPayment) {
        queueScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                // 1. validate pending balance
                validatePendingBalance(); // TODO: 2019-08-15 handle it when starting the task

                if (getPendingPaymentCount() == maxNumOfPayments - 1) {
                    // 2. add to queue
                    // 3. cancel scheduling
                    // 4. maybe events in later stages
                    // 5. clear and send to task queue manager

                    queue.add(pendingPayment);
                    // TODO: 2019-08-08 maybe send events here
                    sendPendingPaymentsToTaskQueue();
                } else {
                    addToQueue();
                }
            }

            private void addToQueue() {
                // 1. add to list
                // 2. handle events later (maybe also change the execution)
                // 3. reset and schedule delay
                // 4. schedule timeout if first in list

                queue.add(pendingPayment);
                // TODO: 2019-08-08 maybe send events here
                queueScheduler.removePendingTask(delayTask);
                delayTask = new DelayTask();
                queueScheduler.scheduleDelayed(delayTask, delayBetweenPayments);
                if (queue.size() == 1) {
                    queueScheduler.scheduleDelayed(new DelayTask(), queueTimeout);
                }
            }
        });
    }

    @Override
    public List<PendingPayment> getPaymentQueue() {
        // TODO: 2019-08-15 need to think why exactly if we really need it? is it only for the pending balance updater?
        //  maybe return a copy or make it immutable/read only...
        return queue;
    }

    @Override
    public int getPendingPaymentCount() {
        return queue.size();
    }

    private void validatePendingBalance() {
        Balance pendingBalance = pendingBalanceUpdater.getPendingBalance();
        // TODO: 2019-08-08 implement check and validate the pending balance
    }

    private void sendPendingPaymentsToTaskQueue() {
        queueScheduler.removeAllPendingTasks();
        // TODO: 2019-08-08 maybe send events
        List<PendingPayment> pendingPayments = queue;
        queue = new ArrayList<>(maxNumOfPayments);
        txTasksQueueManager.enqueue(pendingPayments);
    }

    private class DelayTask implements Runnable {

        @Override
        public void run() {
            validatePendingBalance();
            // TODO: 2019-08-08 act upon the validation
            sendPendingPaymentsToTaskQueue();
        }
    }

}
