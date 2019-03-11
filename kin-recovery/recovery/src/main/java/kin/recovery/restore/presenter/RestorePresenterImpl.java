package kin.recovery.restore.presenter;


import android.content.Intent;
import android.os.Bundle;
import kin.recovery.AccountExtractor;
import kin.recovery.base.BasePresenterImpl;
import kin.recovery.events.CallbackManager;
import kin.recovery.restore.view.RestoreView;
import kin.sdk.KinAccount;
import kin.sdk.KinClient;

public class RestorePresenterImpl extends BasePresenterImpl<RestoreView> implements RestorePresenter {

	static final int STEP_UPLOAD = 0;
	static final int STEP_ENTER_PASSWORD = 1;
	static final int STEP_RESTORE_COMPLETED = 2;
	static final int STEP_FINISH = 3;

	static final String KEY_RESTORE_STEP = "kinrecovery_restore_step";
	public static final String KEY_ACCOUNT_KEY = "kinrecovery_restore_account_key";
	public static final String KEY_PUBLIC_ADDRESS = "kinrecovery_restore_public_address";

	private int currentStep;
	private String accountKey;
	private KinClient kinClient;
	private KinAccount kinAccount;

	private final CallbackManager callbackManager;

	public RestorePresenterImpl(CallbackManager callbackManager, KinClient kinClient, Bundle saveInstanceState) {
		this.callbackManager = callbackManager;
		this.kinClient = kinClient;
		this.kinAccount = getKinAccount(saveInstanceState);
		this.currentStep = getStep(saveInstanceState);
		this.accountKey = getAccountKey(saveInstanceState);
		this.callbackManager.setCancelledResult();
	}

	@Override
	public void onAttach(RestoreView view) {
		super.onAttach(view);
		switchToStep(currentStep);
	}

	private int getStep(Bundle saveInstanceState) {
		return saveInstanceState != null ? saveInstanceState.getInt(KEY_RESTORE_STEP, STEP_UPLOAD) : STEP_UPLOAD;
	}

	private String getAccountKey(Bundle saveInstanceState) {
		return saveInstanceState != null ? saveInstanceState.getString(KEY_ACCOUNT_KEY) : null;
	}

	private KinAccount getKinAccount(Bundle saveInstanceState) {
		return saveInstanceState != null ? AccountExtractor
			.getKinAccount(kinClient, saveInstanceState.getString(KEY_PUBLIC_ADDRESS)) : null;
	}

	@Override
	public void onBackClicked() {
		previousStep();
	}

	private void switchToStep(int step) {
		currentStep = step;
		switch (step) {
			case STEP_UPLOAD:
				getView().navigateToUpload();
				break;
			case STEP_ENTER_PASSWORD:
				if (accountKey != null) {
					getView().navigateToEnterPassword(accountKey);
				} else {
					getView().showError();
				}
				break;
			case STEP_RESTORE_COMPLETED:
				getView().closeKeyboard();
				if (kinAccount != null) {
					getView().navigateToRestoreCompleted();
				} else {
					getView().showError();
				}
				break;
			case STEP_FINISH:
				if (kinAccount != null) {
					callbackManager.sendRestoreSuccessResult(kinAccount.getPublicAddress());
				} else {
					getView().showError();
				}
				getView().close();
				break;
		}
	}

	@Override
	public void navigateToEnterPasswordPage(final String accountKey) {
		this.accountKey = accountKey;
		switchToStep(STEP_ENTER_PASSWORD);
	}

	@Override
	public void navigateToRestoreCompletedPage(final KinAccount kinAccount) {
		this.kinAccount = kinAccount;
		switchToStep(STEP_RESTORE_COMPLETED);
	}

	@Override
	public void closeFlow() {
		switchToStep(STEP_FINISH);
	}


	@Override
	public void previousStep() {
		switch (currentStep) {
			case STEP_UPLOAD:
				getView().close();
				break;
			case STEP_ENTER_PASSWORD:
				getView().navigateBack();
				getView().closeKeyboard();
				break;
			case STEP_RESTORE_COMPLETED:
				getView().navigateBack();
				break;
			case STEP_FINISH:
				getView().navigateBack();
				break;
		}
		currentStep--;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(KEY_RESTORE_STEP, currentStep);
		outState.putString(KEY_ACCOUNT_KEY, accountKey);
		if (kinAccount != null) {
			outState.putString(KEY_PUBLIC_ADDRESS, kinAccount.getPublicAddress());
		}
	}

	@Override
	public KinClient getKinClient() {
		return kinClient;
	}

}
