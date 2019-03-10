package kin.recovery.events;

import static kin.recovery.exception.BackupException.CODE_UNEXPECTED;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import kin.recovery.BackupCallback;
import kin.recovery.BackupEvents;
import kin.recovery.RestoreCallback;
import kin.recovery.RestoreEvents;
import kin.recovery.exception.BackupException;

public class CallbackManager {

	@Nullable
	private BackupCallback backupCallback;
	@Nullable
	private RestoreCallback restoreCallback;

	private final EventDispatcher eventDispatcher;

	// Request Code
	public static final int REQ_CODE_BACKUP = 9000;
	public static final int REQ_CODE_RESTORE = 9001;

	// Result Code
	static final int RES_CODE_SUCCESS = 5000;
	static final int RES_CODE_CANCEL = 5001;
	static final int RES_CODE_FAILED = 5002;
	static final String EXTRA_KEY_ERROR_MESSAGE = "EXTRA_KEY_ERROR_MESSAGE";
	static final String EXTRA_KEY_ERROR_CODE = "EXTRA_KEY_ERROR_CODE";
	static final String EXTRA_KEY_PUBLIC_ADDRESS = "EXTRA_KEY_PUBLIC_ADDRESS";

	public CallbackManager(@NonNull final EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	public void setBackupCallback(@Nullable BackupCallback backupCallback) {
		this.backupCallback = backupCallback;
	}

	public void setRestoreCallback(@Nullable RestoreCallback restoreCallback) {
		this.restoreCallback = restoreCallback;
	}

	public void setBackupEvents(@Nullable BackupEvents backupEvents) {
		this.eventDispatcher.setBackupEvents(backupEvents);
	}

	public void setRestoreEvents(@Nullable RestoreEvents restoreEvents) {
		this.eventDispatcher.setRestoreEvents(restoreEvents);
	}

	public void unregisterCallbacksAndEvents() {
		this.eventDispatcher.unregister();
		this.backupCallback = null;
		this.restoreCallback = null;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_CODE_BACKUP) {
			handleBackupResult(resultCode, data);
		} else if (requestCode == REQ_CODE_RESTORE) {
			handleRestoreResult(resultCode, data);
		}
	}

	public void sendRestoreSuccessResult(String publicAddress) {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_KEY_PUBLIC_ADDRESS, publicAddress);
		eventDispatcher.setActivityResult(RES_CODE_SUCCESS, intent);
	}

	public void sendBackupSuccessResult() {
		eventDispatcher.setActivityResult(RES_CODE_SUCCESS, null);
	}

	public void setCancelledResult() {
		eventDispatcher.setActivityResult(RES_CODE_CANCEL, null);
	}

	private void handleRestoreResult(int resultCode, Intent data) {
		if (restoreCallback != null) {
			switch (resultCode) {
				case RES_CODE_SUCCESS:
					final String publicAddress = data.getStringExtra(EXTRA_KEY_PUBLIC_ADDRESS);
					if (publicAddress == null) {
						restoreCallback.onFailure(new BackupException(CODE_UNEXPECTED,
							"Unexpected error - imported account public address not found"));
					}
					restoreCallback.onSuccess(publicAddress);
					break;
				case RES_CODE_CANCEL:
					restoreCallback.onCancel();
					break;
				case RES_CODE_FAILED:
					String errorMessage = data.getStringExtra(EXTRA_KEY_ERROR_MESSAGE);
					int code = data.getIntExtra(EXTRA_KEY_ERROR_CODE, 0);
					restoreCallback.onFailure(new BackupException(code, errorMessage));
					break;
				default:
					restoreCallback.onFailure(
						new BackupException(CODE_UNEXPECTED, "Unexpected error - unknown result code " + resultCode));
					break;
			}
		}
	}

	private void handleBackupResult(int resultCode, Intent data) {
		if (backupCallback != null) {
			switch (resultCode) {
				case RES_CODE_SUCCESS:
					backupCallback.onSuccess();
					break;
				case RES_CODE_CANCEL:
					backupCallback.onCancel();
					break;
				case RES_CODE_FAILED:
					String errorMessage = data.getStringExtra(EXTRA_KEY_ERROR_MESSAGE);
					int code = data.getIntExtra(EXTRA_KEY_ERROR_CODE, 0);
					backupCallback.onFailure(new BackupException(code, errorMessage));
					break;
				default:
					backupCallback.onFailure(
						new BackupException(CODE_UNEXPECTED, "Unexpected error - unknown result code " + resultCode));
					break;
			}
		}
	}

	public void sendBackupEvent(@BackupEventCode int eventCode) {
		eventDispatcher.sendEvent(EventDispatcher.BACKUP_EVENTS, eventCode);
	}

	public void sendRestoreEvent(@RestoreEventCode int eventCode) {
		eventDispatcher.sendEvent(EventDispatcher.RESTORE_EVENTS, eventCode);
	}
}