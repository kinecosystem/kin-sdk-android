package kin.sdk.internal.queue;

import kin.sdk.TransactionId;
import kin.sdk.queue.PaymentQueue;
import kin.sdk.queue.TransactionProcess;

public class TransactionInterceptorImpl implements PaymentQueue.TransactionInterceptor {

    // TODO: 2019-08-04 implement

    @Override
    public TransactionId interceptTransactionSending(TransactionProcess transactionProcess) throws Exception {
        return null;
    }

}
