package com.kin.sdk.recovery.restore.presenter;


import android.os.Bundle;
import com.kin.sdk.recovery.restore.view.RestoreCompletedView;

public interface RestoreCompletedPresenter extends BaseChildPresenter<RestoreCompletedView> {

	void close();

	void onSaveInstanceState(Bundle outState);
}
