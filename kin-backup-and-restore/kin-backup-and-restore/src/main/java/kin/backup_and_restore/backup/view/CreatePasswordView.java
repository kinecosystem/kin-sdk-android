package kin.backup_and_restore.backup.view;

import kin.backup_and_restore.base.BaseView;

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
