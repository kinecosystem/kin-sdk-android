package kin.backup_and_restore.restore.presenter;


import kin.backup_and_restore.restore.view.RestoreCompletedView;

public interface RestoreCompletedPresenter extends BaseChildPresenter<RestoreCompletedView> {

	void close();
}
