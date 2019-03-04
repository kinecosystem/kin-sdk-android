package kin.recovery.backup.view;

import static kin.recovery.events.BackupEventCode.BACKUP_COMPLETED_PAGE_VIEWED;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import kin.recovery.R;
import kin.recovery.events.BroadcastManagerImpl;
import kin.recovery.events.CallbackManager;
import kin.recovery.events.EventDispatcherImpl;

public class WellDoneBackupFragment extends Fragment {

	public static WellDoneBackupFragment newInstance() {
		return new WellDoneBackupFragment();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
		@Nullable Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.kinrecovery_fragment_well_done_backup, container, false);
		final CallbackManager callbackManager = new CallbackManager(
			new EventDispatcherImpl(new BroadcastManagerImpl(getActivity())));
		callbackManager.sendBackupEvent(BACKUP_COMPLETED_PAGE_VIEWED);
		return root;
	}
}
