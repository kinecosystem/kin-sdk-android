package kin.sdk.internal.events;

import java.util.List;

import kin.sdk.TransactionId;
import kin.sdk.exception.KinException;
import kin.sdk.queue.PaymentQueue;
import kin.sdk.queue.PendingPayment;
import kin.sdk.transactiondata.BatchPaymentTransaction;

public class EventsManagerImpl implements EventsManager {

    private PaymentQueue.EventListener eventListener;

    public EventsManagerImpl() {
    }

    public EventsManagerImpl(PaymentQueue.EventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public void setEventListener(PaymentQueue.EventListener eventListener) {
        // TODO: 2019-09-05 probably implement as a list of them and not one
        this.eventListener = eventListener;
    }

    @Override
    public void onPaymentEnqueued(PendingPayment payment) {
        if (eventListener != null) {
            eventListener.onPaymentEnqueued(payment);
        }
    }

    @Override
    public void onTransactionSend(BatchPaymentTransaction transaction,
                                  List<PendingPayment> payments) {
        if (eventListener != null) {
            eventListener.onTransactionSend(transaction, payments);
        }
    }

    @Override
    public void onTransactionSendSuccess(TransactionId transactionId,
                                         List<PendingPayment> payments) {
        if (eventListener != null) {
            eventListener.onTransactionSendSuccess(transactionId, payments);
        }
    }

    @Override
    public void onTransactionSendFailed(List<PendingPayment> payments, KinException exception) {
        if (eventListener != null) {
            eventListener.onTransactionSendFailed(payments, exception);
        }
    }



}
