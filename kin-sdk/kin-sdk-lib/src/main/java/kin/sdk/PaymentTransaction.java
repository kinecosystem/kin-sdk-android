package kin.sdk;

import java.math.BigDecimal;

public class PaymentTransaction extends TransactionBase {

    private final String destination;
    private final BigDecimal amount;
    private final String memo;

    PaymentTransaction(kin.base.Transaction baseTransaction, String destination, BigDecimal amount, String memo) {
        super(baseTransaction);
        this.destination = destination;
        this.amount = amount;
        this.memo = memo;
    }

    /**
     * @return the destination of this transaction.
     */
    public String destination() {
        return destination;
    }

    /**
     * @return the amount of this transaction.
     */
    public BigDecimal amount() {
        return amount;
    }

    /**
     * @return the amount of this transaction.
     */
    public String memo() {
        return memo;
    }

}
