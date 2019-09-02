package kin.sdk.queue;

import java.util.List;

import kin.base.KeyPair;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.internal.events.EventsManager;
import kin.sdk.transactiondata.Transaction;

public class BatchPaymentTransactionProcessImpl extends BatchPaymentTransactionProcess {

    private final TransactionSender transactionSender;
    private final List<PendingPayment> pendingPayments;
    private final KeyPair accountFrom;
    private final int fee;

    public BatchPaymentTransactionProcessImpl(TransactionSender transactionSender,
                                              List<PendingPayment> pendingPayments,
                                              KeyPair accountFrom,
                                              int fee, EventsManager eventsManager) {
        super(transactionSender, eventsManager);
        this.transactionSender = transactionSender;
        this.pendingPayments = pendingPayments;
        this.accountFrom = accountFrom;
        this.fee = fee;
    }

    @Override
    public List<PendingPayment> payments() {
        return pendingPayments;
    }

    @Override
    protected Transaction buildTransaction(String memo) throws OperationFailedException {
        return transactionSender.buildBatchPaymentTransaction(accountFrom, pendingPayments, fee,
                memo);
    }
}
