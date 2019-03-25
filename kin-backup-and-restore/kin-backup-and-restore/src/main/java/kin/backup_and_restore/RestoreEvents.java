package kin.backup_and_restore;

public interface RestoreEvents {

	void onRestoreUploadQrCodePageViewed();

	void onRestoreUploadQrCodeBackButtonTapped();

	void onRestoreUploadQrCodeButtonTapped();

	void onRestoreAreYouSureOkButtonTapped();

	void onRestoreAreYouSureCancelButtonTapped();

	void onRestorePasswordEntryPageViewed();

	void onRestorePasswordEntryBackButtonTapped();

	void onRestorePasswordDoneButtonTapped();

}
