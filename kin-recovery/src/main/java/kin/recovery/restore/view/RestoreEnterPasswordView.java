package kin.recovery.restore.view;


import kin.recovery.base.BaseView;

public interface RestoreEnterPasswordView extends BaseView {

	void enableDoneButton();

	void disableDoneButton();

	void decodeError();

	void invalidQrError();
}
