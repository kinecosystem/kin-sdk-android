package kin.sdk.internal.services.helpers

import kin.sdk.internal.services.BlockchainEventsImpl

/**
 * Represents a listener to [BlockchainEventsImpl], that can be removed using [.remove].
 */
class ListenerRegistration(private val removeRunnable: Runnable) {

    /**
     * Remove and unregisters this listener.
     */
    fun remove() {
        removeRunnable.run()
    }
}
