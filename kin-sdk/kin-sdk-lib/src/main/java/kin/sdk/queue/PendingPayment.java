package kin.sdk.queue;

import android.support.annotation.Nullable;

import java.math.BigDecimal;

public interface PendingPayment {

    enum Status {
        PENDING, COMPLETED, FAILED
    }

    // TODO: 2019-08-14 namings???

    /**
     * Destination account public address.
     */
    String destinationPublicAddress();

    /**
     * Source account public address.
     */
    String sourcePublicAddress();

    /**
     * Pending Payment amount in kin.
     */
    BigDecimal amount();

    /**
     * @return the Payment key-value metadata if exist or null otherwise.
     * <p>For more details see {@link PaymentQueue#enqueuePayment(String, BigDecimal, Object)}</p>
     */
    @Nullable
    Object metadata();

    /**
     * Current Payment status
     */
    Status status();
}
