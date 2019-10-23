package kin.sdk.models;


import java.math.BigDecimal;

public final class PaymentInfo {

    private final String createdAt;
    private final String destinationPublicKey;
    private final String sourcePublicKey;
    private final BigDecimal amount;
    private final TransactionId hash;
    private final long fee;
    private final String memo;

    public PaymentInfo(String createdAt, String destinationPublicKey, String sourcePublicKey, BigDecimal amount,
                       TransactionId hash, long fee, String memo) {
        this.createdAt = createdAt;
        this.destinationPublicKey = destinationPublicKey;
        this.sourcePublicKey = sourcePublicKey;
        this.amount = amount;
        this.hash = hash;
        this.fee = fee;
        this.memo = memo;
    }

    public String createdAt() {
        return createdAt;
    }

    public String destinationPublicKey() {
        return destinationPublicKey;
    }

    public String sourcePublicKey() {
        return sourcePublicKey;
    }

    public BigDecimal amount() {
        return amount;
    }

    public TransactionId hash() {
        return hash;
    }

    public String memo() {
        return memo;
    }

    public long fee() {
        return fee;
    }
}
