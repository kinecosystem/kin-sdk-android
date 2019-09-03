package kin.sdk.internal.queue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kin.sdk.Balance;
import kin.sdk.internal.data.PendingBalanceUpdater;
import kin.sdk.internal.events.EventsManager;
import kin.sdk.queue.PendingPayment;

class PaymentQueueManagerImpl implements PaymentQueueManager {

    private final TransactionTaskQueueManager txTaskQueueManager;
    private final QueueScheduler queueScheduler;
    private final PendingBalanceUpdater pendingBalanceUpdater;
    private final EventsManager eventsManager;
    private final long delayBetweenPayments;
    private final long queueTimeout;
    private final int maxNumOfPayments;
    private List<PendingPayment> queue;
    private Runnable delayTask;

    PaymentQueueManagerImpl(TransactionTaskQueueManager txTaskQueueManager, QueueScheduler queueScheduler,
                            PendingBalanceUpdater pendingBalanceUpdater, EventsManager eventsManager,
                            long delayBetweenPayments, long queueTimeout, int maxNumOfPayments) {
        this.txTaskQueueManager = txTaskQueueManager;
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
        // TODO: 2019-09-03 add events whenever it is necessary
        queueScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                // 1. validate pending balance
                validatePendingBalance(); // TODO: 2019-08-15 handle it

                if (getPendingPaymentCount() == maxNumOfPayments - 1) {
                    addToQueue(pendingPayment);
                    sendPendingPaymentsToTaskQueue();
                } else {
                    handlePendingPayment();
                }
            }

            private void handlePendingPayment() {
                addToQueue(pendingPayment);
                resetScheduler();
                if (queue.size() == 1) {
                    queueScheduler.scheduleDelayed(new DelayTask(), queueTimeout);
                }
            }

            private void addToQueue(PendingPayment pendingPayment) {
                queue.add(pendingPayment);
            }

            private void resetScheduler() {
                queueScheduler.removePendingTask(delayTask);
                delayTask = new DelayTask();
                queueScheduler.scheduleDelayed(delayTask, delayBetweenPayments);
            }
        });
    }

    @Override
    public List<PendingPayment> getPaymentQueue() {
        // although it is used internally, we return an unmodifiableList here to reduce the
        // chances of mistakes.
        return Collections.unmodifiableList(queue);
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
        List<PendingPayment> pendingPayments = queue;
        queue = new ArrayList<>(maxNumOfPayments);
        txTaskQueueManager.enqueue(pendingPayments);
    }

    private class DelayTask implements Runnable { // TODO: 2019-09-03 change name

        @Override
        public void run() {
            validatePendingBalance();
            // TODO: 2019-08-08 act upon the validation
            sendPendingPaymentsToTaskQueue();
        }
    }

}
