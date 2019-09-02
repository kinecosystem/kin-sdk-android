package kin.sdk.internal.account;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;

import kin.base.KeyPair;
import kin.sdk.Balance;
import kin.sdk.EventListener;
import kin.sdk.ListenerRegistration;
import kin.sdk.PaymentInfo;
import kin.sdk.TransactionId;
import kin.sdk.TransactionInterceptor;
import kin.sdk.exception.AccountDeletedException;
import kin.sdk.exception.CryptoException;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.internal.backuprestore.BackupRestore;
import kin.sdk.internal.blockchain.AccountInfoRetriever;
import kin.sdk.internal.blockchain.GeneralBlockchainInfoRetriever;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.internal.blockchain.events.BlockchainEvents;
import kin.sdk.internal.blockchain.events.BlockchainEventsCreator;
import kin.sdk.internal.queue.PaymentQueueImpl;
import kin.sdk.queue.PaymentQueue;
import kin.sdk.transactiondata.PaymentTransaction;
import kin.sdk.transactiondata.TransactionParams;

public final class KinAccountImpl extends AbstractKinAccount {

    private final KeyPair account;
    private final BackupRestore backupRestore;
    private final TransactionSender transactionSender;
    private final AccountInfoRetriever accountInfoRetriever;
    private final BlockchainEvents blockchainEvents;
    private final PaymentQueue paymentQueue;
    private boolean isDeleted = false;

    public KinAccountImpl(KeyPair account, BackupRestore backupRestore,
                          TransactionSender transactionSender,
                          AccountInfoRetriever accountInfoRetriever,
                          BlockchainEventsCreator blockchainEventsCreator,
                          GeneralBlockchainInfoRetriever generalBlockchainInfoRetriever) {
        this.account = account;
        this.backupRestore = backupRestore;
        this.transactionSender = transactionSender;
        this.accountInfoRetriever = accountInfoRetriever;
        this.blockchainEvents = blockchainEventsCreator.create(account.getAccountId());
        this.paymentQueue = new PaymentQueueImpl(account, transactionSender,
                generalBlockchainInfoRetriever);
    }

    @Override
    public String getPublicAddress() {
        if (!isDeleted) {
            return account.getAccountId();
        }
        return null;
    }

    @Override
    public PaymentTransaction buildTransactionSync(@NonNull String publicAddress,
                                                   @NonNull BigDecimal amount,
                                                   int fee) throws OperationFailedException {
        checkValidAccount();
        return transactionSender.buildPaymentTransaction(account, publicAddress, amount, fee);
    }

    @Override
    public PaymentTransaction buildTransactionSync(@NonNull String publicAddress,
                                                   @NonNull BigDecimal amount,
                                                   int fee, @Nullable String memo) throws OperationFailedException {
        checkValidAccount();
        return transactionSender.buildPaymentTransaction(account, publicAddress, amount, fee, memo);
    }

    @NonNull
    @Override
    public TransactionId sendTransactionSync(PaymentTransaction transaction) throws OperationFailedException {
        checkValidAccount();
        return transactionSender.sendTransaction(transaction);
    }

    @NonNull
    @Override
    public TransactionId sendWhitelistTransactionSync(String whitelist) throws OperationFailedException {
        checkValidAccount();
        return transactionSender.sendWhitelistTransaction(whitelist);
    }

    @Override
    public TransactionId sendTransactionSync(TransactionParams transactionParams,
                                             TransactionInterceptor interceptor) throws OperationFailedException {
        return ((PaymentQueueImpl) paymentQueue).enqueueTransactionParams(transactionParams, interceptor);
    }

    @Override
    public PaymentQueue paymentQueue() {
        return paymentQueue;
    }

    @NonNull
    @Override
    public Balance getPendingBalanceSync() {
        return null; // TODO: 2019-08-15 implement
    }

    @NonNull
    @Override
    public Balance getBalanceSync() throws OperationFailedException {
        checkValidAccount();
        return accountInfoRetriever.getBalance(account.getAccountId());
    }

    @Override
    public int getStatusSync() throws OperationFailedException {
        checkValidAccount();
        return accountInfoRetriever.getStatus(account.getAccountId());
    }

    @Override
    public ListenerRegistration addBalanceListener(@NonNull EventListener<Balance> listener) {
        return blockchainEvents.addBalanceListener(listener);
    }

    @Override
    public ListenerRegistration addPaymentListener(@NonNull EventListener<PaymentInfo> listener) {
        return blockchainEvents.addPaymentListener(listener);
    }

    @Override
    public ListenerRegistration addAccountCreationListener(EventListener<Void> listener) {
        return blockchainEvents.addAccountCreationListener(listener);
    }

    @Override
    public ListenerRegistration addPendingBalanceListener(EventListener<Balance> listener) {
        return null; // TODO: 2019-08-15 implement
    }

    @Override
    public String export(@NonNull String passphrase) throws CryptoException {
        return backupRestore.exportWallet(account, passphrase);
    }

    public KeyPair getKeyPair() {
        return account;
    }

    public void markAsDeleted() {
        isDeleted = true;
    }

    private void checkValidAccount() throws AccountDeletedException {
        if (isDeleted) {
            throw new AccountDeletedException();
        }
    }

}
