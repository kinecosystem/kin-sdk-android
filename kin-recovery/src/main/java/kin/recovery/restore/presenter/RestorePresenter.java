package kin.recovery.restore.presenter;


import android.content.Intent;
import android.os.Bundle;
import kin.recovery.base.BasePresenter;
import kin.recovery.restore.view.RestoreView;

public interface RestorePresenter extends BasePresenter<RestoreView> {

	void navigateToEnterPasswordPage(final String accountKey);

	void navigateToRestoreCompletedPage(final int accountIndex);

	void closeFlow(final int accountIndex);

	void previousStep();

	void onActivityResult(int requestCode, int resultCode, Intent data);

	void onSaveInstanceState(Bundle outState);
}
