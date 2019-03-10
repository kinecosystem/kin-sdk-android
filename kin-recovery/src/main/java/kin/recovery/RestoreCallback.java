package kin.recovery;

import kin.recovery.exception.BackupException;

public interface RestoreCallback<T> {

	void onSuccess(T t);

	void onCancel();

	void onFailure(BackupException throwable);
}
