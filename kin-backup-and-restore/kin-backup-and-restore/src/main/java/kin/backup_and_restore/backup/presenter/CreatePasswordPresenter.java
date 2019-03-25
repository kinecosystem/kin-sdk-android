package kin.backup_and_restore.backup.presenter;

import kin.backup_and_restore.backup.view.CreatePasswordView;
import kin.backup_and_restore.base.BasePresenter;

public interface CreatePasswordPresenter extends BasePresenter<CreatePasswordView> {

	void enterPasswordChanged(String password, String confirmPassword);

	void confirmPasswordChanged(String mainPassword, String confirmPassword);

	void iUnderstandChecked(boolean isChecked);

	void nextButtonClicked(String password);

	void onRetryClicked(String password);
}
