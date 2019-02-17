package com.kin.sdk.recovery.restore.presenter;


import com.kin.sdk.recovery.base.BasePresenterImpl;
import com.kin.sdk.recovery.base.BaseView;

abstract class BaseChildPresenterImpl<T extends BaseView> extends BasePresenterImpl<T> implements
	BaseChildPresenter<T> {

	private RestorePresenter parentPresenter;

	@Override
	public void onAttach(T view, RestorePresenter restorePresenter) {
		super.onAttach(view);
		this.parentPresenter = restorePresenter;
		onAttach(view);
	}

	RestorePresenter getParentPresenter() {
		return parentPresenter;
	}
}
