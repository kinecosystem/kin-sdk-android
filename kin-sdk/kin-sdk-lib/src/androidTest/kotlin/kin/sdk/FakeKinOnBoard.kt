package kin.sdk

import android.util.Log
import kin.sdk.IntegConsts.URL_CREATE_ACCOUNT
import kin.sdk.IntegConsts.URL_FUND
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Fake on board for integration test, support creating and funding accounts on stellar test net
 */
internal class FakeKinOnBoard @Throws(IOException::class)
constructor() {

    private val client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    @Throws(Exception::class)
    fun createAccount(destinationAccount: String, amount: Int = 0) {
        val request = Request.Builder()
                .url(String.format(URL_CREATE_ACCOUNT, destinationAccount, amount)).build()
        client.newCall(request).execute()?.let {
            if (it.body() == null || it.code() != 200) {
                Log.d("test", "createAccount error, error code = ${it.code()}, message = ${it.body()}")
                throw Exception("Cannot create account using the friend bot! error code = ${it.code()}, message = ${it.body()}")
            }
        }
    }

    fun fundWithKin(destinationAccount: String, amount: String) {
        val request = Request.Builder()
                .url(String.format(URL_FUND + amount, destinationAccount))
                .get()
                .build()
        client.newCall(request).execute()?.let {
            if (it.body() == null || it.code() != 200) {
                Log.d("test", "fundWithKin error, error code = ${it.code()}, message = ${it.message()}")
            }
        }

    }
}

