package kin.sdk;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class KinOkHttpClientFactory {

    private static final String KIN_SDK_ANDROID_VERSION_HEADER = "kin-sdk-android-version";
    private static final int TRANSACTIONS_TIMEOUT = 30;

    private String versionName;

    public final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(TRANSACTIONS_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TRANSACTIONS_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TRANSACTIONS_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(new KinHeaderInterceptor())
            .addInterceptor(new RetryInterceptor())
            .build();

    public final OkHttpClient testClient = new OkHttpClient.Builder()
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

    private class RetryInterceptor implements Interceptor {

        private RetryStrategy retryStrategy = new ExponentialBackoffRetryStrategy();

        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());

            int attempt = 0;
            while (retryStrategy.shouldRetry(response, attempt++)) {
                response.close();

                Request newRequest = response.request()
                        .newBuilder()
                        .build();

                response = retryStrategy.retry(newRequest, attempt, chain::proceed);
            }

            return response;
        }
    }

    public interface RetryStrategy {

        boolean shouldRetry(Response response, int attempt);

        Response retry(Request newRequest, int attempt, RequestFunction requestFunction) throws IOException;
    }

    public interface RequestFunction {

        Response run(Request request) throws IOException;
    }

    public class ExponentialBackoffRetryStrategy implements RetryStrategy {

        private static final long BACKOFF_DELAY_MILLIS = 1000;
        private static final long BACK0FF_FACTOR = 2;
        private static final long RETRY_MAX_ATTEMPTS = 4;

        private final long delayMillis;
        private final long backoffFactor;
        private final long maxAttempts;

        ExponentialBackoffRetryStrategy() {
            this(BACKOFF_DELAY_MILLIS, BACK0FF_FACTOR, RETRY_MAX_ATTEMPTS);
        }

        ExponentialBackoffRetryStrategy(long delayMillis, long backoffFactor, long maxAttempts) {
            this.delayMillis = delayMillis;
            this.backoffFactor = backoffFactor;
            this.maxAttempts = maxAttempts;
        }

        public boolean shouldRetry(Response response, int attempt) {
            return response.code() != 200 && attempt < maxAttempts;
        }

        public Response retry(Request newRequest, int attempt, RequestFunction requestFunction) throws IOException {
            try {
                Thread.sleep(delayMillis * (attempt != 0 ? backoffFactor : 1) * attempt);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Logger.getLogger("").info("Retry Attempt " + attempt);
            return requestFunction.run(newRequest);
        }
    }

    public class NoRetryStrategy implements RetryStrategy {

        @Override
        public boolean shouldRetry(okhttp3.Response response, int attempt) {
            return false;
        }

        @Override
        public Response retry(Request newRequest, int attempt, RequestFunction requestFunction) throws IOException {
            throw new RuntimeException("No retry strategy set");
        }
    }
}
