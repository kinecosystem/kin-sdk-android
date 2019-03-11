package kin.recovery.backup.presenter;

import android.os.Bundle;
import kin.recovery.backup.view.SaveAndShareView;
import kin.recovery.base.BasePresenter;

public interface SaveAndSharePresenter extends BasePresenter<SaveAndShareView> {

	void iHaveSavedChecked(boolean isChecked);

	void sendQREmailClicked();

	void couldNotLoadQRImage();

	void onSaveInstanceState(Bundle outState);
}
