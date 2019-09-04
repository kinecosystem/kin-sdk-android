package kin.sdk.transactiondata;

import java.math.BigDecimal;

import kin.sdk.internal.blockchain.TransactionInternal;

public class PaymentTransaction extends TransactionInternal {

    private final String destinationPublicAddress;
    private final BigDecimal amount;
    private final String memo;

    public PaymentTransaction(kin.base.Transaction baseTransaction,
                              String destinationPublicAddress, BigDecimal amount,
                              String memo) {
        super(baseTransaction);
        this.destinationPublicAddress = destinationPublicAddress;
        this.amount = amount;
        this.memo = memo;
    }

    /**
     * @return the destination account public address that should receives the payment.
     */
    public String destination() {
        return destinationPublicAddress;
    }

    /**
     * @return the amount of kin that was sent in this transaction.
     */
    public BigDecimal amount() {
        return amount;
    }

    /**
     * @return the memo of this transaction.
     * In this case you will get a string representation of a memo as opposed to {@link RawTransaction#memo()}
     */
    public String memo() {
        return memo;
    }

}
