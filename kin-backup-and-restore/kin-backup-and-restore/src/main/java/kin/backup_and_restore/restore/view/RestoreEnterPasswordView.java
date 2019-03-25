package kin.backup_and_restore.restore.view;


import kin.backup_and_restore.base.BaseView;

public interface RestoreEnterPasswordView extends BaseView {

	void enableDoneButton();

	void disableDoneButton();

	void decodeError();

	void invalidQrError();
}
