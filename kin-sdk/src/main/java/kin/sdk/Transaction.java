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
    private final WhitelistableTransaction whitelistableTransaction;

    Transaction(KeyPair destination, KeyPair source, BigDecimal amount, int fee, String memo,
                TransactionId id, kin.base.Transaction stellarTransaction, WhitelistableTransaction whitelistableTransaction) {
        this.destination = destination;
        this.source = source;
        this.amount = amount;
        this.fee = fee;
        this.memo = memo;
        this.id = id;
        this.stellarTransaction = stellarTransaction;
        this.whitelistableTransaction = whitelistableTransaction;
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

    kin.base.Transaction getStellarTransaction() {
        return stellarTransaction;
    }

    public WhitelistableTransaction getWhitelistableTransaction() {
        return whitelistableTransaction;
    }
}
