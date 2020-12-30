package kin.base.requests;


import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.here.oksse.OkSse;
import com.here.oksse.ServerSentEvent;

import java.net.URI;

import kin.base.responses.GsonSingleton;
import okhttp3.Request;

public class StreamHandler<T> {

    private static final String OPEN_MESSAGE_DATA = "\"hello\""; //opening message contains "hello" string
    private TypeToken<T> type;
    private OkSse okSse = new OkSse();

    /**
     * "Generics on a type are typically erased at runtime, except when the type is compiled with the
     * generic parameter bound. In that case, the compiler inserts the generic type information into
     * the compiled class. In other cases, that is not possible."
     * More info: http://stackoverflow.com/a/14506181
     *
     * @param type
     */
    public StreamHandler(TypeToken<T> type) {
        this.type = type;
    }

    public ServerSentEvent handleStream(final URI uri, final EventListener<T> listener) {
        Request request = new Request.Builder()
                .url(uri.toString())
                .build();

        return okSse.newServerSentEvent(request, new ServerSentEvent.Listener() {
            @Override
            public void onOpen(ServerSentEvent sse, okhttp3.Response response) {
            }

            @Override
            public void onMessage(ServerSentEvent sse, String id, String event, String data) {
                if (OPEN_MESSAGE_DATA.equals(data)) {
                    return;
                }
                try {
                    T object = GsonSingleton.getInstance().fromJson(data, type.getType());
                    if (object != null) {
                        listener.onEvent(object);
                    }
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onComment(ServerSentEvent sse, String comment) {
            }

            @Override
            public boolean onRetryTime(ServerSentEvent sse, long milliseconds) {
                return true;
            }

            @Override
            public boolean onRetryError(ServerSentEvent sse, Throwable throwable, okhttp3.Response response) {
                return true;
            }

            @Override
            public void onClosed(ServerSentEvent sse) {
            }

            @Override
            public Request onPreRetry(ServerSentEvent sse, Request originalRequest) {
                return originalRequest;
            }
        });
    }


}
