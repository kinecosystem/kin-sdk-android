package kin.sdk;

import android.util.Log;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;


public class WhitelistServiceForTest {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient;

    WhitelistServiceForTest() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    String whitelistTransaction(WhitelistableTransaction whitelistableTransaction) throws Exception {
        RequestBody requestBody = RequestBody.create(JSON, toJson(whitelistableTransaction));
        okhttp3.Request request = new Request.Builder()
                .url(IntegConsts.URL_WHITELISTING_SERVICE)
                .post(requestBody)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        String whitelist;
        if (response.code() == 200 && response.body() != null) {
            whitelist = response.body().string();
        } else {
            String msg =
                    "whitelistTransaction error, error code = " + response.code() + " message = " + response.body();
            Log.d("test", msg);
            throw new Exception(msg);
        }
        return whitelist;
    }

    private String toJson(WhitelistableTransaction whitelistableTransaction) throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("envelop", whitelistableTransaction.transactionPayload());
        jo.put("network_id", whitelistableTransaction.networkPassphrase());
        return jo.toString();
    }

}
