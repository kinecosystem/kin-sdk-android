package kin.sdk;

import java.math.BigDecimal;

public class PaymentTransaction extends TransactionBase {

    private final String destination;
    private final BigDecimal amount;
    private final String memo;

    public PaymentTransaction(kin.base.Transaction baseTransaction, String destination,
        BigDecimal amount, String memo) {

        super(baseTransaction);
        this.destination = destination;
        this.amount = amount;
        this.memo = memo;
    }

    public String destination() {
        return destination;
    }

    public BigDecimal amount() {
        return amount;
    }

    public String memo() {
        return memo;
    }

}
