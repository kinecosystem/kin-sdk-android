package kin.recovery;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import kin.recovery.events.BroadcastManagerImpl;
import kin.recovery.events.CallbackManager;
import kin.recovery.events.EventDispatcherImpl;
import kin.recovery.events.InternalRestoreCallback;
import kin.recovery.exception.BackupException;
import kin.sdk.KinClient;

public final class BackupManager {

	public static final String NETWORK_URL_EXTRA = "networkUrlExtra";
	public static final String NETWORK_PASSPHRASE_EXTRA = "networkPassphraseExtra";
	public static final String APP_ID_EXTRA = "appIdExtra";
	public static final String STORE_KEY_EXTRA = "storeKeyExtra";
	public static final String PUBLIC_ADDRESS_EXTRA = "publicAddressExtra";

	private final CallbackManager callbackManager;
	private final KinClient kinClient;
	private Activity activity;

	public BackupManager(@NonNull final Activity activity, @NonNull KinClient kinClient) {
		Validator.checkNotNull(activity, "activity");
		this.activity = activity;
		this.kinClient = kinClient;
		this.callbackManager = new CallbackManager(
			new EventDispatcherImpl(new BroadcastManagerImpl(activity)));
	}

	public void backup(String publicAddress) {
		new Launcher(activity, kinClient).backupFlow(publicAddress);
	}

	public void restore() {
		new Launcher(activity, kinClient).restoreFlow();
	}

	public void registerBackupCallback(@NonNull final BackupCallback backupCallback) {
		Validator.checkNotNull(backupCallback, "backupCallback");
		this.callbackManager.setBackupCallback(backupCallback);
	}

//	public void registerBackupEvents(@NonNull final BackupEvents backupEvents) {
//		Validator.checkNotNull(backupEvents, "backupEvents");
//		this.callbackManager.setBackupEvents(backupEvents);
//	}

	public void registerRestoreCallback(@NonNull final RestoreCallback restoreCallback) {
		Validator.checkNotNull(restoreCallback, "restoreCallback");
		this.callbackManager.setRestoreCallback(new InternalRestoreCallback() {

			@Override
			public void onSuccess(String publicAddress) {
				restoreCallback.onSuccess(AccountExtractor.getKinAccount(kinClient, publicAddress));
			}

			@Override
			public void onCancel() {
				restoreCallback.onCancel();
			}

			@Override
			public void onFailure(BackupException throwable) {
				restoreCallback.onFailure(throwable);
			}
		});
	}

//	public void registerRestoreEvents(@NonNull final RestoreEvents restoreEvents) {
//		Validator.checkNotNull(restoreEvents, "restoreEvents");
//		this.callbackManager.setRestoreEvents(restoreEvents);
//	}

	public void release() {
		this.callbackManager.unregisterCallbacksAndEvents();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		this.callbackManager.onActivityResult(requestCode, resultCode, data);
	}

}
