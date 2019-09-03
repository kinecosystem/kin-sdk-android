package kin.sdk.internal.queue;

import java.util.List;

import kin.base.KeyPair;
import kin.sdk.TransactionId;
import kin.sdk.TransactionInterceptor;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.internal.blockchain.GeneralBlockchainInfoRetriever;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.internal.events.EventsManager;
import kin.sdk.queue.BatchPaymentTransactionProcessImpl;
import kin.sdk.queue.PendingPayment;
import kin.sdk.queue.TransactionProcess;
import kin.sdk.transactiondata.BatchPaymentTransaction;

class SendPendingPaymentsTask extends SendTransactionTask {

    private final List<PendingPayment> pendingPayments;
    private final TransactionSender transactionSender;
    private final TransactionInterceptor transactionInterceptor;
    private final EventsManager eventsManager;
    private final GeneralBlockchainInfoRetriever generalBlockchainInfoRetriever;
    private int fee;
    private final KeyPair accountFrom;

    SendPendingPaymentsTask(List<PendingPayment> pendingPayments,
                            TransactionSender transactionSender,
                            TransactionInterceptor transactionInterceptor,
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
        TransactionProcess transactionProcess =
                new BatchPaymentTransactionProcessImpl(transactionSender, pendingPayments,
                        accountFrom, fee, eventsManager);
        // TODO: 2019-09-01 if we give them the TransactionProcess then they wont have access to
        //  the pending payments... need to be changed
        try {
            TransactionId transactionId =
                    transactionInterceptor.interceptTransactionSending(transactionProcess);
            handleTransactionFinished(transactionId);
        } catch (Exception e) {
            // TODO: 2019-09-03 if it is not kinsdkexception then wrap it with kinsdkexception
            //  and send it with events manager
        }
    }

    @Override
    void handleTransactionFinished(TransactionId transactionId) {
        super.handleTransactionFinished(transactionId);
        if (transactionId != null) {
            // TODO: 2019-09-02 need to update pending payments with their status?...

            // TODO: 2019-09-01 invoke events for transaction success
        } else {
            // TODO: 2019-09-01 invoke events for transaction fail
        }
    }

    @Override
    void buildAndSendTransaction() throws OperationFailedException {
        BatchPaymentTransaction transaction =
                transactionSender.buildBatchPaymentTransaction(accountFrom,
                        pendingPayments, fee, null);
        sendTransaction(transaction);
    }

    private void setFeeIfNeeded() {
        // If fee is not set then retrieve the minimum fee.
        if (fee < 0) {
            try {
                fee = (int) generalBlockchainInfoRetriever.getMinimumFeeSync();
            } catch (OperationFailedException e) {
                // TODO: 2019-08-27 need to maybe add an exception to the events manager or something...
            }
        }
    }
}
