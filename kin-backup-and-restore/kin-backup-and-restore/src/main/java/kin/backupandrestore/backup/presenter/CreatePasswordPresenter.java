package kin.backupandrestore.backup.presenter;

import kin.backupandrestore.backup.view.CreatePasswordView;
import kin.backupandrestore.base.BasePresenter;

public interface CreatePasswordPresenter extends BasePresenter<CreatePasswordView> {

	void passwordChanged(String changedPassword, String otherPassword, boolean isConfirmPassword);

	void iUnderstandChecked(boolean isChecked);

	void nextButtonClicked(String confirmPassword, String password);

	void onRetryClicked(String password);
}
