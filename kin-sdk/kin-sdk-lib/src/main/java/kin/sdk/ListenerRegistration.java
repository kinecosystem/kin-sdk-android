package kin.sdk;

/**
 * Represents a listener to {@link BlockchainEvents}, that can be removed using {@link #remove()}.
 */
public class ListenerRegistration {

    private final Runnable removeRunnable;

    ListenerRegistration(Runnable removeRunnable) {
        this.removeRunnable = removeRunnable;
    }

    /**
     * Remove and unregisters this listener.
     */
    public void remove() {
        removeRunnable.run();
    }
}
