package kin.sdk.internal.queue;

import kin.sdk.TransactionId;
import kin.sdk.queue.PendingPayment;
import kin.sdk.queue.TransactionProcess;
import kin.sdk.transactiondata.Transaction;

import java.util.List;

public class TransactionProcessImpl implements TransactionProcess {

    // TODO: 2019-08-04 implement

    @Override
    public Transaction transaction() {
        return null;
    }

    @Override
    public Transaction transaction(String memo) {
        return null;
    }

    @Override
    public List<PendingPayment> payments() {
        return null;
    }

    @Override
    public TransactionId send(Transaction transaction) {
        return null;
    }

    @Override
    public TransactionId send(String whitelistPayload) {
        return null;
    }
}
