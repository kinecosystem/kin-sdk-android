package kin.sdk.internal.queue;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

import kin.base.KeyPair;
import kin.sdk.TransactionInterceptor;
import kin.sdk.exception.InsufficientKinException;
import kin.sdk.internal.Utils;
import kin.sdk.internal.blockchain.GeneralBlockchainInfoRetriever;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.internal.data.PendingBalanceUpdaterImpl;
import kin.sdk.internal.events.EventsManager;
import kin.sdk.internal.events.EventsManagerImpl;
import kin.sdk.queue.PaymentQueue;
import kin.sdk.queue.PaymentQueueTransactionProcess;
import kin.sdk.queue.PendingPayment;
import kin.sdk.queue.TransactionProcess;
import kin.sdk.transactiondata.TransactionParams;

public class PaymentQueueImpl implements PaymentQueue {

    private final KeyPair accountFrom;
    private final PaymentQueueManager paymentQueueManager;
    private final TransactionTasksQueueManager txTasksQueueManager;
    private final EventsManager eventsManager;
    private final TasksQueueImpl tasksQueue;

    public PaymentQueueImpl(@NonNull KeyPair accountFrom, TransactionSender transactionSender,
                            GeneralBlockchainInfoRetriever generalBlockchainInfoRetriever,
                            PaymentQueueConfiguration configuration) {
        this.accountFrom = accountFrom;
        this.eventsManager = new EventsManagerImpl();
        tasksQueue = new TasksQueueImpl(transactionSender, eventsManager,
                generalBlockchainInfoRetriever, accountFrom);
        this.txTasksQueueManager = new TransactionTasksQueueManagerImpl(tasksQueue, eventsManager
                , configuration.maxNumOfPayments);
        this.paymentQueueManager = new PaymentQueueManagerImpl(txTasksQueueManager,
                new QueueSchedulerImpl(), new PendingBalanceUpdaterImpl(), eventsManager,
                configuration);
    }

    @Override
    public PendingPayment enqueuePayment(@NonNull String destinationPublicAddress,
                                         @NonNull BigDecimal amount) throws InsufficientKinException {
        return enqueuePayment(destinationPublicAddress, amount, null);
    }

    @Override
    public PendingPayment enqueuePayment(@NonNull String destinationPublicAddress,
                                         @NonNull BigDecimal amount, Object metadata) throws InsufficientKinException {
        Utils.checkForNegativeAmount(amount);
        Utils.checkAddressNotEmpty(destinationPublicAddress);
        PendingPayment pendingPayment = new PendingPaymentImpl(destinationPublicAddress,
                accountFrom.getAccountId(), amount, metadata);
        paymentQueueManager.enqueue(pendingPayment);
        return pendingPayment;
    }

    /**
     * enqueue a non blocking transaction. This transaction will be pushed to the top of the queue.
     *
     * @param transactionParams      the transaction parameters
     * @param transactionInterceptor an optional interceptor that will be used to intercept the
     *                               transaction.
     */
    public void enqueueTransactionParams(TransactionParams transactionParams,
                                         TransactionInterceptor<TransactionProcess> transactionInterceptor) {

        tasksQueue.scheduleTransactionParamsTask(transactionParams, transactionInterceptor);
    }

    @Override
    public void setTransactionInterceptor(TransactionInterceptor<PaymentQueueTransactionProcess> transactionInterceptor) {
        tasksQueue.setPaymentQueueTransactionInterceptor(transactionInterceptor);
    }

    @Override
    public void setEventListener(EventListener eventListener) {
        eventsManager.setEventListener(eventListener);
    }

    @Override
    public void setFee(int fee) {
        Utils.checkForNegativeFee(fee);
        tasksQueue.setBatchPaymentsFee(fee);
    }

    @Override
    public boolean transactionInProgress() {
        return txTasksQueueManager.transactionInProgress();
    }

    @Override
    public int pendingPaymentsCount() {
        return paymentQueueManager.getPendingPaymentCount();
    }

    @Override
    public void releaseQueue() {
        tasksQueue.stopTaskQueue();
    }

    public static class PaymentQueueConfiguration {

        private long delayBetweenPaymentsMillis; // TODO: 2019-08-15 get this number from somewhere
        private long queueTimeoutMillis;         // TODO: 2019-08-15 get this number from somewhere
        private int maxNumOfPayments;            // TODO: 2019-08-15 get this number from somewhere

        public PaymentQueueConfiguration(long delayBetweenPaymentsMillis, long queueTimeoutMillis,
                                         int maxNumOfPayments) {
            this.delayBetweenPaymentsMillis = delayBetweenPaymentsMillis;
            this.queueTimeoutMillis = queueTimeoutMillis;
            this.maxNumOfPayments = maxNumOfPayments;
        }

        long getDelayBetweenPayments() {
            return delayBetweenPaymentsMillis;
        }

        long getQueueTimeout() {
            return queueTimeoutMillis;
        }

        int getMaxNumOfPayments() {
            return maxNumOfPayments;
        }
    }

}
