package recovery.sample;

import kin.recovery.BackupManager;
import kin.sdk.Balance;

public interface IBackupAndRestoreView {

	BackupManager getBackupManager();


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
