package com.kin.sdk.recovery.restore.view;


import com.kin.sdk.recovery.base.BaseView;

public interface UploadQRView extends BaseView {

	void showConsentDialog();

	void showErrorLoadingFileDialog();

	void showErrorDecodingQRDialog();
}
