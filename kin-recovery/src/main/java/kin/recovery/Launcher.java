package kin.recovery;


import static kin.recovery.events.CallbackManager.REQ_CODE_BACKUP;
import static kin.recovery.events.CallbackManager.REQ_CODE_RESTORE;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import kin.recovery.backup.view.BackupActivity;
import kin.recovery.restore.view.RestoreActivity;

class Launcher {

	private final Activity activity;

	Launcher(@NonNull final Activity activity) {
		this.activity = activity;
	}

	void backupFlow() {
		startForResult(new Intent(activity, BackupActivity.class), REQ_CODE_BACKUP);
	}

	void restoreFlow() {
		startForResult(new Intent(activity, RestoreActivity.class), REQ_CODE_RESTORE);
	}

	private void startForResult(@NonNull final Intent intent, final int reqCode) {
		activity.startActivityForResult(intent, reqCode);
		activity.overridePendingTransition(R.anim.kinrecovery_slide_in_right, R.anim.kinrecovery_slide_out_left);
	}
}
