package com.kin.sdk.recovery.backup.presenter;

import com.kin.sdk.recovery.backup.view.CreatePasswordView;
import com.kin.sdk.recovery.base.BasePresenter;

public interface CreatePasswordPresenter extends BasePresenter<CreatePasswordView> {

	void enterPasswordChanged(String password, String confirmPassword);

	void confirmPasswordChanged(String mainPassword, String confirmPassword);

	void iUnderstandChecked(boolean isChecked);

	void nextButtonClicked(String password);

	void onRetryClicked(String password);
}
