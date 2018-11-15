package kin.sdk.sample;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import kin.sdk.WhitelistableTransaction;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WhitelistService {

    private static final String URL_WHITELISTING_SERVICE = "http://18.206.35.110:3000/whitelist";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient;
    private final Handler handler;

    public WhitelistService() {
        handler = new Handler(Looper.getMainLooper());
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    public void whitelistTransaction(WhitelistableTransaction whitelistableTransaction,
                                     TransactionActivity.WhitelistServiceListener whitelistServiceListener) throws JSONException {
        RequestBody requestBody = RequestBody.create(JSON, toJson(whitelistableTransaction));
        Request request = new Request.Builder()
                .url(URL_WHITELISTING_SERVICE)
                .post(requestBody)
                .build();
        okHttpClient.newCall(request) // TODO: 15/11/2018 We could do it also synchronously using 'execute' and maybe it is the correct way in this case
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        if (whitelistServiceListener != null) {
                            fireOnFailure(whitelistServiceListener, e);
                        }
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (whitelistServiceListener != null) {
                            fireOnSuccess(whitelistServiceListener, response.body().string());// TODO: 15/11/2018 handel null exception
                        }
                        int code = response.code();
                        response.close();
                        if (code != 200) {
                            // fireOnFailure(callbacks, new Exception("Create account - response code is " + response.code()));
                        }
                    }
                });

    }

    private String toJson(WhitelistableTransaction whitelistableTransaction) throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("envelop", whitelistableTransaction.getTransactionEnvelopXdrBase64());
        jo.put("network_id", whitelistableTransaction.getNetworkPassphrase());
        return jo.toString();
    }


    private void fireOnFailure(TransactionActivity.WhitelistServiceListener whitelistServiceListener, IOException e) {
        handler.post(() -> whitelistServiceListener.onFailure(e));
    }

    private void fireOnSuccess(TransactionActivity.WhitelistServiceListener whitelistServiceListener, String response) {
        handler.post(() -> whitelistServiceListener.onSuccess(response));
    }
}




