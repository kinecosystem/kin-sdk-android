package kin.recovery.restore.presenter;

import kin.recovery.restore.view.RestoreCompletedView;

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
