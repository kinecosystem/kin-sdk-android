package kin.sdk.internal.services.helpers;

import kin.sdk.internal.services.BlockchainEventsImpl;

/**
 * Represents a listener to {@link BlockchainEventsImpl}, that can be removed using {@link #remove()}.
 */
public class ListenerRegistration {

    private final Runnable removeRunnable;

    public ListenerRegistration(Runnable removeRunnable) {
        this.removeRunnable = removeRunnable;
    }

    /**
     * Remove and unregisters this listener.
     */
    public void remove() {
        removeRunnable.run();
    }
}
