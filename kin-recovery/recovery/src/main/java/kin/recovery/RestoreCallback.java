package kin.recovery;

import kin.recovery.exception.BackupAndRestoreException;
import kin.sdk.KinAccount;
import kin.sdk.KinClient;

public interface RestoreCallback {

	void onSuccess(KinClient kinClient, KinAccount kinAccount);

	void onCancel();

	void onFailure(BackupAndRestoreException throwable);
}
