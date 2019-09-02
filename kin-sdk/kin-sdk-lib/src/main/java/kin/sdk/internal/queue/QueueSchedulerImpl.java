package kin.sdk.internal.queue;

import android.os.Handler;
import android.os.Looper;

class QueueSchedulerImpl implements QueueScheduler {

    private final Handler handler;

    QueueSchedulerImpl() {
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void schedule(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public void scheduleDelayed(Runnable runnable, long delayInMillis) {
        handler.postDelayed(runnable, delayInMillis);
    }

    @Override
    public void removeAllPendingTasks() {
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void removePendingTask(Runnable runnable) {
        handler.removeCallbacks(runnable);
    }
}



