package sdk.sample;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import kin.sdk.WhitelistPayload;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class WhitelistService {

    private static final String URL_WHITELISTING_SERVICE = "http://34.239.111.38:3000/whitelist";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient;
    private final Handler handler;

    WhitelistService() {
        handler = new Handler(Looper.getMainLooper());
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    void whitelistTransaction(WhitelistPayload whitelistPayload,
                              TransactionActivity.WhitelistServiceListener whitelistServiceListener) throws JSONException {
        RequestBody requestBody = RequestBody.create(JSON, toJson(whitelistPayload));
        Request request = new Request.Builder()
                .url(URL_WHITELISTING_SERVICE)
                .post(requestBody)
                .build();
        okHttpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        if (whitelistServiceListener != null) {
                            fireOnFailure(whitelistServiceListener, e);
                        }
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        handleResponse(response, whitelistServiceListener);
                    }
                });
    }

    private void handleResponse(@NonNull Response response, TransactionActivity.WhitelistServiceListener whitelistServiceListener) throws IOException {
        if (whitelistServiceListener != null) {
            if (response.body() != null) {
                fireOnSuccess(whitelistServiceListener, response.body().string());
            } else {
                fireOnFailure(whitelistServiceListener, new Exception("Whitelist - no body, response code is " + response.code()));
            }
        }
        int code = response.code();
        response.close();
        if (code != 200) {
            fireOnFailure(whitelistServiceListener, new Exception("Whitelist - response code is " + response.code()));
        }
    }

    private String toJson(WhitelistPayload whitelistPayload) throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("envelope", whitelistPayload.getTransactionPayload());
        jo.put("network_id", whitelistPayload.getNetworkPassphrase());
        return jo.toString();
    }


    private void fireOnFailure(TransactionActivity.WhitelistServiceListener whitelistServiceListener, Exception e) {
        handler.post(() -> whitelistServiceListener.onFailure(e));
    }

    private void fireOnSuccess(TransactionActivity.WhitelistServiceListener whitelistServiceListener, String response) {
        handler.post(() -> whitelistServiceListener.onSuccess(response));
    }
}




