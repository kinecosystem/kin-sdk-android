package kin.backup_and_restore.events;

import kin.backup_and_restore.exception.BackupAndRestoreException;

public interface InternalRestoreCallback {

	void onSuccess(String publicAddress);

	void onCancel();

	void onFailure(BackupAndRestoreException throwable);

}
