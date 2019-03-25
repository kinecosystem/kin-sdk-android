package kin.backup_and_restore.backup.view;

import kin.backup_and_restore.base.BaseView;
import kin.backup_and_restore.base.KeyboardHandler;

public interface BackupView extends BaseView, KeyboardHandler {

	void startBackupFlow();

	void moveToCreatePasswordPage();

	void moveToSaveAndSharePage(String key);

	void onBackButtonClicked();

	void moveToWellDonePage();

	void close();

	void showError();
}
