package kin.backupandrestore.backup.presenter;

import kin.backupandrestore.backup.view.CreatePasswordView;
import kin.backupandrestore.base.BasePresenter;

public interface CreatePasswordPresenter extends BasePresenter<CreatePasswordView> {

	void enterPasswordChanged(String password, String confirmPassword);

	void confirmPasswordChanged(String mainPassword, String confirmPassword);

	void iUnderstandChecked(boolean isChecked);

	void nextButtonClicked(String password);

	void onRetryClicked(String password);
}
