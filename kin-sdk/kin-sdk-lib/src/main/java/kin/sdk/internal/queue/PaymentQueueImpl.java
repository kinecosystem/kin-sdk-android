package kin.sdk.internal.queue;

import android.support.annotation.NonNull;
import kin.sdk.TransactionInterceptor;
import kin.sdk.exception.InsufficientKinException;
import kin.sdk.internal.data.PendingBalanceUpdaterImpl;
import kin.sdk.internal.events.EventsManagerImpl;
import kin.sdk.queue.PaymentQueue;
import kin.sdk.queue.PendingPayment;

import java.math.BigDecimal;

public class PaymentQueueImpl implements PaymentQueue {

    private static final long DELAY_BETWEEN_PAYMENTS_MILLIS = 3000; // TODO: 2019-08-15 get those number from somewhere
    private static final long QUEUE_TIMEOUT_MILLIS = 10000;         // TODO: 2019-08-15 get those number from somewhere
    private static final int MAX_NUM_OF_PAYMENTS = 100;

    private final String sourcePublicAddress;
    private final PaymentQueueManager paymentQueueManager;
    private final TransactionTaskQueueManager txTaskQueueManager;

    public PaymentQueueImpl(@NonNull String sourcePublicAddress) {
        this.sourcePublicAddress = sourcePublicAddress;
        this.txTaskQueueManager = new TransactionTaskQueueManagerImpl();
        this.paymentQueueManager = new PaymentQueueManagerImpl(txTaskQueueManager, new QueueSchedulerImpl(),
                new PendingBalanceUpdaterImpl(), new EventsManagerImpl(),
                DELAY_BETWEEN_PAYMENTS_MILLIS, QUEUE_TIMEOUT_MILLIS, MAX_NUM_OF_PAYMENTS);
    }

    @Override
    public PendingPayment enqueuePayment(@NonNull String publicAddress, @NonNull BigDecimal amount) throws InsufficientKinException {
        return enqueuePayment(publicAddress, amount, null);
    }

    @Override
    public PendingPayment enqueuePayment(@NonNull String publicAddress, @NonNull BigDecimal amount, Object metadata) throws InsufficientKinException {
        PendingPayment pendingPayment = new PendingPaymentImpl(publicAddress, sourcePublicAddress, amount, metadata);
        paymentQueueManager.enqueue(pendingPayment);
        return pendingPayment;
    }

    @Override
    public void setTransactionInterceptor(TransactionInterceptor interceptor) {
        // TODO: 2019-08-15 implement
    }

    @Override
    public void addEventsListener(EventsListener listener) {
        // TODO: 2019-08-15 implement
    }

    @Override
    public void setFee(int fee) {
        // TODO: 2019-08-14 should we keep it here or set it directly into the task queue manager
    }

    @Override
    public boolean transactionInProgress() {
        return false; // TODO: 2019-08-15 implement
    }

    @Override
    public int pendingPaymentsCount() {
        return paymentQueueManager.getPendingPaymentCount();
    }

}
