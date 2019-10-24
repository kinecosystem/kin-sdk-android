package kin.sdk.internal

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class KinOkHttpClientFactory(versionName: String) {

    private companion object {
        const val TRANSACTIONS_TIMEOUT = 30
    }

    val client = OkHttpClient.Builder()
        .connectTimeout(TRANSACTIONS_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .writeTimeout(TRANSACTIONS_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .readTimeout(TRANSACTIONS_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .addInterceptor(KinHeaderInterceptor(versionName))
        .addInterceptor(RetryInterceptor())
        .build()

    val testClient = OkHttpClient.Builder()
        .connectTimeout(TRANSACTIONS_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .writeTimeout(TRANSACTIONS_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .readTimeout(TRANSACTIONS_TIMEOUT.toLong(), TimeUnit.SECONDS)
        .addInterceptor(KinHeaderInterceptor(versionName))
        .build()
}

private class KinHeaderInterceptor(private val versionName: String) : Interceptor {

    private companion object {
        const val KIN_SDK_ANDROID_VERSION_HEADER = "kin-sdk-android-version"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder()
            .addHeader(KIN_SDK_ANDROID_VERSION_HEADER, versionName)
            .build()
        return chain.proceed(request)
    }
}

private class RetryInterceptor : Interceptor {

    private val retryStrategy = ExponentialBackoffRetryStrategy()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var response = chain.proceed(chain.request())

        var attempt = 0
        while (retryStrategy.shouldRetry(response, attempt++)) {
            response.close()

            val newRequest = response.request()
                .newBuilder()
                .build()

            response = retryStrategy.retry(newRequest, attempt, object : RequestFunction {
                override fun run(request: Request): Response {
                    return chain.proceed(request)
                }
            })
        }

        return response
    }
}

private interface RetryStrategy {

    fun shouldRetry(response: Response, attempt: Int): Boolean

    @Throws(IOException::class)
    fun retry(newRequest: Request, attempt: Int, requestFunction: RequestFunction): Response
}

private interface RequestFunction {

    @Throws(IOException::class)
    fun run(request: Request): Response
}

private class ExponentialBackoffRetryStrategy @JvmOverloads internal constructor(private val delayMillis: Long = BACKOFF_DELAY_MILLIS, private val backoffFactor: Long = BACK0FF_FACTOR, private val maxAttempts: Long = RETRY_MAX_ATTEMPTS) : RetryStrategy {

    private companion object {
        const val BACKOFF_DELAY_MILLIS: Long = 1000
        const val BACK0FF_FACTOR: Long = 2
        const val RETRY_MAX_ATTEMPTS: Long = 4
    }

    override fun shouldRetry(response: Response, attempt: Int): Boolean {
        return response.code() != 200 && attempt < maxAttempts
    }

    @Throws(IOException::class)
    override fun retry(newRequest: Request, attempt: Int, requestFunction: RequestFunction): Response {
        try {
            Thread.sleep(delayMillis * (if (attempt != 0) backoffFactor else 1) * attempt.toLong())
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }

        Logger.getLogger("").info("Retry Attempt $attempt")
        return requestFunction.run(newRequest)
    }
}
