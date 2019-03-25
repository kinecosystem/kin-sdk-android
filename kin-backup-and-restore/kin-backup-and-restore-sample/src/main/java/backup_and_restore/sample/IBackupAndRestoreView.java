package backup_and_restore.sample;

import kin.backup_and_restore.BackupAndRestoreManager;
import kin.sdk.Balance;

public interface IBackupAndRestoreView {

	BackupAndRestoreManager getBackupManager();

	void updatePublicAddress(String publicAddress);

	void updateBalance(Balance balance);

	void updateBalanceError();

	void updateRestoreError();

	void noAccountToBackupError();

	void createAccountError();

	void enableCreateAccountButton();

	void onBoardAccountError();

	void updateBackupError();

	void backupSuccess();

	void cancelBackup();

	void cancelRestore();
}
