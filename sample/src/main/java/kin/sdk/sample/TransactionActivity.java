package kin.sdk.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import java.math.BigDecimal;
import kin.sdk.KinAccount;
import kin.sdk.Request;
import kin.sdk.ResultCallback;
import kin.sdk.Transaction;
import kin.sdk.TransactionId;
import kin.sdk.exception.AccountDeletedException;
import kin.sdk.exception.OperationFailedException;

/**
 * Displays form to enter public address and amount and a button to send a transaction
 */
public class TransactionActivity extends BaseActivity {

    public static final String TAG = TransactionActivity.class.getSimpleName();

    public static Intent getIntent(Context context) {
        return new Intent(context, TransactionActivity.class);
    }

    private View sendTransaction, retrieveMinimumFee, progressBar;

    private EditText toAddressInput, amountInput, feeInput, memoInput;
    private Request<Long> gertMinimumFeeRequest;
    private Request<Transaction> buildTransactionRequest;
    private Request<TransactionId> sendTransactionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_activity);
        initWidgets();
    }

    private void initWidgets() {
        sendTransaction = findViewById(R.id.send_transaction_btn);
        retrieveMinimumFee = findViewById(R.id.retrieve_minimum_fee_btn);
        progressBar = findViewById(R.id.transaction_progress);
        toAddressInput = findViewById(R.id.to_address_input);
        amountInput = findViewById(R.id.amount_input);
        feeInput = findViewById(R.id.fee_input);
        memoInput = findViewById(R.id.memo_input);

        if (getKinClient().getEnvironment().isMainNet()) {
            sendTransaction.setBackgroundResource(R.drawable.button_main_network_bg);
        }

        initToAddressInput();
        initAmountInput();
        initFeeInput();

        initMinimumFee();
        initSendTransaction();
    }

    private void initToAddressInput() {
        toAddressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence) && !TextUtils.isEmpty(amountInput.getText()) && !TextUtils.isEmpty(feeInput.getText())) {
                    if (!sendTransaction.isEnabled()) {
                        sendTransaction.setEnabled(true);
                    }
                } else if (sendTransaction.isEnabled()) {
                    sendTransaction.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        toAddressInput.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus && !toAddressInput.hasFocus()) {
                hideKeyboard(view);
            }
        });
    }

    private void initAmountInput() {
        amountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence) && !TextUtils.isEmpty(toAddressInput.getText()) && !TextUtils.isEmpty(feeInput.getText())) {
                    if (!sendTransaction.isEnabled()) {
                        sendTransaction.setEnabled(true);
                    }
                } else if (sendTransaction.isEnabled()) {
                    sendTransaction.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        amountInput.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus && !amountInput.hasFocus()) {
                hideKeyboard(view);
            }
        });
    }

    private void initFeeInput() {
        feeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence) && !TextUtils.isEmpty(toAddressInput.getText()) && !TextUtils.isEmpty(amountInput.getText())) {
                    if (!sendTransaction.isEnabled()) {
                        sendTransaction.setEnabled(true);
                    }
                } else if (sendTransaction.isEnabled()) {
                    sendTransaction.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        feeInput.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus && !feeInput.hasFocus()) {
                hideKeyboard(view);
            }
        });
    }

    private void initMinimumFee() {
        retrieveMinimumFee.setOnClickListener(v -> {
            retrieveMinimumFee.setEnabled(false);
            gertMinimumFeeRequest = getKinClient().getMinimumFee();
            gertMinimumFeeRequest.run(new ResultCallback<Long>() {
                @Override
                public void onResult(Long minimumFee) {
                    Log.d(TAG, "handleSendTransaction: minimum fee = " + minimumFee);
                    feeInput.setText(minimumFee != null ? String.valueOf(minimumFee) : "");
                    retrieveMinimumFee.setEnabled(true);
                }

                @Override
                public void onError(Exception e) {
                    retrieveMinimumFee.setEnabled(true);
                    Utils.logError(e, "handleSendTransaction");
                    KinAlertDialog.createErrorDialog(TransactionActivity.this, e.getMessage()).show();
                }
            });
        });
    }

    private void initSendTransaction() {
        sendTransaction.setOnClickListener(view -> {
            BigDecimal amount = new BigDecimal(amountInput.getText().toString());
            try {
                handleSendTransaction(toAddressInput.getText().toString(), amount, Integer.valueOf(feeInput.getText().toString()), memoInput.getText().toString());
            } catch (OperationFailedException e) {
                Utils.logError(e, "handleSendTransaction");
                KinAlertDialog.createErrorDialog(TransactionActivity.this, e.getMessage()).show();
            }
        });
    }

    @Override
    Intent getBackIntent() {
        return WalletActivity.getIntent(this);
    }

    @Override
    int getActionBarTitleRes() {
        return R.string.transaction;
    }

    private void handleSendTransaction(String toAddress, BigDecimal amount, int fee, String memo) throws OperationFailedException {
        progressBar.setVisibility(View.VISIBLE);
        KinAccount account = getKinClient().getAccount(0);
        if (account != null) {
            DisplayCallback<TransactionId> callback = new DisplayCallback<TransactionId>(progressBar) {
                @Override
                public void displayResult(Context context, View view, TransactionId transactionId) {
                    KinAlertDialog.createErrorDialog(context, "Transaction id " + transactionId.id()).show();
                }
            };

            buildAndSendTransaction(toAddress, amount, fee, memo, account, callback);

        } else {
            progressBar.setVisibility(View.GONE);
            throw new AccountDeletedException();
        }
    }

    private void buildAndSendTransaction(String toAddress, BigDecimal amount, int fee, String memo, KinAccount account, DisplayCallback<TransactionId> callback) {
        if (memo == null) {
            buildTransactionRequest = account.buildTransaction(toAddress, amount, fee);
        } else {
            buildTransactionRequest = account.buildTransaction(toAddress, amount, fee, memo);
        }
        buildTransactionRequest.run(new ResultCallback<Transaction>() {
            @Override
            public void onResult(Transaction transaction) {
                Log.d(TAG, "handleSendTransaction: build transaction " + transaction.getId().id() + " succeeded");
                sendTransactionRequest = account.sendTransaction(transaction);
                sendTransactionRequest.run(callback);
            }

            @Override
            public void onError(Exception e) {
                Utils.logError(e, "handleSendTransaction");
                KinAlertDialog.createErrorDialog(TransactionActivity.this, e.getMessage()).show();
            }
        });
    }

    @Override
    protected void onDestroy() { 
        super.onDestroy();
        if (gertMinimumFeeRequest != null) {
            gertMinimumFeeRequest.cancel(false);
        }
        if (buildTransactionRequest != null) {
            buildTransactionRequest.cancel(false);
        }
        if (sendTransactionRequest != null) {
            sendTransactionRequest.cancel(false);
        }
        progressBar = null;
    }
}
