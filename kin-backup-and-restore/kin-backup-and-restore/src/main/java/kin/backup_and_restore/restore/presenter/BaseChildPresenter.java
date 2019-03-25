package kin.backup_and_restore.restore.presenter;


import kin.backup_and_restore.base.BasePresenter;
import kin.backup_and_restore.base.BaseView;

interface BaseChildPresenter<T extends BaseView> extends BasePresenter<T> {

	void onAttach(T view, RestorePresenter restorePresenter);
}
