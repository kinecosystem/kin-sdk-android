package kin.backupandrestore.restore.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import kin.backupandrestore.R;
import kin.backupandrestore.base.BaseToolbarActivity;
import kin.backupandrestore.restore.presenter.RestoreCompletedPresenter;
import kin.backupandrestore.restore.presenter.RestoreCompletedPresenterImpl;


public class RestoreCompletedFragment extends Fragment implements RestoreCompletedView {

	private RestoreCompletedPresenter presenter;

	public static RestoreCompletedFragment newInstance() {
		return new RestoreCompletedFragment();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
		@Nullable Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.backup_and_restore_fragment_restore_completed, container, false);

		injectPresenter();
		presenter.onAttach(this, ((RestoreActivity) getActivity()).getPresenter());

		initToolbar();
		initViews(root);
		return root;
	}

	private void injectPresenter() {
		presenter = new RestoreCompletedPresenterImpl();
	}

	private void initToolbar() {
		BaseToolbarActivity toolbarActivity = (BaseToolbarActivity) getActivity();
		toolbarActivity.setNavigationIcon(R.drawable.back);
		toolbarActivity.setToolbarColor(android.R.color.white);
		toolbarActivity.setToolbarTitle(R.string.backup_and_restore_restore_completed_title);
		toolbarActivity.setNavigationClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				presenter.onBackClicked();
			}
		});
	}

	private void initViews(View root) {
		root.findViewById(R.id.kinrecovery_v_btn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				presenter.close();
			}
		});
	}

}
