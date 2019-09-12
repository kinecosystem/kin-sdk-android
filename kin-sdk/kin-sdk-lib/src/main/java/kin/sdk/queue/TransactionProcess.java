package kin.sdk.queue;

import kin.sdk.TransactionId;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.transactiondata.Transaction;

/**
 * This class can be used to access both generated transaction and all of its associated
 * PendingPayments.
 */
public abstract class TransactionProcess {

    private final TransactionSender transactionSender;

    TransactionProcess(TransactionSender transactionSender) {
        this.transactionSender = transactionSender;
    }

    /**
     * @return a transaction that consist of the list of pending payments.
     */
    public abstract Transaction transaction() throws OperationFailedException;

    /**
     * Send the transaction with a Transaction object
     *
     * @param transaction the Transaction object to send
     * @return the transaction id.
     */
    public TransactionId send(Transaction transaction) throws OperationFailedException {
        return transactionSender.sendTransaction(transaction);
    }

    /**
     * Send the transaction with a whitelisted payload
     *
     * @param whitelistPayload the whitelist payload
     * @return the transaction id.
     */
    public TransactionId send(String whitelistPayload) throws OperationFailedException {
        return transactionSender.sendWhitelistTransaction(whitelistPayload);
    }
}
