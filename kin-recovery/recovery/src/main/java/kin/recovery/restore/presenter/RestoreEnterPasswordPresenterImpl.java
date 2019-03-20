package kin.recovery.restore.presenter;


import static kin.recovery.events.RestoreEventCode.RESTORE_PASSWORD_DONE_TAPPED;
import static kin.recovery.events.RestoreEventCode.RESTORE_PASSWORD_ENTRY_PAGE_BACK_TAPPED;
import static kin.recovery.events.RestoreEventCode.RESTORE_PASSWORD_ENTRY_PAGE_VIEWED;
import static kin.recovery.exception.BackupAndRestoreException.CODE_RESTORE_FAILED;
import static kin.recovery.exception.BackupAndRestoreException.CODE_RESTORE_INVALID_KEYSTORE_FORMAT;
import static kin.recovery.restore.presenter.RestorePresenterImpl.KEY_ACCOUNT_KEY;

import android.os.Bundle;
import android.support.annotation.NonNull;
import kin.recovery.Validator;
import kin.recovery.events.CallbackManager;
import kin.recovery.exception.BackupAndRestoreException;
import kin.recovery.restore.view.RestoreEnterPasswordView;
import kin.recovery.utils.Logger;
import kin.sdk.KinAccount;
import kin.sdk.exception.CorruptedDataException;
import kin.sdk.exception.CreateAccountException;
import kin.sdk.exception.CryptoException;

public class RestoreEnterPasswordPresenterImpl extends BaseChildPresenterImpl<RestoreEnterPasswordView> implements
	RestoreEnterPasswordPresenter {

	private final String keystoreData;
	private final CallbackManager callbackManager;

	public RestoreEnterPasswordPresenterImpl(@NonNull final CallbackManager callbackManager, String keystoreData) {
		this.callbackManager = callbackManager;
		this.keystoreData = keystoreData;
		this.callbackManager.sendRestoreEvent(RESTORE_PASSWORD_ENTRY_PAGE_VIEWED);
	}

	@Override
	public void onPasswordChanged(String password) {
		if (password.isEmpty()) {
			getView().disableDoneButton();
		} else {
			getView().enableDoneButton();
		}
	}

	@Override
	public void restoreClicked(String password) {
		callbackManager.sendRestoreEvent(RESTORE_PASSWORD_DONE_TAPPED);
		try {
			KinAccount kinAccount = importAccount(keystoreData, password);
			getParentPresenter().navigateToRestoreCompletedPage(kinAccount);
		} catch (BackupAndRestoreException e) {
			Logger.e("RestoreEnterPasswordPresenterImpl - restore failed.", e);
			if (e.getCode() == CODE_RESTORE_INVALID_KEYSTORE_FORMAT) {
				getView().invalidQrError();
			} else {
				getView().decodeError();
			}
		}
	}

	private KinAccount importAccount(@NonNull final String keystore, @NonNull final String password)
		throws BackupAndRestoreException {
		Validator.checkNotNull(keystore, "keystore");
		Validator.checkNotNull(keystore, "password");
		KinAccount importedAccount;
		try {
			importedAccount = getParentPresenter().getKinClient().importAccount(keystore, password);
		} catch (CryptoException e) {
			throw new BackupAndRestoreException(CODE_RESTORE_FAILED, "Could not import the account");
		} catch (CreateAccountException e) {
			throw new BackupAndRestoreException(CODE_RESTORE_FAILED, "Could not create the account");
		} catch (CorruptedDataException e) {
			throw new BackupAndRestoreException(CODE_RESTORE_INVALID_KEYSTORE_FORMAT,
				"The keystore is invalid - wrong format");
		}
		return importedAccount;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(KEY_ACCOUNT_KEY, keystoreData);
	}

	@Override
	public void onBackClicked() {
		callbackManager.sendRestoreEvent(RESTORE_PASSWORD_ENTRY_PAGE_BACK_TAPPED);
		getParentPresenter().previousStep();
	}
}
