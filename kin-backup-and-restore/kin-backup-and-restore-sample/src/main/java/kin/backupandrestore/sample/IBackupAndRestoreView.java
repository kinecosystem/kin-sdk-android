package kin.backupandrestore.sample;

import kin.backupandrestore.BackupAndRestoreManager;
import kin.sdk.models.Balance;

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
