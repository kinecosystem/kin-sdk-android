package kin.sdk.internal.data;

import java.math.BigDecimal;

import kin.sdk.transactiondata.BatchPaymentTransaction;

public class PaymentOperationImpl implements BatchPaymentTransaction.PaymentOperation {

    private final String destinationPublicAddress;
    private final String sourcePublicAddress;
    private final BigDecimal amount;

    public PaymentOperationImpl(String destinationPublicAddress, String sourcePublicAddress, BigDecimal amount) {
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
