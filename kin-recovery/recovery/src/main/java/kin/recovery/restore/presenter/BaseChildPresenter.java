package kin.recovery.restore.presenter;


import kin.recovery.base.BasePresenter;
import kin.recovery.base.BaseView;

interface BaseChildPresenter<T extends BaseView> extends BasePresenter<T> {

	void onAttach(T view, RestorePresenter restorePresenter);
}
