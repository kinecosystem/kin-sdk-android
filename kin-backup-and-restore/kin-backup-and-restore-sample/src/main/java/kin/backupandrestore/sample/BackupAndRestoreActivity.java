package kin.backupandrestore.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import kin.backupandrestore.BackupAndRestoreManager;
import kin.backupandrestore.sample.BackupAndRestorePresenterImpl.NetWorkType;
import kin.sdk.Balance;
import kin.sdk.Environment;
import kin.sdk.KinClient;

public class BackupAndRestoreActivity extends AppCompatActivity implements IBackupAndRestoreView, View.OnClickListener {

    private static final int REQ_CODE_BACKUP = 9000;
    private static final int REQ_CODE_RESTORE = 9001;

    private IBackupAndRestorePresenter backupAndRestorePresenter;

    private Button createNewAccount;

    private Button backupCurrentAccount;
    private Button restoreAccount;
    private TextView restoredAccountBalance;
    private TextView restoredAccountPublicAddress;
    private View balanceProgress;
    private View publicAddressProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_and_restore);
        createNewAccount = findViewById(R.id.create_new_account);
        backupCurrentAccount = findViewById(R.id.backup_current_account);
        restoreAccount = findViewById(R.id.restore_account);
        restoredAccountBalance = findViewById(R.id.balance_value);
        restoredAccountPublicAddress = findViewById(R.id.public_address_value);
        balanceProgress = findViewById(R.id.balance_value_progress);
        publicAddressProgressBar = findViewById(R.id.public_address_value_progress);

        createNewAccount.setOnClickListener(this);
        backupCurrentAccount.setOnClickListener(this);
        restoreAccount.setOnClickListener(this);

        KinClient kinClient = getKinClient(NetWorkType.TEST);
        backupAndRestorePresenter = new BackupAndRestorePresenterImpl(getBackupManager(), kinClient);
        backupAndRestorePresenter.onAttach(this);

    }

    private KinClient getKinClient(NetWorkType type) {
        return new KinClient(this, type == NetWorkType.MAIN ? Environment.PRODUCTION : Environment.TEST, "test",
                "backup_and_restore_sample_app");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        backupAndRestorePresenter.onDetach();
    }

    @Override
    public void onClick(View v) {
        final int vId = v.getId();
        if (vId == R.id.backup_current_account) {
            backupAndRestorePresenter.backupClicked();
        } else if (vId == R.id.restore_account) {
            setBalanceProgressBar(true);
            backupAndRestorePresenter.restoreClicked();
        } else if (vId == R.id.create_new_account) {
            setBalanceProgressBar(true);
            setPublicAddressProgressBar(true);
            createNewAccount.setEnabled(false);
            backupCurrentAccount.setEnabled(false);
            restoreAccount.setEnabled(false);
            backupAndRestorePresenter.createAccountClicked();
        }
    }

    private void setBalanceProgressBar(boolean show) {
        balanceProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setPublicAddressProgressBar(boolean show) {
        publicAddressProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_BACKUP || requestCode == REQ_CODE_RESTORE) {
            backupAndRestorePresenter.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void enableCreateAccountButton() {
        createNewAccount.setEnabled(true);
        backupCurrentAccount.setEnabled(true);
        restoreAccount.setEnabled(true);
    }

    @Override
    public BackupAndRestoreManager getBackupManager() {
        return new BackupAndRestoreManager(this, REQ_CODE_BACKUP, REQ_CODE_RESTORE);
    }

    @Override
    public void cancelBackup() {
        setBalanceProgressBar(false);
    }

    @Override
    public void cancelRestore() {
        setBalanceProgressBar(false);
    }

    @Override
    public void updatePublicAddress(String publicAddress) {
        setPublicAddressProgressBar(false);
        restoredAccountPublicAddress.setText(publicAddress);
    }

    @Override
    public void updateBalance(Balance balance) {
        setBalanceProgressBar(false);
        String balanceText = balance.value().stripTrailingZeros().toPlainString() + " " + getString(R.string.kin);
        restoredAccountBalance.setText(balanceText);
    }

    @Override
    public void updateBalanceError() {
        setBalanceProgressBar(false);
        restoredAccountBalance.setText(getString(R.string.balance_error));
        showMessage(R.string.failed_to_retrieve_account_balance);
    }

    @Override
    public void updateRestoreError() {
        updateBalanceError();
        updatePublicAddress(getString(R.string.public_address_error));
        showMessage(R.string.restoration_has_failed);
    }

    @Override
    public void backupSuccess() {
        showMessage(R.string.backup_success_message);
    }

    @Override
    public void updateBackupError() {
        showMessage(R.string.backup_has_failed);
    }

    @Override
    public void noAccountToBackupError() {
        showMessage(R.string.no_account_to_backup_error);
    }

    @Override
    public void createAccountError() {
        showMessage(R.string.account_creation_error);
    }

    @Override
    public void onBoardAccountError() {
        showMessage(R.string.on_board_error);
    }

    private void showMessage(int errorId) {
        Toast.makeText(this, getString(errorId), Toast.LENGTH_LONG).show();
    }

}
