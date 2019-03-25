package kin.backup_and_restore.backup.presenter;

import android.os.Bundle;
import kin.backup_and_restore.backup.view.SaveAndShareView;
import kin.backup_and_restore.base.BasePresenter;

public interface SaveAndSharePresenter extends BasePresenter<SaveAndShareView> {

	void iHaveSavedChecked(boolean isChecked);

	void sendQREmailClicked();

	void couldNotLoadQRImage();

	void onSaveInstanceState(Bundle outState);
}
