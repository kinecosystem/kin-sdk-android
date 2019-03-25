package kin.backup_and_restore.restore.presenter;


import android.os.Bundle;
import kin.backup_and_restore.restore.view.RestoreEnterPasswordView;

public interface RestoreEnterPasswordPresenter extends BaseChildPresenter<RestoreEnterPasswordView> {

	void onPasswordChanged(String password);

	void restoreClicked(String password);

	void onSaveInstanceState(Bundle outState);
}
