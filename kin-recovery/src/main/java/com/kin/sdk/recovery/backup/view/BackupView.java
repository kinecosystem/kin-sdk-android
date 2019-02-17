package com.kin.sdk.recovery.backup.view;

import com.kin.sdk.recovery.base.BaseView;
import com.kin.sdk.recovery.base.KeyboardHandler;

public interface BackupView extends BaseView, KeyboardHandler {

	void startBackupFlow();

	void moveToCreatePasswordPage();

	void moveToSaveAndSharePage(String key);

	void onBackButtonClicked();

	void moveToWellDonePage();

	void close();

	void showError();
}
