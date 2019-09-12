package kin.sdk.internal.queue

import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

open class FakeQueueScheduler : QueueScheduler {

    private var scheduler: ScheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(1)
    private val futureTasks: HashMap<Runnable?, ScheduledFuture<*>?> = HashMap()
    val numOfTasks: Int
        get() = futureTasks.size

    init {
        scheduler.removeOnCancelPolicy = true
    }

    override fun scheduleDelayed(runnable: Runnable?, delayInMillis: Long) {
        val scheduleFuture = scheduler.schedule(runnable, delayInMillis, TimeUnit.MILLISECONDS)
        futureTasks[runnable] = scheduleFuture
    }

    override fun removeAllPendingTasks() {
        // killing all future futureTasks not including the current running task if there is one.
        scheduler.shutdown()
        scheduler = ScheduledThreadPoolExecutor(1)
        futureTasks.clear()
    }

    override fun removePendingTask(runnable: Runnable?) {
        val removedRunnable = futureTasks.remove(runnable)
        removedRunnable?.cancel(false)
    }

    override fun schedule(runnable: Runnable?) {
        scheduler.schedule(runnable, 0, TimeUnit.MILLISECONDS)
    }

}