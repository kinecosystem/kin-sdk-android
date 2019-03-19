package recovery.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import kin.recovery.BackupAndRestoreManager;
import kin.sdk.Balance;
import kin.sdk.Environment;
import kin.sdk.KinClient;
import recovery.sample.BackupAndRestorePresenterImpl.NetWorkType;

public class BackupAndRestoreActivity extends AppCompatActivity implements IBackupAndRestoreView, View.OnClickListener {

	private static final String TAG = BackupAndRestoreActivity.class.getSimpleName();

	private IBackupAndRestorePresenter backupAndRestorePresenter;

	private Button createNewAccount;
	private Button backupCurrentAccount;
	private Button recoverAccount;
	private TextView restoredAccountBalance;
	private TextView restoredAccountPublicAddress;
	private View balanceProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup_and_restore);
		createNewAccount = findViewById(R.id.create_new_account);
		backupCurrentAccount = findViewById(R.id.backup_current_account);
		recoverAccount = findViewById(R.id.recover_account);
		restoredAccountBalance = findViewById(R.id.balance_value);
		restoredAccountPublicAddress = findViewById(R.id.public_address_value);
//		balanceProgress = findViewById(R.id.balance_progress);

		createNewAccount.setOnClickListener(this);
		backupCurrentAccount.setOnClickListener(this);
		recoverAccount.setOnClickListener(this);

		// TODO: 13/03/2019 should we support production?
		backupAndRestorePresenter = new BackupAndRestorePresenterImpl(getBackupManager(),
			getKinClient(NetWorkType.TEST));
		backupAndRestorePresenter.onAttach(this);

	}

	private KinClient getKinClient(NetWorkType type) {
		return new KinClient(this, type == NetWorkType.MAIN ? Environment.PRODUCTION : Environment.TEST, "test",
			"recovery_sample_app");
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
		} else if (vId == R.id.recover_account) {
			backupAndRestorePresenter.restoreClicked();
		} else if (vId == R.id.create_new_account) {
			createNewAccount.setEnabled(false);
			backupAndRestorePresenter.createAccountClicked();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		backupAndRestorePresenter.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void enableCreateAccountButton() {
		createNewAccount.setEnabled(true);
	}

	@Override
	public BackupAndRestoreManager getBackupManager() {
		return new BackupAndRestoreManager(this);
	}

	@Override
	public void updatePublicAddress(String publicAddress) {
		restoredAccountPublicAddress.setText(publicAddress);
	}

	@Override
	public void updateBalance(Balance balance) {
		restoredAccountBalance.setText(balance.value().toPlainString());
	}

	@Override
	public void updateBalanceError() {
		restoredAccountBalance.setText(getString(R.string.balance_error));
		showError(R.string.failed_to_retrieve_account_balance);
	}

	@Override
	public void updateRestoreError() {
		updateBalanceError();
		updatePublicAddress(getString(R.string.public_address_error));
		showError(R.string.restoration_has_failed);
	}

	@Override
	public void updateBackupError() {
		showError(R.string.backup_has_failed);
	}

	@Override
	public void noAccountToBackupError() {
		showError(R.string.no_account_to_backup_error);
	}

	@Override
	public void createAccountError() {
		showError(R.string.account_creation_error);
	}

	@Override
	public void onBoardAccountError() {
		showError(R.string.on_board_error);
	}

	private void showError(int errorId) {
		Toast.makeText(this, getString(errorId), Toast.LENGTH_LONG).show();
	}

}
