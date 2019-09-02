package kin.sdk.internal.queue;

import kin.sdk.TransactionId;
import kin.sdk.TransactionInterceptor;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.transactiondata.Transaction;

//  A class that sends a transaction to the blockchain.
abstract class SendTransactionTask implements Runnable {

    private final TransactionSender transactionSender;
    private final TransactionInterceptor transactionInterceptor;
    private final TaskFinishListener taskFinishListener;

    SendTransactionTask(TransactionSender transactionSender,
                        TransactionInterceptor transactionInterceptor,
                        TaskFinishListener taskFinishListener) {
        this.transactionSender = transactionSender;
        this.transactionInterceptor = transactionInterceptor;
        this.taskFinishListener = taskFinishListener;
    }

    void sendTransaction(Transaction transaction) throws OperationFailedException {
        TransactionId transactionId = transactionSender.sendTransaction(transaction);
        handleTransactionFinished(transactionId);
    }

    void handleTransactionFinished(TransactionId transactionId) {
        // if transactionId is null then it means that the transaction has failed
        if (transactionId != null) {
            // TODO: 2019-09-01 invoke events for transaction success
        } else {
            // TODO: 2019-09-01 invoke events for transaction fail
        }
    }

    @Override
    public void run() {
        try {
            if (transactionInterceptor != null) {
                invokeInterceptor();
            } else {
                // TODO: 2019-09-01 if they wanted to send a memo then they should intercept it.
                //  but maybe we should let them add memo easily without the need to intercept?
                buildAndSendTransaction();
            }
        } catch (Exception e) {
            // TODO: 2019-08-26 use events manager and send them the exception if it is not a kin
            //  exception then create a KinException with the exception
        }
    }

    abstract void invokeInterceptor() throws Exception;

    abstract void buildAndSendTransaction() throws OperationFailedException;
}
