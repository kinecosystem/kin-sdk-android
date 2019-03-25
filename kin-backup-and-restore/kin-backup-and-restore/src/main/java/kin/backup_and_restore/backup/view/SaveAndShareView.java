package kin.backup_and_restore.backup.view;

import android.net.Uri;
import kin.backup_and_restore.base.BaseView;

public interface SaveAndShareView extends BaseView {

	void setQRImage(Uri qrURI);

	void showSendIntent(Uri qrURI);

	void showIHaveSavedCheckBox();

	void showErrorTryAgainLater();
}
