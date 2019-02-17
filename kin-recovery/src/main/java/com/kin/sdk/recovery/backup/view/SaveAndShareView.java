package com.kin.sdk.recovery.backup.view;

import android.net.Uri;
import com.kin.sdk.recovery.base.BaseView;

public interface SaveAndShareView extends BaseView {

	void setQRImage(Uri qrURI);

	void showSendIntent(Uri qrURI);

	void showIHaveSavedCheckBox();

	void showErrorTryAgainLater();
}
