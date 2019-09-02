package kin.sdk.internal.queue

import java.util.*
import kotlin.concurrent.schedule

open class FakeQueueScheduler : QueueScheduler {

    private var timer: Timer? = Timer()
    private val futureTasks: HashMap<Runnable?, TimerTask?> = HashMap()

    //TODO what is better? one of the commented one or the non commented one. Which one?

//    override fun scheduleDelayed(runnable: Runnable?, delayInMillis: Long) {
//        Handler().postDelayed({
//            runnable?.run()
//        }, delayInMillis)
//    }

//    override fun scheduleDelayed(runnable: Runnable?, delayInMillis: Long) {
//        val timer = Timer()
//        timer.schedule(object: TimerTask() {
//            override fun run() {
//                runnable?.run()
//            }
//        }, delayInMillis)
//    }

//    override fun scheduleDelayed(runnable: Runnable?, delayInMillis: Long) {
//        val timer = Timer()
//        timer.schedule(timerTask {runnable?.run() }, delayInMillis)
//    }

    override fun scheduleDelayed(runnable: Runnable?, delayInMillis: Long) {
        val task = timer?.schedule(delayInMillis) {
            runnable?.run()
        }
        futureTasks[runnable] = task
    }

    override fun removeAllPendingTasks() {
        // killing all future futureTasks not including the current running task if there is one.
        timer?.cancel()
        timer?.purge()
        futureTasks.clear()
        timer = Timer()
    }

    override fun removePendingTask(runnable: Runnable?) {
        futureTasks[runnable]?.cancel()
    }

    override fun schedule(runnable: Runnable?) {
        runnable?.run()
    }


}