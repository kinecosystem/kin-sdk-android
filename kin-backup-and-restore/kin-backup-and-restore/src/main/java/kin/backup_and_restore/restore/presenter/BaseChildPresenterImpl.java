package kin.backup_and_restore.restore.presenter;


import kin.backup_and_restore.base.BasePresenterImpl;
import kin.backup_and_restore.base.BaseView;

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
