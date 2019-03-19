package recovery.sample;

import kin.recovery.BackupAndRestoreManager;
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
}
