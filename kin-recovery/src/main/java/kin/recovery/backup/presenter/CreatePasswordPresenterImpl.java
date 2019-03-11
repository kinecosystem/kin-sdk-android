package kin.recovery.backup.presenter;


import static kin.recovery.events.BackupEventCode.BACKUP_CREATE_PASSWORD_PAGE_NEXT_TAPPED;
import static kin.recovery.events.BackupEventCode.BACKUP_CREATE_PASSWORD_PAGE_VIEWED;

import android.support.annotation.NonNull;
import java.util.regex.Pattern;
import kin.recovery.Validator;
import kin.recovery.backup.view.BackupNavigator;
import kin.recovery.backup.view.CreatePasswordView;
import kin.recovery.base.BasePresenterImpl;
import kin.recovery.events.CallbackManager;
import kin.sdk.KinAccount;
import kin.sdk.exception.CryptoException;

public class CreatePasswordPresenterImpl extends BasePresenterImpl<CreatePasswordView> implements
	CreatePasswordPresenter {

	private final BackupNavigator backupNavigator;
	private final CallbackManager callbackManager;
	private KinAccount kinAccount;

	private boolean isPasswordRulesOK = false;
	private boolean isPasswordsMatches = false;
	private boolean isIUnderstandChecked = false;
	private final Pattern pattern;

	public CreatePasswordPresenterImpl(@NonNull final CallbackManager callbackManager,
		@NonNull final BackupNavigator backupNavigator, @NonNull KinAccount kinAccount) {
		this.backupNavigator = backupNavigator;
		this.callbackManager = callbackManager;
		this.callbackManager.sendBackupEvent(BACKUP_CREATE_PASSWORD_PAGE_VIEWED);
		this.kinAccount = kinAccount;
		this.pattern = getPattern();
	}

	@Override
	public void onBackClicked() {
		backupNavigator.closeFlow();
	}

	@Override
	public void enterPasswordChanged(String password, String confirmPassword) {
		if (validatePassword(password)) {
			isPasswordRulesOK = true;
			if (view != null) {
				view.setEnterPasswordIsCorrect(true);
			}
			checkConfirmPassword(password, confirmPassword);
		} else {
			isPasswordRulesOK = false;
			if (password.isEmpty()) {
				if (view != null) {
					view.resetEnterPasswordField();
					view.resetConfirmPasswordField();
				}
			} else {
				if (view != null) {
					view.setEnterPasswordIsCorrect(false);
				}
				checkConfirmPassword(password, confirmPassword);
			}
		}
		checkAllCompleted();
	}

	private void checkConfirmPassword(String password, String confirmPassword) {
		if (!password.isEmpty() && !confirmPassword.isEmpty()) {
			if (password.equals(confirmPassword)) {
				isPasswordsMatches = true;
				if (view != null) {
					view.setConfirmPasswordIsCorrect(true);
					view.closeKeyboard();
				}
			} else {
				isPasswordsMatches = false;
				if (view != null) {
					view.setConfirmPasswordIsCorrect(false);
				}
			}
		} else {
			if (view != null) {
				view.resetConfirmPasswordField();
			}
		}
	}

	@Override
	public void confirmPasswordChanged(String mainPassword, String confirmPassword) {
		checkConfirmPassword(mainPassword, confirmPassword);
		checkAllCompleted();
	}

	@Override
	public void iUnderstandChecked(boolean isChecked) {
		isIUnderstandChecked = isChecked;
		checkAllCompleted();
	}

	@Override
	public void nextButtonClicked(String password) {
		callbackManager.sendBackupEvent(BACKUP_CREATE_PASSWORD_PAGE_NEXT_TAPPED);
		exportAccount(password);
	}

	private void exportAccount(String password) {
		try {
			final String accountKey = kinAccount.export(password);
			backupNavigator.navigateToSaveAndSharePage(accountKey);
		} catch (CryptoException e) {
			if (view != null) {
				view.showBackupFailed();
			}
		}
	}

	@Override
	public void onRetryClicked(String password) {
		exportAccount(password);
	}

	private void checkAllCompleted() {
		if (isPasswordRulesOK && isPasswordsMatches && isIUnderstandChecked) {
			enableNextButton();
		} else {
			disableNextButton();
		}
	}

	private void disableNextButton() {
		if (view != null) {
			view.disableNextButton();
		}
	}

	private void enableNextButton() {
		if (view != null) {
			view.enableNextButton();
		}
	}

	private Pattern getPattern() {
		String digit = "(?=.*\\d)";
		String upper = "(?=.*[A-Z])";
		String lower = "(?=.*[a-z])";
		String special = "(?=.*[!@#$%^&*()_+{}\\[\\]])";
		int min = 9;
		int max = 20;
		StringBuilder sb = new StringBuilder()
			.append("^")
			.append(digit)
			.append(upper)
			.append(lower)
			.append(special)
			.append("(.{")
			.append(min).append(",")
			.append(max).append("})$");
		return Pattern.compile(sb.toString());
	}


	private boolean validatePassword(@NonNull final String password) {
		Validator.checkNotNull(password, "password");
		return pattern.matcher(password).matches();
	}
}
