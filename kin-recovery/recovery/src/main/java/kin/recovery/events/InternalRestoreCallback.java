package kin.recovery.events;

import kin.recovery.exception.BackupAndRestoreException;

public interface InternalRestoreCallback {

	void onSuccess(String publicAddress);

	void onCancel();

	void onFailure(BackupAndRestoreException throwable);

}
