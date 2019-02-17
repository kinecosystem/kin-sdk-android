package com.kin.sdk.recovery.backup.presenter;

import android.os.Bundle;
import com.kin.sdk.recovery.backup.view.BackupNavigator;
import com.kin.sdk.recovery.backup.view.BackupView;
import com.kin.sdk.recovery.base.BasePresenter;

public interface BackupPresenter extends BasePresenter<BackupView>, BackupNavigator {

	void onSaveInstanceState(Bundle outState);

	void setAccountKey(String key);
}
