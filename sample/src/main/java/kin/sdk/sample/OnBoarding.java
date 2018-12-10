package kin.sdk.sample;


import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import kin.sdk.KinAccount;
import kin.sdk.ListenerRegistration;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class OnBoarding {

    private static final String URL_CREATE_ACCOUNT = "http://18.206.35.110:8001?addr=";
    private static final int FUND_KIN_AMOUNT = 6000;
    private static final String URL_FUND =
        "http://18.206.35.110:3000/fund?account=%s&amount=" + String.valueOf(FUND_KIN_AMOUNT);
    private final OkHttpClient okHttpClient;
    private final Handler handler;
    private ListenerRegistration listenerRegistration;

    public interface Callbacks {

        void onSuccess();
        void onFailure(Exception e);

    }

    OnBoarding() {
        handler = new Handler(Looper.getMainLooper());
        okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();
    }

    void onBoard(@NonNull KinAccount account, @NonNull Callbacks callbacks) {
        Runnable accountCreationListeningTimeout = () -> {
            listenerRegistration.remove();
            fireOnFailure(callbacks, new TimeoutException("Waiting for account creation event time out"));
        };
        listenerRegistration = account.addAccountCreationListener(data -> {
            listenerRegistration.remove();
            handler.removeCallbacks(accountCreationListeningTimeout);
            fundAccountWithKin(account, callbacks);
        });
        createAccount(account, callbacks);
    }

    private void createAccount(@NonNull KinAccount account, @NonNull Callbacks callbacks) {
        Request request = new Request.Builder()
            .url(URL_CREATE_ACCOUNT + account.getPublicAddress())
            .get()
            .build();
        okHttpClient.newCall(request)
            .enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    fireOnFailure(callbacks, e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    int code = response.code();
                    response.close();
                    if (code != 200) {
                        fireOnFailure(callbacks, new Exception("Create account - response code is " + response.code()));
                    }
                }
            });
    }

    private void fundAccountWithKin(KinAccount account, @NonNull Callbacks callbacks) {
        Request request = new Request.Builder()
            .url(String.format(URL_FUND, account.getPublicAddress()))
            .get()
            .build();
        okHttpClient.newCall(request)
            .enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    fireOnFailure(callbacks, e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    int code = response.code();
                    response.close();
                    if (code == 200) {
                        //will trigger a call to get updated balance
                        fireOnSuccess(callbacks);
                    } else {
                        fireOnFailure(callbacks, new Exception("Fund account - response code is " + response.code()));
                    }
                }
            });
    }

    private void fireOnFailure(@NonNull Callbacks callbacks, Exception ex) {
        handler.post(() -> callbacks.onFailure(ex));
    }

    private void fireOnSuccess(@NonNull Callbacks callbacks) {
        handler.post(callbacks::onSuccess);
    }
}