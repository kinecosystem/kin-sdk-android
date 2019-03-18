package recovery.sample;

import android.content.Intent;
import android.util.Log;
import kin.recovery.BackupCallback;
import kin.recovery.BackupManager;
import kin.recovery.RestoreCallback;
import kin.recovery.exception.BackupException;
import kin.sdk.Balance;
import kin.sdk.KinAccount;
import kin.sdk.KinClient;
import kin.sdk.exception.CreateAccountException;
import kin.utils.Request;
import kin.utils.ResultCallback;
import recovery.sample.AccountCreator.Callbacks;

public class BackupAndRestorePresenterImpl implements IBackupAndRestorePresenter {

	private static final String TAG = BackupAndRestorePresenterImpl.class.getSimpleName();

	private IBackupAndRestoreView view;
	private BackupManager backupManager;
	private Request<Balance> balanceRequest;
	private KinClient kinClient;
	private KinAccount currentKinAccount;

	public enum NetWorkType {
		MAIN,
		TEST
	}

	BackupAndRestorePresenterImpl(BackupManager backupManager, KinClient kinClient) {
		this.backupManager = backupManager;
		this.kinClient = kinClient;
		registerToCallbacks();
	}

	@Override
	public void onAttach(IBackupAndRestoreView view) {
		this.view = view;
	}

	@Override
	public void onDetach() {
		backupManager.release();
		view = null;
		balanceRequest = null;
		backupManager = null;
	}

	@Override
	public void backupClicked() {
		if (kinClient.hasAccount()) {
			if (currentKinAccount == null) {
				currentKinAccount = kinClient.getAccount(kinClient.getAccountCount() - 1);
			}
			backupManager.backup(currentKinAccount.getPublicAddress());
		} else {
			if (view != null) {
				view.noAccountToBackupError();
			}
		}
	}

	@Override
	public void restoreClicked() {
		backupManager.restore();
	}

	@Override
	public void createAccountClicked() {
		createAccountLocally();
		if (currentKinAccount != null) {
			onBoardAccount();
		} else {
			if (view != null) {
				view.createAccountError();
			}
		}
	}

	private void createAccountLocally() {
		try {
			currentKinAccount = kinClient.addAccount();
		} catch (CreateAccountException e) {
			Utils.logError(e, "createAccount");
		}
	}

	private void onBoardAccount() {
		if (currentKinAccount != null) {
			AccountCreator accountCreator = new AccountCreator();
			accountCreator.onBoard(currentKinAccount, new Callbacks() {
				@Override
				public void onSuccess() {
					if (view != null) {
						view.updatePublicAddress(currentKinAccount.getPublicAddress());
						view.enableCreateAccountButton();
						updateAccountBalance();
					}
				}

				@Override
				public void onFailure(Exception e) {
					Utils.logError(e, "onBoardAccount");
					if (view != null) {
						view.onBoardAccountError();
						view.enableCreateAccountButton();
					}
				}
			});
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		backupManager.onActivityResult(requestCode, resultCode, data);
	}

	private void registerToCallbacks() {
		backupManager.registerBackupCallback(new BackupCallback() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "BackupCallback - onSuccess");
			}

			@Override
			public void onCancel() {
				Log.d(TAG, "BackupCallback - onCancel");
			}

			@Override
			public void onFailure(BackupException throwable) {
				Log.d(TAG, "BackupCallback - onFailure");
				if (view != null) {
					view.updateBackupError();
				}
			}
		});

		backupManager.registerRestoreCallback(new RestoreCallback() {
			@Override
			public void onSuccess(KinAccount kinAccount) {
				handleRestoreSuccess(kinAccount);
			}

			@Override
			public void onCancel() {
				Log.d(TAG, "BackupCallback - onCancel");
			}

			@Override
			public void onFailure(BackupException throwable) {
				Log.d(TAG, "BackupCallback - onFailure");
				if (view != null) {
					view.updateRestoreError();
				}
			}
		});
	}

	private void handleRestoreSuccess(KinAccount kinAccount) {
		Log.d(TAG, "BackupCallback - onSuccess");
		if (kinAccount != null) {
			currentKinAccount = kinAccount;
			if (view != null) {
				view.updatePublicAddress(currentKinAccount.getPublicAddress());
			}
			updateAccountBalance();
		} else {
			if (view != null) {
				view.updateRestoreError();
			}
		}
	}

	private void updateAccountBalance() {
		balanceRequest = currentKinAccount.getBalance();
		balanceRequest.run(new ResultCallback<Balance>() {
			@Override
			public void onResult(Balance balance) {
				Log.d(TAG, "balance request success");
				if (view != null) {
					view.updateBalance(balance);
				}
			}

			@Override
			public void onError(Exception e) {
				Utils.logError(e, "balance request");
				if (view != null) {
					view.updateBalanceError();
				}
			}
		});
	}
}