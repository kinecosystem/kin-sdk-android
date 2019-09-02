package kin.sdk.queue;

import android.support.annotation.Nullable;

import kin.sdk.TransactionId;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.internal.events.EventsManager;
import kin.sdk.transactiondata.Transaction;

/**
 * This class can be used to access both generated transaction and all of its associated
 * PendingPayments.
 */
public abstract class TransactionProcess {

    private final TransactionSender transactionSender;
    private final EventsManager eventsManager;

    TransactionProcess(TransactionSender transactionSender, EventsManager eventsManager) {
        this.transactionSender = transactionSender;
        this.eventsManager = eventsManager;
    }

    /**
     * Build the transaction
     *
     * @param memo the memo that should be added to the transaction
     * @return a new created transaction.
     * @throws OperationFailedException in case it couldn't be build.
     */
    abstract Transaction buildTransaction(@Nullable String memo) throws OperationFailedException;

    /**
     * @return a transaction that consist of the list of pending payments.
     */
    public Transaction transaction() throws OperationFailedException {
        return buildTransaction(null);
    }

    /**
     * @return a transaction that consist of the list of pending payments.
     * Also add a memo to that transaction
     */
    public Transaction transaction(String memo) throws OperationFailedException {
        return buildTransaction(memo);
    }

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
