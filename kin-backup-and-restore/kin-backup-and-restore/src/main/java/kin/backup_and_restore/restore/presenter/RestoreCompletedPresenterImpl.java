package kin.backup_and_restore.restore.presenter;

import kin.backup_and_restore.restore.view.RestoreCompletedView;

public class RestoreCompletedPresenterImpl extends BaseChildPresenterImpl<RestoreCompletedView> implements
	RestoreCompletedPresenter {

	public RestoreCompletedPresenterImpl() {
	}

	@Override
	public void onBackClicked() {
		getParentPresenter().previousStep();
	}

	@Override
	public void close() {
		getParentPresenter().closeFlow();
	}

}
