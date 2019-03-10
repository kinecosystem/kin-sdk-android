package kin.recovery.restore.view;

import static kin.recovery.base.BaseToolbarActivity.EMPTY_TITLE;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog.Builder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;
import kin.recovery.R;
import kin.recovery.base.BaseToolbarActivity;
import kin.recovery.events.BroadcastManagerImpl;
import kin.recovery.events.CallbackManager;
import kin.recovery.events.EventDispatcherImpl;
import kin.recovery.qr.QRBarcodeGeneratorImpl;
import kin.recovery.qr.QRFileUriHandlerImpl;
import kin.recovery.restore.presenter.FileSharingHelper;
import kin.recovery.restore.presenter.UploadQRPresenter;
import kin.recovery.restore.presenter.UploadQRPresenterImpl;
import kin.recovery.utils.ViewUtils;


public class UploadQRFragment extends Fragment implements UploadQRView {

	private UploadQRPresenter presenter;

	public static UploadQRFragment newInstance() {
		return new UploadQRFragment();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
		@Nullable Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.kinrecovery_fragment_upload_qr, container, false);

		injectPresenter();
		presenter.onAttach(this, ((RestoreActivity) getActivity()).getPresenter());

		initToolbar();
		initViews(root);
		return root;
	}

	private void injectPresenter() {
		presenter = new UploadQRPresenterImpl(
			new CallbackManager(new EventDispatcherImpl(new BroadcastManagerImpl(getActivity()))),
			new FileSharingHelper(this),
			new QRBarcodeGeneratorImpl(new QRFileUriHandlerImpl(getContext())));
	}

	private void initViews(View root) {
		Group btnUploadGroup = root.findViewById(R.id.btn_group);
		ViewUtils.registerToGroupOnClickListener(btnUploadGroup, root, new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				presenter.uploadClicked();
			}
		});
	}

	private void initToolbar() {
		BaseToolbarActivity toolbarActivity = (BaseToolbarActivity) getActivity();
		toolbarActivity.setNavigationIcon(R.drawable.kinrecovery_ic_back);
		toolbarActivity.setToolbarColor(R.color.kinrecovery_bluePrimary);
		toolbarActivity.setToolbarTitle(EMPTY_TITLE);
		toolbarActivity.setNavigationClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				presenter.onBackClicked();
			}
		});
	}

	@Override
	public void showConsentDialog() {
		new Builder(getActivity(), R.style.KinrecoveryAlertDialogTheme)
			.setTitle(R.string.kinrecovery_restore_consent_title)
			.setMessage(R.string.kinrecovery_consent_message)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String chooserTitle = getString(R.string.kinrecovery_choose_qr_image);
					presenter.onOkPressed(chooserTitle);
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					presenter.onCancelPressed();
				}
			})
			.create()
			.show();
	}

	@Override
	public void showErrorDecodingQRDialog() {
		Toast.makeText(getContext(), R.string.kinrecovery_error_decoding_QR, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void showErrorLoadingFileDialog() {
		Toast.makeText(getContext(), R.string.kinrecovery_loading_file_error, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		presenter.onActivityResult(requestCode, resultCode, data);
	}
}