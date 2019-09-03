package kin.sdk.internal.queue;

import java.math.BigDecimal;

import kin.sdk.queue.PendingPayment;

public class PendingPaymentImpl implements PendingPayment {

    private final String destinationPublicAddress;
    private final String sourcePublicAddress;
    private final BigDecimal amount;
    private final Object metadata;

    public PendingPaymentImpl(String destinationPublicAddress, String sourcePublicAddress, BigDecimal amount) {
        this(destinationPublicAddress, sourcePublicAddress, amount, null);
    }

    PendingPaymentImpl(String destinationPublicAddress, String sourcePublicAddress, BigDecimal amount,
                       Object metadata) {
        this.destinationPublicAddress = destinationPublicAddress;
        this.sourcePublicAddress = sourcePublicAddress;
        this.amount = amount;
        this.metadata = metadata;
    }

    @Override
    public String destinationPublicAddress() {
        return destinationPublicAddress;
    }

    @Override
    public String sourcePublicAddress() {
        return sourcePublicAddress;
    }

    @Override
    public BigDecimal amount() {
        return amount;
    }

    @Override
    public Object metadata() {
        return metadata;
    }
}
