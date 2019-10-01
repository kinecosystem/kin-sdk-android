package kin.sdk;

import com.here.oksse.ServerSentEvent;
import java.util.ArrayList;
import java.util.List;
import kin.base.requests.EventListener;
import kin.base.requests.RequestBuilder;


class ManagedServerSentEventStream<ResponseType> {

    private final Object lock = new Object();

    private final RequestBuilder requestBuilder;

    private final List<EventListener<ResponseType>> listeners = new ArrayList<>();

    private ServerSentEvent connection;

    private ResponseType lastReceivedResponse;

    ManagedServerSentEventStream(RequestBuilder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    void addListener(EventListener<ResponseType> listener) {
        synchronized (lock) {
            listeners.add(listener);

            if (lastReceivedResponse != null) {
                listener.onEvent(lastReceivedResponse);
            }

            connectIfNecessary();
        }
    }

    void removeListener(EventListener<ResponseType> listener) {
        synchronized (lock) {
            listeners.remove(listener);

            closeIfNecessary();
        }
    }

    /* Starts a connection if not already; criteria for connection is at least one active listener. */
    private void connectIfNecessary() {
        synchronized (lock) {
            if (connection != null) {
                return;
            }

            if (!listeners.isEmpty()) {
                connection = requestBuilder.stream(new EventListener<ResponseType>() {
                    @Override
                    public void onEvent(ResponseType object) {
                        lastReceivedResponse = object;
                        synchronized (lock) {
                            for (int i = 0; i < listeners.size(); i++) {
                                EventListener<ResponseType> listener = listeners.get(i);
                                listener.onEvent(object);
                            }
                        }
                    }
                });
            }
        }
    }

    private void closeIfNecessary() {
        synchronized (lock) {
            if (connection == null) {
                return;
            }

            if (!listeners.isEmpty()) {
                return;
            }

            connection.close();
            connection = null;
        }
    }

}
