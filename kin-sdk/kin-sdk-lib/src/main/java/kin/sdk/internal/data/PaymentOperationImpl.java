package kin.sdk.internal.data;

import kin.sdk.transactiondata.BatchPaymentTransaction;

import java.math.BigDecimal;

public class PaymentOperationImpl implements BatchPaymentTransaction.PaymentOperation {

    private final String destinationPublicKey;
    private final String sourcePublicKey;
    private final BigDecimal amount;

    public PaymentOperationImpl(String destinationPublicKey, String sourcePublicKey, BigDecimal amount) {
        // TODO: 2019-08-15 namings?
        this.destinationPublicKey = destinationPublicKey;
        this.sourcePublicKey = sourcePublicKey;
        this.amount = amount;
    }

    @Override
    public String destination() {
        return null;
    }

    @Override
    public String source() {
        return null;
    }

    @Override
    public BigDecimal amount() {
        return null;
    }
}
