package kin.sdk.transactiondata;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

public class PaymentTransactionParams extends TransactionParams {

    // TODO: 2019-08-21 implement

    private PaymentTransactionParams() {
    }

    public String destinationPublicAddress() {
        return null;
    }

    public BigDecimal amount() {
        return null;
    }

    public String memo() {
        return null;
    }

    public class Builder {

        Builder(@NonNull String publicAddress, @NonNull BigDecimal amount, int fee) {
        }

        public Builder memo(String memo) {
            return null;
        }

        public PaymentTransactionParams build() {
            return null;
        }
    }

}
