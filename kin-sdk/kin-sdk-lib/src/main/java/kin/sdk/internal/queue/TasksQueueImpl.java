package kin.sdk.internal.queue;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

import java.util.List;

import kin.base.KeyPair;
import kin.sdk.TransactionInterceptor;
import kin.sdk.internal.blockchain.GeneralBlockchainInfoRetriever;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.internal.events.EventsManager;
import kin.sdk.queue.PendingPayment;
import kin.sdk.transactiondata.TransactionParams;

/**
 * Class which handles the task queue.
 * When finishing then please call to 'stopTaskQueue' in order to free the resources.
 */
class TasksQueueImpl extends HandlerThread implements TasksQueue {

    private static final String nameTag = "TaskQueueHandlerThread";

    private final TransactionSender transactionSender;
    private TransactionInterceptor pendingPaymentsTransactionInterceptor;
    private final EventsManager eventsManager;
    private final GeneralBlockchainInfoRetriever generalBlockchainInfoRetriever;
    private final KeyPair accountFrom;
    private TaskFinishListener taskFinishListener;
    private Handler backgroundHandler;
    private int fee = -1;

    TasksQueueImpl(@NonNull TransactionSender transactionSender,
                   @NonNull EventsManager eventsManager,
                   @NonNull GeneralBlockchainInfoRetriever generalBlockchainInfoRetriever,
                   @NonNull KeyPair accountFrom) {
        super(nameTag);
        this.transactionSender = transactionSender;
        this.eventsManager = eventsManager;
        this.generalBlockchainInfoRetriever = generalBlockchainInfoRetriever;
        this.accountFrom = accountFrom;

        start();
        // TODO: 2019-08-21 check the threading priority and what is the difference between this
        //  and Process.THREAD_PRIORITY_...
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        backgroundHandler = new Handler(getLooper());
    }

    @Override
    public void schedulePendingPaymentsTask(List<PendingPayment> batchPendingPayments) {
        if (backgroundHandler != null) {
            SendPendingPaymentsTask sendPendingPaymentsTask =
                    new SendPendingPaymentsTask(batchPendingPayments, transactionSender,
                            pendingPaymentsTransactionInterceptor, taskFinishListener,
                            eventsManager, generalBlockchainInfoRetriever, fee, accountFrom);
            backgroundHandler.post(sendPendingPaymentsTask);
        }
    }

    @Override
    public void scheduleTransactionParamsTask(TransactionParams transactionParams,
                                              TransactionInterceptor transactionParamsInterceptor) {
        if (backgroundHandler != null) {
            SendTransactionParamsTask sendTransactionParamsTask =
                    new SendTransactionParamsTask(transactionParams,
                            transactionSender,
                            transactionParamsInterceptor, taskFinishListener, eventsManager,
                            accountFrom);
            // TODO: 2019-08-27 although currently we only have one task each time and we are
            //  dealing with the param at the manager level,
            //  if in the future something will change in the manager then it will still post it
            //  at front of queue. if not needed then we can currently just do backgroundHandler
            //  .post(...)
            backgroundHandler.postAtFrontOfQueue(sendTransactionParamsTask);
        }
    }

    @Override
    public void stopTaskQueue() {
        // TODO: 2019-08-22 need to call quit from outside when finishing
        quit();
    }

    @Override
    public void setTaskFinishListener(TaskFinishListener taskFinishListener) {
        this.taskFinishListener = taskFinishListener;
    }

    @Override
    public void setFee(int fee) {
        this.fee = fee;
    }

    @Override
    public void setPendingPaymentsTransactionInterceptor(TransactionInterceptor pendingPaymentsTransactionInterceptor) {
        this.pendingPaymentsTransactionInterceptor = pendingPaymentsTransactionInterceptor;
    }
}
