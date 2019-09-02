package kin.sdk.queue;

import kin.base.KeyPair;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.internal.events.EventsManager;
import kin.sdk.transactiondata.PaymentTransactionParams;
import kin.sdk.transactiondata.Transaction;
import kin.sdk.transactiondata.TransactionParams;

public class TransactionParamsProcessImpl extends TransactionProcess {

    private final TransactionSender transactionSender;
    private final TransactionParams transactionParams;
    private final KeyPair accountFrom;

    // TODO: 2019-08-04 implement and add java docs
    public TransactionParamsProcessImpl(TransactionSender transactionSender,
                                        TransactionParams transactionParams, KeyPair accountFrom,
                                        EventsManager eventsManager) {
        super(transactionSender, eventsManager);
        this.transactionSender = transactionSender;
        this.transactionParams = transactionParams;
        this.accountFrom = accountFrom;
    }

    @Override
    Transaction buildTransaction(String memo) throws OperationFailedException {
        // TODO: 2019-08-28 which memo should we use? probably the last one, which is the one
        //  they set here, no? only if it is null or empty use the first one?  we need to decide!
        if (transactionParams instanceof PaymentTransactionParams) {
            PaymentTransactionParams paymentTransactionParams =
                    (PaymentTransactionParams) transactionParams;
            return transactionSender.buildPaymentTransaction(accountFrom,
                    paymentTransactionParams.destinationPublicAddress(),
                    paymentTransactionParams.amount(),
                    paymentTransactionParams.fee(), paymentTransactionParams.memo());
        } else {
            // will be implemented in the future when we will add more types
            return null;
        }

    }
}
