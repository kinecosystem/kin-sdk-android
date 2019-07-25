package kin.backupandrestore.sample;

import android.content.Intent;

public interface IBackupAndRestorePresenter {

    void backupClicked();

    void restoreClicked();

    void createAccountClicked();

    void onActivityResult(int requestCode, int resultCode, Intent data);

    void onAttach(IBackupAndRestoreView view);

    void onDetach();

}
