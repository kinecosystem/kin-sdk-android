package kin.sdk;


import java.math.BigDecimal;

class PaymentInfoImpl implements PaymentInfo {

    private final String createdAt;
    private final String destinationPublicKey;
    private final String sourcePublicKey;
    private final BigDecimal amount;
    private final TransactionId hash;
    private final long fee;
    private final String memo;

    PaymentInfoImpl(String createdAt, String destinationPublicKey, String sourcePublicKey, BigDecimal amount,
        TransactionId hash, long fee, String memo) {
        this.createdAt = createdAt;
        this.destinationPublicKey = destinationPublicKey;
        this.sourcePublicKey = sourcePublicKey;
        this.amount = amount;
        this.hash = hash;
        this.fee = fee;
        this.memo = memo;
    }

    @Override
    public String createdAt() {
        return createdAt;
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
    public TransactionId hash() {
        return hash;
    }

    @Override
    public String memo() {
        return memo;
    }

    @Override
    public long fee() {
        return fee;
    }
}
