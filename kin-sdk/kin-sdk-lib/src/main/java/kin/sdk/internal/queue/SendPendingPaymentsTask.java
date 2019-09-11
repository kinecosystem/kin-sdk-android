package kin.sdk.internal.queue;

import java.util.List;

import kin.base.KeyPair;
import kin.sdk.TransactionId;
import kin.sdk.TransactionInterceptor;
import kin.sdk.exception.KinException;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.internal.blockchain.GeneralBlockchainInfoRetriever;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.internal.events.EventsManager;
import kin.sdk.queue.PaymentQueueTransactionProcess;
import kin.sdk.queue.PendingPayment;
import kin.sdk.transactiondata.BatchPaymentTransaction;

class SendPendingPaymentsTask extends SendTransactionTask {

    private final List<PendingPayment> pendingPayments;
    private final TransactionSender transactionSender;
    private final TransactionInterceptor<PaymentQueueTransactionProcess> transactionInterceptor;
    private final EventsManager eventsManager;
    private final GeneralBlockchainInfoRetriever generalBlockchainInfoRetriever;
    private int fee;
    private final KeyPair accountFrom;

    SendPendingPaymentsTask(List<PendingPayment> pendingPayments,
                            TransactionSender transactionSender,
                            TransactionInterceptor<PaymentQueueTransactionProcess> transactionInterceptor,
                            TaskFinishListener taskFinishListener,
                            EventsManager eventsManager,
                            GeneralBlockchainInfoRetriever generalBlockchainInfoRetriever,
                            int fee, KeyPair accountFrom) {
        super(transactionSender, transactionInterceptor, taskFinishListener);
        this.pendingPayments = pendingPayments;
        this.transactionSender = transactionSender;
        this.transactionInterceptor = transactionInterceptor;
        this.eventsManager = eventsManager;
        this.generalBlockchainInfoRetriever = generalBlockchainInfoRetriever;
        this.fee = fee;
        this.accountFrom = accountFrom;
    }

    @Override
    public void run() {
        setFeeIfNeeded();
        super.run();
    }

    @Override
    void invokeInterceptor() {
        PaymentQueueTransactionProcess transactionProcess =
                new PaymentQueueTransactionProcess(transactionSender, pendingPayments,
                        accountFrom, fee);
        try {
            TransactionId transactionId =
                    transactionInterceptor.interceptTransactionSending(transactionProcess);
            handleTransactionFinished(transactionId);
        } catch (Exception e) {
            if (!(e instanceof KinException)) {
                e = new OperationFailedException(e);
            }

            //TODO send it with events manager
        }
    }

    @Override
    void handleTransactionFinished(TransactionId transactionId) {
        super.handleTransactionFinished(transactionId);
        if (transactionId != null) {
            eventsManager.onTransactionSendSuccess(transactionId, pendingPayments);
            // TODO: 2019-09-01 invoke events for transaction success
        } else {
            //TODO need to create a meaningful message for this scenario
            eventsManager.onTransactionSendFailed(pendingPayments, new OperationFailedException(
                    "Transaction Id is null"));
            // TODO: 2019-09-01 invoke events for transaction fail
        }
    }

    @Override
    void buildAndSendTransaction() throws OperationFailedException {
        BatchPaymentTransaction transaction =
                transactionSender.buildBatchPaymentTransaction(accountFrom, pendingPayments, fee,
                        null);
        eventsManager.onTransactionSend(transaction, pendingPayments);
        sendTransaction(transaction);
    }

    private void setFeeIfNeeded() {
        // If fee is not set then retrieve the minimum fee.
        if (fee < 0) {
            try {
                fee = (int) generalBlockchainInfoRetriever.getMinimumFeeSync();
            } catch (OperationFailedException e) {
                e.printStackTrace();
                // TODO: 2019-08-27 need to maybe add an exception to the events manager or
                //  something...
            }
        }
    }
}
