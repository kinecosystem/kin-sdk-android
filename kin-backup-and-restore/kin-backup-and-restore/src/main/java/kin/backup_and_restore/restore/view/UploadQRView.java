package kin.backup_and_restore.restore.view;


import kin.backup_and_restore.base.BaseView;

public interface UploadQRView extends BaseView {

	void showConsentDialog();

	void showErrorLoadingFileDialog();

	void showErrorDecodingQRDialog();
}
