package kin.sdk.internal.queue;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

import kin.base.KeyPair;
import kin.sdk.TransactionId;
import kin.sdk.TransactionInterceptor;
import kin.sdk.exception.InsufficientKinException;
import kin.sdk.internal.Utils;
import kin.sdk.internal.blockchain.GeneralBlockchainInfoRetriever;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.internal.data.PendingBalanceUpdaterImpl;
import kin.sdk.internal.events.EventsManager;
import kin.sdk.internal.events.EventsManagerImpl;
import kin.sdk.queue.PaymentQueue;
import kin.sdk.queue.PendingPayment;
import kin.sdk.transactiondata.TransactionParams;

public class PaymentQueueImpl implements PaymentQueue {

    private static final long DELAY_BETWEEN_PAYMENTS_MILLIS = 3000; // TODO: 2019-08-15 get those
    // number from somewhere
    private static final long QUEUE_TIMEOUT_MILLIS = 10000;         // TODO: 2019-08-15 get those
    // number from somewhere
    private static final int MAX_NUM_OF_PAYMENTS = 100;

    private final KeyPair accountFrom;
    private final PaymentQueueManager paymentQueueManager;
    private final TransactionTasksQueueManager txTasksQueueManager;
    private final EventsManager eventsManager;
    private final TasksQueueImpl tasksQueue;

    public PaymentQueueImpl(@NonNull KeyPair accountFrom, TransactionSender transactionSender,
                            GeneralBlockchainInfoRetriever generalBlockchainInfoRetriever) {
        this.accountFrom = accountFrom;
        this.eventsManager = new EventsManagerImpl();
        tasksQueue = new TasksQueueImpl(transactionSender, eventsManager,
                generalBlockchainInfoRetriever, accountFrom);
        this.txTasksQueueManager = new TransactionTasksQueueManagerImpl(tasksQueue, eventsManager
                , MAX_NUM_OF_PAYMENTS);
        this.paymentQueueManager = new PaymentQueueManagerImpl(txTasksQueueManager,
                new QueueSchedulerImpl(),
                new PendingBalanceUpdaterImpl(), eventsManager,
                DELAY_BETWEEN_PAYMENTS_MILLIS, QUEUE_TIMEOUT_MILLIS, MAX_NUM_OF_PAYMENTS);
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
    public TransactionId enqueueTransactionParams(TransactionParams transactionParams,
                                                  TransactionInterceptor transactionInterceptor) {

        tasksQueue.scheduleTransactionParamsTask(transactionParams, transactionInterceptor);
        return null;
        // TODO: 2019-09-02 because we want it to be sync then we should handle it here and
        //  return the transaction id
    }

    @Override
    public void setTransactionInterceptor(TransactionInterceptor transactionInterceptor) {
        tasksQueue.setPendingPaymentsTransactionInterceptor(transactionInterceptor);
    }

    @Override
    public void addEventsListener(EventsListener eventListener) {
        // TODO: 2019-08-27 did we agree that we call it add? meaning we will maintain a list of
        //  them in the events manager?
    }

    @Override
    public void setFee(int fee) {
        Utils.checkForNegativeFee(fee);
        tasksQueue.setFee(fee);
    }

    @Override
    public boolean transactionInProgress() {
        return txTasksQueueManager.transactionInProgress();
    }

    @Override
    public int pendingPaymentsCount() {
        return paymentQueueManager.getPendingPaymentCount();
    }

}
