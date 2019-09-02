package kin.sdk.queue;

import android.support.annotation.Nullable;

import java.util.List;

import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.internal.events.EventsManager;

abstract class BatchPaymentTransactionProcess extends TransactionProcess {

    BatchPaymentTransactionProcess(TransactionSender transactionSender,
                                   EventsManager eventsManager) {
        super(transactionSender, eventsManager);
    }

    /**
     * @return the list of pending payment that the current transaction consist of.
     * Can be null or empty.
     */
    @Nullable
    public abstract List<PendingPayment> payments();

}
