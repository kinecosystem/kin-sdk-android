package kin.recovery.restore.view;


import kin.recovery.base.BaseView;

public interface UploadQRView extends BaseView {

	void showConsentDialog();

	void showErrorLoadingFileDialog();

	void showErrorDecodingQRDialog();
}
