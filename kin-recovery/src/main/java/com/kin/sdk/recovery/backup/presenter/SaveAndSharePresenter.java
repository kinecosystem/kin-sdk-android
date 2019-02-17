package com.kin.sdk.recovery.backup.presenter;

import android.os.Bundle;
import com.kin.sdk.recovery.backup.view.SaveAndShareView;
import com.kin.sdk.recovery.base.BasePresenter;

public interface SaveAndSharePresenter extends BasePresenter<SaveAndShareView> {

	void iHaveSavedChecked(boolean isChecked);

	void sendQREmailClicked();

	void couldNotLoadQRImage();

	void onSaveInstanceState(Bundle outState);
}
