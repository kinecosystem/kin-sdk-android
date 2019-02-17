package kin.recovery.backup.view;

import kin.recovery.base.BaseView;
import kin.recovery.base.KeyboardHandler;

public interface BackupView extends BaseView, KeyboardHandler {

	void startBackupFlow();

	void moveToCreatePasswordPage();

	void moveToSaveAndSharePage(String key);

	void onBackButtonClicked();

	void moveToWellDonePage();

	void close();

	void showError();
}
