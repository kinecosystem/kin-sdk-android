package kin.sdk.internal.queue;

import kin.base.KeyPair;
import kin.sdk.TransactionId;
import kin.sdk.TransactionInterceptor;
import kin.sdk.exception.KinException;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.queue.TransactionParamsProcessImpl;
import kin.sdk.queue.TransactionProcess;
import kin.sdk.transactiondata.PaymentTransactionParams;
import kin.sdk.transactiondata.Transaction;
import kin.sdk.transactiondata.TransactionParams;

class SendTransactionParamsTask extends SendTransactionTask {

    private final TransactionParams transactionParams;
    private final TransactionSender transactionSender;
    private final TransactionInterceptor<TransactionProcess> transactionInterceptor;
    private final KeyPair accountFrom;

    SendTransactionParamsTask(TransactionParams transactionParams,
                              TransactionSender transactionSender,
                              TransactionInterceptor<TransactionProcess> transactionInterceptor,
                              TaskFinishListener taskFinishListener, KeyPair accountFrom) {
        super(transactionSender, transactionInterceptor, taskFinishListener);
        this.transactionParams = transactionParams;
        this.transactionSender = transactionSender;
        this.transactionInterceptor = transactionInterceptor;
        this.accountFrom = accountFrom;
    }

    @Override
    void invokeInterceptor() {
        TransactionProcess transactionProcess =
                new TransactionParamsProcessImpl(transactionSender, transactionParams,
                        accountFrom);
        TransactionId transactionId = null;
        try {
            transactionId = transactionInterceptor.interceptTransactionSending(transactionProcess);
        } catch (Exception e) {
            if (!(e instanceof KinException)) {
                e = new OperationFailedException(e);
            }
            // TODO: 2019-09-11 because transaction params doesn't have any event listener then
            //  what should we do here?
        }
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
}
