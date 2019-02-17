package com.kin.sdk.recovery.restore.view;


import com.kin.sdk.recovery.base.BaseView;

public interface RestoreView extends BaseView {

	void navigateToUpload();

	void navigateToEnterPassword(String keystoreData);

	void navigateToRestoreCompleted(Integer data);

	void navigateBack();

	void close();

	void closeKeyboard();

	void showError();
}
