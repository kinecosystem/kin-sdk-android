package kin.recovery;

public interface RestoreFinishCallback extends RestoreCallback {

	void onRestoreFinishedSuccessfully(String publicAddress);

}
