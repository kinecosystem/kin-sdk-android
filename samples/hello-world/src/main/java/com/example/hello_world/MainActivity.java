package com.example.hello_world;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.util.Log;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import kin.sdk.Balance;
import kin.sdk.Environment;
import kin.sdk.KinAccount;
import kin.sdk.KinClient;
import kin.sdk.Transaction;
import kin.sdk.TransactionId;
import kin.utils.ResultCallback;
import kin.sdk.exception.CreateAccountException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SUM_TO_TRANSFER = 5;
    private static final int FUND_KIN_AMOUNT = 10000;
    private static final String TARGET_WALLET = "GDBHSCMB3G7ILK4ADWJS6GK6IOZGYOWSMHE33BWBXLZTVEMVYSCKJE5Z";
    private static final String URL_CREATE_ACCOUNT = "http://friendbot-testnet.kininfrastructure.com?addr=%s&amount=" + String.valueOf(FUND_KIN_AMOUNT);
    private static final String STUB_APP_ID = "nfs1";
    private static final int FEE = 100;
    private static final String MEMO = "arbitrary data";
    private static final int PRECISION = 2;
    private static final int APP_INDEX = 0;
    private static final BigDecimal AMOUNT_KIN =  new BigDecimal(SUM_TO_TRANSFER);
    private OkHttpClient okHttpClient;
    private KinClient kinClient;
    private KinAccount account;

    public interface Callbacks {
        void onSuccess();
        void onFailure(Exception e);
    }

    private void fireOnFailure(@NonNull Callbacks callbacks, Exception ex) {
        callbacks.onFailure(ex);
    }

    private void fireOnSuccess(@NonNull Callbacks callbacks) {
        callbacks.onSuccess();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        // Kin client is the manager for Kin accounts
        kinClient = new KinClient(this, Environment.TEST, STUB_APP_ID);

        // Kin account the is the entity that holds Kins
        // The accounts are stored in the kinClient and accessed with incremental index
        account = getKinAccount(APP_INDEX);

        // Listener for balance changes
        addBalanceListeners(account);

        // Async function to get kin balance
        getKinBalance(account);

        // Create the wallet with the Kin account keypair
        // As it is an async request, at the callback we will be able to transfer Kins and check the account balance
        onBoardAccount(account, new Callbacks() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Onboarding succeeded");
                transferKin(account, TARGET_WALLET, AMOUNT_KIN);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Onboarding failed");
            }
        });
    }

    public KinAccount getKinAccount(int index) {
        // The index that is used to get a specific account from the client manager
        KinAccount kinAccount = kinClient.getAccount(index);
        try {
            // Creates a local keypair
            if (kinAccount == null) {
                kinAccount = kinClient.addAccount();
                Log.d(TAG, "Created new account succeeded");
            }
        } catch (CreateAccountException e) {
            e.printStackTrace();
        }

        return kinAccount;
    }

    private void onBoardAccount(@NonNull KinAccount account, @NonNull Callbacks callbacks) {
        // Creating the kin account for a specific user
        if (account != null) {
            Request request = new Request.Builder()
                    .url(String.format(URL_CREATE_ACCOUNT, account.getPublicAddress()))
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
                            final int code = response.code();
                            response.close();
                            if (code != 200) {
                                fireOnFailure(callbacks,
                                        new Exception("Create account - response code is " + code));
                                // Continue execution after this failure, it may occur due to "op_already_exists",
                                // i.e, account creation failed because it already exists.
                            }

                            fireOnSuccess(callbacks);
                        }
                    });
        }
    }

    public void getKinBalance(KinAccount account) {
        account.getBalance().run(new ResultCallback<Balance>() {

            @Override
            public void onResult(Balance result) {
                Log.d(TAG, "The balance is: " + result.value(PRECISION));
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void transferKin(KinAccount sender, String targetPublicAddress, BigDecimal amountInKin) {

        // Build the transaction request and run the request asynchronously:
        // The sender is a kin account that transfers Kins to the target public address
        // Each transaction will be charged a fee
        // Memo will state the transaction's reason
        sender.buildTransaction(targetPublicAddress, amountInKin, FEE, MEMO).run(new ResultCallback<Transaction>() {

            @Override
            public void onResult(Transaction transaction) {
                Log.d(TAG, "The transaction amount in Kins: " + transaction.getAmount());

                sender.sendTransaction(transaction).run(new ResultCallback<TransactionId>() {

                    @Override
                    public void onResult(TransactionId id) {
                        Log.d(TAG, "The transaction id: " + transaction.getId().id());
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }

        });
    }

    public void addBalanceListeners(KinAccount account) {
        account.addBalanceListener(
                balance -> Log.d(TAG, "balance event, new balance is = " + balance.value().toPlainString()));
    }

}
