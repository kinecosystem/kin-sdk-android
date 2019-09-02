package kin.sdk.internal.queue;

public interface TaskFinishListener {

    /**
     * Callback for when a task has finished successfully or with failure.
     *
     * @param task the task itself
     */
    void onTaskFinish(SendTransactionTask task); // TODO: 2019-08-27 check if we really need to
    // give back the task itself

}
