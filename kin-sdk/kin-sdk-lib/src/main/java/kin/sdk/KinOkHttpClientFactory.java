package kin.sdk;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class KinOkHttpClientFactory {

    private static final String KIN_SDK_ANDROID_VERSION_HEADER = "kin-sdk-android-version";
    private static final int TRANSACTIONS_TIMEOUT = 30;

    private String versionName;

    public final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(TRANSACTIONS_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TRANSACTIONS_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TRANSACTIONS_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(new KinHeaderInterceptor())
            .build();

    public KinOkHttpClientFactory(String versionName) {
        this.versionName = versionName;
    }

    private class KinHeaderInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            request = request.newBuilder()
                    .addHeader(KIN_SDK_ANDROID_VERSION_HEADER, versionName)
                    .build();
            return chain.proceed(request);
        }
    }
}
