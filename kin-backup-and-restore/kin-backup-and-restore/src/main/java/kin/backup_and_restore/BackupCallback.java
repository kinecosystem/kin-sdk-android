package kin.backup_and_restore;

import kin.backup_and_restore.exception.BackupAndRestoreException;

public interface BackupCallback {

	void onSuccess();

	void onCancel();

	void onFailure(BackupAndRestoreException exception);
}
