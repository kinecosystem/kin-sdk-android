package com.kin.sdk.recovery.restore.presenter;


import com.kin.sdk.recovery.base.BasePresenter;
import com.kin.sdk.recovery.base.BaseView;

interface BaseChildPresenter<T extends BaseView> extends BasePresenter<T> {

	void onAttach(T view, RestorePresenter restorePresenter);
}
