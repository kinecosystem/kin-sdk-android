package kin.recovery;

import kin.recovery.exception.BackupException;

public interface RestoreCallback {

	void onSuccess(int index);

	void onCancel();

	void onFailure(BackupException throwable);
}
