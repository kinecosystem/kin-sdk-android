package kin.sdk.internal.queue;

public interface TaskFinishListener {

    /**
     * Callback for when a task has finished successfully or with failure.
     */
    void onTaskFinish();

}
