package kin.sdk.queue;

import java.util.List;

import kin.base.KeyPair;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.transactiondata.BatchPaymentTransaction;

public class PaymentQueueTransactionProcess extends TransactionProcess {

    private final TransactionSender transactionSender;
    private final List<PendingPayment> pendingPayments;
    private final KeyPair accountFrom;
    private final int fee;

    public PaymentQueueTransactionProcess(TransactionSender transactionSender,
                                          List<PendingPayment> pendingPayments,
                                          KeyPair accountFrom, int fee) {
        super(transactionSender);
        this.transactionSender = transactionSender;
        this.pendingPayments = pendingPayments;
        this.accountFrom = accountFrom;
        this.fee = fee;
    }

    /**
     * @return the list of pending payment that the current transaction consist of.
     * Can be null or empty.
     */
    public List<PendingPayment> payments() {
        return pendingPayments;
    }

    @Override
    public BatchPaymentTransaction transaction() throws OperationFailedException {
        return transaction(null);
    }

    /**
     * @return a transaction that consist of the list of pending payments.
     * Also add a memo to that transaction
     */

    public BatchPaymentTransaction transaction(String memo) throws OperationFailedException {
        return buildTransaction(memo);
    }

    /**
     * Build the transaction
     *
     * @param memo the memo that should be added to the transaction
     * @return a new created transaction.
     * @throws OperationFailedException in case it couldn't be build.
     */
    private BatchPaymentTransaction buildTransaction(String memo) throws OperationFailedException {
        return transactionSender.buildBatchPaymentTransaction(accountFrom, pendingPayments, fee,
                memo);
    }
}
