package com.kin.sdk.recovery;

import com.kin.sdk.recovery.exception.BackupException;

public interface BackupCallback {

	void onSuccess();

	void onCancel();

	void onFailure(BackupException exception);
}
