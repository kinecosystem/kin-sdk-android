package kin.sdk.transactiondata;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

public class TransactionParams {

    // TODO: 2019-08-06 implement

    static PaymentTransactionParams.Builder paymentTransactionBuilder(@NonNull String publicAddress,
                                                                      @NonNull BigDecimal amount,
                                                                      int fee) {
        return null;
    }

    public int fee() {
        return 0;
    }

}
