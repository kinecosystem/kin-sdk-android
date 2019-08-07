package kin.sdk.internal.queue;

import kin.sdk.queue.PendingPayment;

import java.math.BigDecimal;

public class PendingPaymentImpl implements PendingPayment {

    // TODO: 2019-08-04 implement

    @Override
    public String destinationPublicKey() {
        return null;
    }

    @Override
    public String sourcePublicKey() {
        return null;
    }

    @Override
    public BigDecimal amount() {
        return null;
    }

    @Override
    public int operationIndex() {
        return 0;
    }

    @Override
    public Object metadata() {
        return null;
    }

    @Override
    public Status status() {
        return null;
    }
}
