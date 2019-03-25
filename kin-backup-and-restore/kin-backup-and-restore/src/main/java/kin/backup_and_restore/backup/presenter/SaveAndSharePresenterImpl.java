package kin.backup_and_restore.backup.presenter;

import static kin.backup_and_restore.events.BackupEventCode.BACKUP_QR_CODE_PAGE_VIEWED;
import static kin.backup_and_restore.events.BackupEventCode.BACKUP_QR_PAGE_QR_SAVED_TAPPED;
import static kin.backup_and_restore.events.BackupEventCode.BACKUP_QR_PAGE_SEND_QR_TAPPED;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import kin.backup_and_restore.backup.view.BackupNavigator;
import kin.backup_and_restore.backup.view.SaveAndShareView;
import kin.backup_and_restore.base.BasePresenterImpl;
import kin.backup_and_restore.events.CallbackManager;
import kin.backup_and_restore.qr.QRBarcodeGenerator;
import kin.backup_and_restore.qr.QRBarcodeGenerator.QRBarcodeGeneratorException;

public class SaveAndSharePresenterImpl extends BasePresenterImpl<SaveAndShareView> implements SaveAndSharePresenter {

	static final String IS_SEND_EMAIL_CLICKED = "is_send_email_clicked";
	private final BackupNavigator backupNavigator;
	private final QRBarcodeGenerator qrBarcodeGenerator;
	private final CallbackManager callbackManager;

	private Uri qrURI;
	private boolean isSendQREmailClicked;
	private boolean couldNotGenerateQR = false;


	public SaveAndSharePresenterImpl(@NonNull final CallbackManager callbackManager,
		BackupNavigator backupNavigator,
		QRBarcodeGenerator qrBarcodeGenerator, String key, Bundle savedInstanceState) {
		this.backupNavigator = backupNavigator;
		this.qrBarcodeGenerator = qrBarcodeGenerator;
		this.callbackManager = callbackManager;
		this.isSendQREmailClicked = getIsSendQrEmailClicked(savedInstanceState);
		this.callbackManager.sendBackupEvent(BACKUP_QR_CODE_PAGE_VIEWED);
		createQR(key);
	}

	private boolean getIsSendQrEmailClicked(Bundle savedInstanceState) {
		return savedInstanceState != null && savedInstanceState.getBoolean(IS_SEND_EMAIL_CLICKED);
	}

	private void createQR(String key) {
		try {
			this.qrURI = this.qrBarcodeGenerator.generate(key);
		} catch (QRBarcodeGeneratorException e) {
			couldNotGenerateQR = true;
		}
	}

	@Override
	public void onAttach(SaveAndShareView view) {
		super.onAttach(view);
		if (couldNotGenerateQR) {
			couldNotLoadQRImage();
		} else {
			setQRImage();
		}
		if (isSendQREmailClicked && view != null) {
			view.showIHaveSavedCheckBox();
		}
	}

	private void setQRImage() {
		if (view != null) {
			view.setQRImage(qrURI);
		}
	}

	@Override
	public void onBackClicked() {

	}

	@Override
	public void iHaveSavedChecked(boolean isChecked) {
		if (isChecked) {
			callbackManager.sendBackupEvent(BACKUP_QR_PAGE_QR_SAVED_TAPPED);
			backupNavigator.navigateToWellDonePage();
		}
	}

	@Override
	public void sendQREmailClicked() {
		callbackManager.sendBackupEvent(BACKUP_QR_PAGE_SEND_QR_TAPPED);
		isSendQREmailClicked = true;
		if (qrURI != null && view != null) {
			view.showSendIntent(qrURI);
			view.showIHaveSavedCheckBox();
		}
	}

	@Override
	public void couldNotLoadQRImage() {
		if (view != null) {
			view.showErrorTryAgainLater();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(IS_SEND_EMAIL_CLICKED, isSendQREmailClicked);
	}
}
