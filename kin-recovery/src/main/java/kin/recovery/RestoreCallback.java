package kin.recovery;

import kin.recovery.exception.BackupException;
import kin.sdk.KinAccount;

public interface RestoreCallback {

	void onSuccess(KinAccount kinAccount);

	void onCancel();

	void onFailure(BackupException throwable);
}
