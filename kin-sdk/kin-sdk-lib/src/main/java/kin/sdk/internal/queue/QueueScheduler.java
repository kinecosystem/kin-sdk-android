package kin.sdk.internal.queue;

public interface QueueScheduler {

    /**
     * schedule a task
     */
    void schedule(Runnable runnable);

    /**
     * schedule a task to be run after the specified amount of time elapses.
     *
     * @param delayInMillis The delay (in milliseconds) until the task will be executed
     */
    void scheduleDelayed(Runnable runnable, long delayInMillis);

    /**
     * Remove all the pending tasks
     */
    void removeAllPendingTasks();

    /**
     * Remove a specific pending task
     *
     * @param task is the task to remove
     */
    void removePendingTask(Runnable task);
}
