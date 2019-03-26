package kin.backup_and_restore;

public interface BackupEvents {

	void onBackupWelcomePageViewed();

	void onBackupWelcomePageBackButtonTapped();

	void onBackupStartButtonTapped();

	void onBackupCreatePasswordPageViewed();

	void onBackupCreatePasswordBackButtonTapped();

	void onBackupCreatePasswordNextButtonTapped();

	void onBackupQrCodePageViewed();

	void onBackupQrCodeBackButtonTapped();

	void onBackupQrCodeSendButtonTapped();

	void onBackupQrCodeMyQrCodeButtonTapped();

	void onBackupCompletedPageViewed();

	void onBackupPopupPageViewed();

	void onBackupPopupButtonTapped();

	void onBackupPopupLaterButtonTapped();
}