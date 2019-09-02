package kin.sdk.internal.queue;

import kin.sdk.exception.InsufficientKinException;
import kin.sdk.queue.PendingPayment;

import java.util.List;

public interface PaymentQueueManager {

    /**
     * enqueue this pending payment.
     *
     * @param pendingPayment the pending payment to enqueue
     * @throws InsufficientKinException if currently there is not enough balance to enqueue this pending payment.
     */
    void enqueue(PendingPayment pendingPayment) throws InsufficientKinException;

    /**
     * @return the list of pending payments that are in the queue.
     */
    List<PendingPayment> getPaymentQueue();

    /**
     * @return the number of pending payments in the queue.
     */
    int getPendingPaymentCount();

}
