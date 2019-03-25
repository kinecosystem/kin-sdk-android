package kin.backup_and_restore.backup.presenter;


import static kin.backup_and_restore.events.BackupEventCode.BACKUP_WELCOME_PAGE_START_TAPPED;
import static kin.backup_and_restore.events.BackupEventCode.BACKUP_WELCOME_PAGE_VIEWED;

import android.support.annotation.NonNull;
import kin.backup_and_restore.backup.view.BackupNavigator;
import kin.backup_and_restore.base.BasePresenterImpl;
import kin.backup_and_restore.base.BaseView;
import kin.backup_and_restore.events.CallbackManager;

public class BackupInfoPresenterImpl extends BasePresenterImpl<BaseView> implements BackupInfoPresenter {

	private final BackupNavigator backupNavigator;
	private final CallbackManager callbackManager;

	public BackupInfoPresenterImpl(@NonNull CallbackManager callbackManager,
		BackupNavigator backupNavigator) {
		this.backupNavigator = backupNavigator;
		this.callbackManager = callbackManager;
		this.callbackManager.sendBackupEvent(BACKUP_WELCOME_PAGE_VIEWED);
		this.callbackManager.setCancelledResult(); // make sure cancel will be called if nothing happened.
	}

	@Override
	public void onBackClicked() {
		backupNavigator.closeFlow();
	}

	@Override
	public void letsGoButtonClicked() {
		callbackManager.sendBackupEvent(BACKUP_WELCOME_PAGE_START_TAPPED);
		if (view != null) {
			backupNavigator.navigateToCreatePasswordPage();
		}
	}
}
