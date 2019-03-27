package kin.recovery;

import kin.recovery.exception.BackupAndRestoreException;

public interface BackupCallback {

	void onSuccess();

	void onCancel();

	void onFailure(BackupAndRestoreException exception);
}
