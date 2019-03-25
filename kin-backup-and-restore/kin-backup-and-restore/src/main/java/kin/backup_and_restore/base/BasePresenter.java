package kin.backup_and_restore.base;


public interface BasePresenter<T extends BaseView> {

	void onAttach(T view);

	void onDetach();

	T getView();

	void onBackClicked();
}
