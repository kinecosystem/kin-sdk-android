package kin.sdk;

import kin.base.KeyPair;

import java.math.BigDecimal;

public class Transaction {

    private final KeyPair destination;
    private final KeyPair source;
    private final BigDecimal amount;
    private final int fee;
    private final String memo;

    /**
     * The transaction hash
     */
    private final TransactionId id;

    private final kin.base.Transaction stellarTransaction;
    private final WhitelistPayload whitelistPayload;

    public Transaction(KeyPair destination, KeyPair source, BigDecimal amount, int fee, String memo,
                       TransactionId id, kin.base.Transaction stellarTransaction, WhitelistPayload whitelistPayload) {
        this.destination = destination;
        this.source = source;
        this.amount = amount;
        this.fee = fee;
        this.memo = memo;
        this.id = id;
        this.stellarTransaction = stellarTransaction;
        this.whitelistPayload = whitelistPayload;
    }

    public KeyPair getDestination() {
        return destination;
    }

    public KeyPair getSource() {
        return source;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public int getFee() {
        return fee;
    }

    public String getMemo() {
        return memo;
    }

    public TransactionId getId() {
        return id;
    }

    public kin.base.Transaction getStellarTransaction() {
        return stellarTransaction;
    }

    public WhitelistPayload getWhitelistableTransaction() {
        return whitelistPayload;
    }
}
