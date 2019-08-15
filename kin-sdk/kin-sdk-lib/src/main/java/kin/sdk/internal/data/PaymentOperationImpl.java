package kin.sdk.internal.data;

import kin.sdk.transactiondata.BatchPaymentTransaction;

import java.math.BigDecimal;

public class PaymentOperationImpl implements BatchPaymentTransaction.PaymentOperation {

    private final String destinationPublicAddress;
    private final String sourcePublicAddress;
    private final BigDecimal amount;

    public PaymentOperationImpl(String destinationPublicAddress, String sourcePublicAddress, BigDecimal amount) {
        // TODO: 2019-08-15 namings?
        this.destinationPublicAddress = destinationPublicAddress;
        this.sourcePublicAddress = sourcePublicAddress;
        this.amount = amount;
    }

    @Override
    public String destinationPublicAddress() {
        return null;
    }

    @Override
    public String sourcePublicAddress() {
        return null;
    }

    @Override
    public BigDecimal amount() {
        return null;
    }
}
