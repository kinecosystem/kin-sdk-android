package kin.sdk.internal.queue;

import android.support.annotation.NonNull;
import kin.sdk.exception.InsufficientKinException;
import kin.sdk.queue.PaymentQueue;
import kin.sdk.queue.PendingPayment;

import java.math.BigDecimal;

public class PaymentQueueImpl implements PaymentQueue {

    // TODO: 2019-08-04 implement

    @Override
    public PendingPayment enqueuePayment(@NonNull String publicAddress, @NonNull BigDecimal amount) throws InsufficientKinException {
        return null;
    }

    @Override
    public PendingPayment enqueuePayment(@NonNull String publicAddress, @NonNull BigDecimal amount, Object metadata) throws InsufficientKinException {
        return null;
    }

    @Override
    public void setTransactionInterceptor(TransactionInterceptor interceptor) {

    }

    @Override
    public void addEventsListener(EventsListener listener) {

    }

    @Override
    public void setFee(int fee) {

    }

    @Override
    public Status status() {
        return null;
    }

    public static class StatusImpl implements PaymentQueueImpl.Status {

        @Override
        public boolean transactionInProgress() {
            return false;
        }

        @Override
        public int pendingPaymentsCount() {
            return 0;
        }
    }

}
