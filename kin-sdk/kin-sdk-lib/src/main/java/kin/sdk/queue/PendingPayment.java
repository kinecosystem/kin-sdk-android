package kin.sdk.queue;

import android.support.annotation.Nullable;
import kin.sdk.transaction_data.BatchPaymentTransaction;

import java.math.BigDecimal;

public interface PendingPayment {

    enum Status {
        PENDING, COMPLETED, FAILED
    }

    String destinationPublicKey();

    String sourcePublicKey();

    BigDecimal amount();

    /**
     * @return the operation index in the pending batch payment transaction
     */
    int operationIndex();

    /**
     * Note - the payment transaction will be available only after sending the pending transaction.
     *
     * @return the transaction
     */
    // TODO: 2019-08-04 why the hell do we have this method?
    @Nullable
    BatchPaymentTransaction transaction();

    /**
     * @return the Payment key-value metadata if exist or null otherwise.
     * <p>For more details see {@link PaymentQueue#enqueuePayment(String, BigDecimal, Object)}</p>
     */
    @Nullable
    Object metadata();

    Status status();

}
