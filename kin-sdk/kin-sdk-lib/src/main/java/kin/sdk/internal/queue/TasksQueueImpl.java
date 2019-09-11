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
import kin.sdk.queue.PaymentQueueTransactionProcess;
import kin.sdk.queue.PendingPayment;
import kin.sdk.queue.TransactionProcess;
import kin.sdk.transactiondata.TransactionParams;

/**
 * Class which handles the task queue.
 * When finishing then please call to 'stopTaskQueue' in order to free the resources.
 */
class TasksQueueImpl extends HandlerThread implements TasksQueue {

    private static final String nameTag = "TaskQueueHandlerThread";

    private final TransactionSender transactionSender;
    private TransactionInterceptor<PaymentQueueTransactionProcess> paymentQueueTransactionInterceptor;
    private final EventsManager eventsManager;
    private final GeneralBlockchainInfoRetriever generalBlockchainInfoRetriever;
    private final KeyPair accountFrom;
    private TaskFinishListener taskFinishListener;
    private Handler backgroundHandler;
    private int batchPaymentsFee = -1;

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
                            paymentQueueTransactionInterceptor, taskFinishListener,
                            eventsManager, generalBlockchainInfoRetriever, batchPaymentsFee,
                            accountFrom);
            backgroundHandler.post(sendPendingPaymentsTask);
        }
    }

    @Override
    public void scheduleTransactionParamsTask(TransactionParams transactionParams,
                                              TransactionInterceptor<TransactionProcess> transactionParamsInterceptor) {
        if (backgroundHandler != null) {
            SendTransactionParamsTask sendTransactionParamsTask =
                    new SendTransactionParamsTask(transactionParams,
                            transactionSender,
                            transactionParamsInterceptor, taskFinishListener, accountFrom);
            backgroundHandler.postAtFrontOfQueue(sendTransactionParamsTask);
        }
    }

    @Override
    public void stopTaskQueue() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            quitSafely();
        } else {
            quit();
        }
    }

    @Override
    public void setTaskFinishListener(TaskFinishListener taskFinishListener) {
        this.taskFinishListener = taskFinishListener;
    }

    @Override
    public void setBatchPaymentsFee(int batchPaymentsFee) {
        this.batchPaymentsFee = batchPaymentsFee;
    }

    @Override
    public void setPaymentQueueTransactionInterceptor(TransactionInterceptor<PaymentQueueTransactionProcess> pendingPaymentsTransactionInterceptor) {
        this.paymentQueueTransactionInterceptor = pendingPaymentsTransactionInterceptor;
    }
}
