package kin.sdk.internal.queue;

import kin.base.KeyPair;
import kin.sdk.TransactionId;
import kin.sdk.TransactionInterceptor;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.internal.events.EventsManager;
import kin.sdk.queue.TransactionParamsProcessImpl;
import kin.sdk.queue.TransactionProcess;
import kin.sdk.transactiondata.PaymentTransactionParams;
import kin.sdk.transactiondata.Transaction;
import kin.sdk.transactiondata.TransactionParams;

class SendTransactionParamsTask extends SendTransactionTask {

    private final TransactionParams transactionParams;
    private final TransactionSender transactionSender;
    private final TransactionInterceptor transactionInterceptor;
    private final EventsManager eventsManager;
    private final KeyPair accountFrom;

    SendTransactionParamsTask(TransactionParams transactionParams,
                              TransactionSender transactionSender,
                              TransactionInterceptor transactionInterceptor,
                              TaskFinishListener taskFinishListener,
                              EventsManager eventsManager,
                              KeyPair accountFrom) {
        super(transactionSender, transactionInterceptor, taskFinishListener);
        this.transactionParams = transactionParams;
        this.transactionSender = transactionSender;
        this.transactionInterceptor = transactionInterceptor;
        this.eventsManager = eventsManager;
        this.accountFrom = accountFrom;
    }

    @Override
    void invokeInterceptor() throws Exception {
        TransactionProcess transactionProcess =
                new TransactionParamsProcessImpl(transactionSender, transactionParams,
                        accountFrom, eventsManager);
        TransactionId transactionId =
                transactionInterceptor.interceptTransactionSending(transactionProcess);
        handleTransactionFinished(transactionId);
    }

    @Override
    void buildAndSendTransaction() throws OperationFailedException {
        // TODO: 2019-09-02 don't like this instanceof of shit. maybe we should think about an
        //  Object Oriented Solution,
        //  for example, create a PaymentTransactionTask Class and in the future
        //  SomeOtherTransactionTask
        Transaction transaction = null;
        if (transactionParams instanceof PaymentTransactionParams) {
            PaymentTransactionParams paymentTransactionParams =
                    (PaymentTransactionParams) transactionParams;
            transaction =
                    transactionSender.buildPaymentTransaction(accountFrom,
                            paymentTransactionParams.destinationPublicAddress(),
                            paymentTransactionParams.amount(), paymentTransactionParams.fee(),
                            paymentTransactionParams.memo());
        }
        sendTransaction(transaction);
    }
    // TODO: 2019-09-01 handle pending payment status
}
