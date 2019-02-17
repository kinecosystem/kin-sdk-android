package com.kin.sdk.recovery.restore.view;


import com.kin.sdk.recovery.base.BaseView;

public interface RestoreEnterPasswordView extends BaseView {

	void enableDoneButton();

	void disableDoneButton();

	void decodeError();

	void invalidQrError();
}
