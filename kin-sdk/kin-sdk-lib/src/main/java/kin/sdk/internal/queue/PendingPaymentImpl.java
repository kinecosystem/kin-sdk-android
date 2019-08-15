package kin.sdk.internal.queue;

import kin.sdk.queue.PendingPayment;

import java.math.BigDecimal;

public class PendingPaymentImpl implements PendingPayment {

    private final String destinationPublicKey;
    private final String sourcePublicKey;
    private final BigDecimal amount;
    private final Object metadata;
    private Status status;

    public PendingPaymentImpl(String destinationPublicKey, String sourcePublicKey, BigDecimal amount) {
        this(destinationPublicKey, sourcePublicKey, amount, null);
    }

    PendingPaymentImpl(String destinationPublicKey, String sourcePublicKey, BigDecimal amount, Object metadata) {
        this.destinationPublicKey = destinationPublicKey;
        this.sourcePublicKey = sourcePublicKey;
        this.amount = amount;
        this.metadata = metadata;
        this.status = Status.PENDING;
    }

    @Override
    public String destinationPublicKey() {
        return destinationPublicKey;
    }

    @Override
    public String sourcePublicKey() {
        return sourcePublicKey;
    }

    @Override
    public BigDecimal amount() {
        return amount;
    }

    @Override
    public Object metadata() {
        return metadata;
    }

    @Override
    public Status status() {
        return status;
    }

    void setStatus(Status status) {
        this.status = status;
    }
}
