package kin.sdk.internal.services;


import android.support.annotation.NonNull;

import kin.sdk.internal.services.helpers.EventListener;
import kin.sdk.internal.services.helpers.ListenerRegistration;
import kin.sdk.models.Balance;
import kin.sdk.models.PaymentInfo;

public interface BlockchainEvents {

    /**
     * Creates and adds listener for balance changes of this account, use returned {@link ListenerRegistration} to
     * stop listening. <p><b>Note:</b> Events will be fired on background thread.</p>
     *
     * @param listener listener object for payment events
     */
    ListenerRegistration addBalanceListener(@NonNull EventListener<Balance> listener);

    /**
     * Creates and adds listener for payments concerning this account, use returned {@link ListenerRegistration} to
     * stop listening. <p><b>Note:</b> Events will be fired on background thread.</p>
     *
     * @param listener listener object for payment events
     */
    ListenerRegistration addPaymentListener(@NonNull EventListener<PaymentInfo> listener);

    /**
     * Creates and adds listener for account creation event, use returned {@link ListenerRegistration} to stop
     * listening. <p><b>Note:</b> Events will be fired on background thread.</p>
     *
     * @param listener listener object for payment events
     */
    ListenerRegistration addAccountCreationListener(EventListener<Void> listener);
}
