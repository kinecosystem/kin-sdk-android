package kin.sdk.queue;

import android.support.annotation.Nullable;

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
     * @return the Payment key-value metadata if exist or null otherwise.
     * <p>For more details see {@link PaymentQueue#enqueuePayment(String, BigDecimal, Object)}</p>
     */
    @Nullable
    Object metadata();

    Status status();

}
