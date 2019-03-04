package kin.recovery.backup.view;

import kin.recovery.base.BaseView;

public interface CreatePasswordView extends BaseView {

	void setEnterPasswordIsCorrect(boolean isCorrect);

	void setConfirmPasswordIsCorrect(boolean isCorrect);

	void enableNextButton();

	void disableNextButton();

	void showBackupFailed();

	void closeKeyboard();

	void resetEnterPasswordField();

	void resetConfirmPasswordField();
}
