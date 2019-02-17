package com.kin.sdk.recovery;

import com.kin.sdk.recovery.exception.BackupException;

public interface RestoreCallback {

	void onSuccess(int index);

	void onCancel();

	void onFailure(BackupException throwable);
}
