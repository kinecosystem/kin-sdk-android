package kin.sdk;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class WhitelistServiceForTest {

    private static final String URL_WHITELISTING_SERVICE = "http://34.239.111.38:3000/whitelist";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient;

    WhitelistServiceForTest() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    String whitelistTransaction(WhitelistableTransaction whitelistableTransaction) throws JSONException, IOException {
        RequestBody requestBody = RequestBody.create(JSON, toJson(whitelistableTransaction));
        okhttp3.Request request = new Request.Builder()
                .url(URL_WHITELISTING_SERVICE)
                .post(requestBody)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        String whitelist = null;
        if (response.body() != null) {
            whitelist = response.body().string();
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
