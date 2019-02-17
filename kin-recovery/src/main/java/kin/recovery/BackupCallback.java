package kin.recovery;

import kin.recovery.exception.BackupException;

public interface BackupCallback {

	void onSuccess();

	void onCancel();

	void onFailure(BackupException exception);
}
