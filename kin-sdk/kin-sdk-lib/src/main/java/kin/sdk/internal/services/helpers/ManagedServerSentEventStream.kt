package kin.sdk.internal.services.helpers

import com.here.oksse.ServerSentEvent
import kin.base.requests.EventListener
import kin.base.requests.RequestBuilder
import java.util.ArrayList

internal class ManagedServerSentEventStream<ResponseType>(private val requestBuilder: RequestBuilder) {

    private val lock = Any()
    private val listeners = ArrayList<EventListener<ResponseType>>()
    private var connection: ServerSentEvent? = null
    private var lastReceivedResponse: ResponseType? = null

    fun addListener(listener: EventListener<ResponseType>) {
        synchronized(lock) {
            listeners.add(listener)
            lastReceivedResponse?.let { listener.onEvent(lastReceivedResponse) }
            connectIfNecessary()
        }
    }

    fun removeListener(listener: EventListener<ResponseType>) {
        synchronized(lock) {
            listeners.remove(listener)
            closeIfNecessary()
        }
    }

    /* Starts a connection if not already; criteria for connection is at least one active listener. */
    private fun connectIfNecessary() {
        synchronized(lock) {
            if (connection != null) {
                return
            }

            if (listeners.isNotEmpty()) {
                connection = requestBuilder.stream(EventListener<ResponseType> { obj ->
                    lastReceivedResponse = obj
                    synchronized(lock) {
                        listeners.indices
                            .asSequence()
                            .map { listeners[it] }
                            .forEach { it.onEvent(obj) }
                    }
                })
            }
        }
    }

    private fun closeIfNecessary() {
        synchronized(lock) {
            if (connection == null) {
                return
            }

            if (listeners.isNotEmpty()) {
                return
            }

            connection!!.close()
            connection = null
        }
    }
}
