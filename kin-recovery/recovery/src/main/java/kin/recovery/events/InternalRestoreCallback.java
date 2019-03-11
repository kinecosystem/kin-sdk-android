package kin.recovery.events;

import kin.recovery.exception.BackupException;

public interface InternalRestoreCallback {

	void onSuccess(String publicAddress);

	void onCancel();

	void onFailure(BackupException throwable);

}
