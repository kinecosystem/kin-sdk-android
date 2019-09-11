package kin.sdk.queue;

import kin.base.KeyPair;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.transactiondata.PaymentTransactionParams;
import kin.sdk.transactiondata.Transaction;
import kin.sdk.transactiondata.TransactionParams;

public class TransactionParamsProcessImpl extends TransactionProcess {

    private final TransactionSender transactionSender;
    private final TransactionParams transactionParams;
    private final KeyPair accountFrom;

    public TransactionParamsProcessImpl(TransactionSender transactionSender,
                                        TransactionParams transactionParams, KeyPair accountFrom) {
        super(transactionSender);
        this.transactionSender = transactionSender;
        this.transactionParams = transactionParams;
        this.accountFrom = accountFrom;
    }

    @Override
    public Transaction transaction() throws OperationFailedException {
        return buildTransaction();
    }

    /**
     * Build the transaction
     *
     * @return a new created transaction.
     * @throws OperationFailedException in case it couldn't be build.
     */
    private Transaction buildTransaction() throws OperationFailedException {
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
