package kin.recovery;

import kin.recovery.exception.BackupAndRestoreException;
import kin.sdk.KinAccount;

public interface RestoreCallback {

	void onSuccess(KinAccount kinAccount);

	void onCancel();

	void onFailure(BackupAndRestoreException throwable);
}
