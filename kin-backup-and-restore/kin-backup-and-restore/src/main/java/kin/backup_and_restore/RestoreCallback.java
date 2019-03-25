package kin.backup_and_restore;

import kin.backup_and_restore.exception.BackupAndRestoreException;
import kin.sdk.KinAccount;
import kin.sdk.KinClient;

public interface RestoreCallback {

	void onSuccess(KinClient kinClient, KinAccount kinAccount);

	void onCancel();

	void onFailure(BackupAndRestoreException throwable);
}
