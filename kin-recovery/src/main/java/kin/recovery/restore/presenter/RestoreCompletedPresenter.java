package kin.recovery.restore.presenter;


import android.os.Bundle;
import kin.recovery.restore.view.RestoreCompletedView;

public interface RestoreCompletedPresenter extends BaseChildPresenter<RestoreCompletedView> {

	void close();

	void onSaveInstanceState(Bundle outState);
}
