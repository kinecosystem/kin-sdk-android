package kin.backup_and_restore.restore.view;


import kin.backup_and_restore.base.BaseView;

public interface RestoreView extends BaseView {

	void navigateToUpload();

	void navigateToEnterPassword(String keystoreData);

	void navigateToRestoreCompleted();

	void navigateBack();

	void close();

	void closeKeyboard();

	void showError();
}
