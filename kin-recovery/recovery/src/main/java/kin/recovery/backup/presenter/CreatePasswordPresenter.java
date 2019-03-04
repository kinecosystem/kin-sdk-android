package kin.recovery.backup.presenter;

import kin.recovery.backup.view.CreatePasswordView;
import kin.recovery.base.BasePresenter;

public interface CreatePasswordPresenter extends BasePresenter<CreatePasswordView> {

	void enterPasswordChanged(String password, String confirmPassword);

	void confirmPasswordChanged(String mainPassword, String confirmPassword);

	void iUnderstandChecked(boolean isChecked);

	void nextButtonClicked(String password);

	void onRetryClicked(String password);
}
