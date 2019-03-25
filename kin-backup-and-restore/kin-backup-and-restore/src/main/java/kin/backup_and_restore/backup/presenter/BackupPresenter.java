package kin.backup_and_restore.backup.presenter;

import android.os.Bundle;
import kin.backup_and_restore.backup.view.BackupNavigator;
import kin.backup_and_restore.backup.view.BackupView;
import kin.backup_and_restore.base.BasePresenter;
import kin.sdk.KinAccount;

public interface BackupPresenter extends BasePresenter<BackupView>, BackupNavigator {

	void onSaveInstanceState(Bundle outState);

	void setAccountKey(String key);

	KinAccount getKinAccount();
}
