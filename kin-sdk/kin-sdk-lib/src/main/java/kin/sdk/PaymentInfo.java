package kin.sdk;


import java.math.BigDecimal;

/**
 * Represents payment issued on the blockchain.
 */
public interface PaymentInfo {

    /**
     * Transaction creation time.
     */
    String createdAt();

    // TODO: 2019-08-14 should we change namings??? instead of key use address?
    /**
     * Destination account public id.
     */
    String destinationPublicKey();

    /**
     * Source account public id.
     */
    String sourcePublicKey();

    /**
     * Payment amount in kin.
     */
    BigDecimal amount();

    /**
     * Transaction id (hash).
     */
    TransactionId hash();

    /**
     * An optional string, up-to 28 characters, included on the transaction record.
     */
    String memo();

    /**
     * Amount of fee(in Quarks for this payment (1 Quark = 0.00001 KIN).
     */
    long fee();
}
