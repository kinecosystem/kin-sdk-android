package kin.recovery.backup.presenter;

import android.os.Bundle;
import kin.recovery.backup.view.BackupNavigator;
import kin.recovery.backup.view.BackupView;
import kin.recovery.base.BasePresenter;
import kin.sdk.KinAccount;

public interface BackupPresenter extends BasePresenter<BackupView>, BackupNavigator {

	void onSaveInstanceState(Bundle outState);

	void setAccountKey(String key);

	KinAccount getKinAccount();
}
