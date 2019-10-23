package kin.sdk;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;

import kin.base.KeyPair;
import kin.sdk.exception.AccountDeletedException;
import kin.sdk.exception.CryptoException;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.internal.services.AccountInfoRetriever;
import kin.sdk.internal.services.BlockchainEvents;
import kin.sdk.internal.services.TransactionSender;
import kin.sdk.internal.services.helpers.EventListener;
import kin.sdk.internal.services.helpers.ListenerRegistration;
import kin.sdk.internal.utils.BackupRestore;
import kin.sdk.internal.utils.BlockchainEventsCreator;
import kin.sdk.models.Balance;
import kin.sdk.models.PaymentInfo;
import kin.sdk.models.Transaction;
import kin.sdk.models.TransactionId;
import kin.utils.Request;


final class KinAccountImpl implements KinAccount {

    private final KeyPair account;
    private final BackupRestore backupRestore;
    private final TransactionSender transactionSender;
    private final AccountInfoRetriever accountInfoRetriever;
    private final BlockchainEvents blockchainEvents;
    private boolean isDeleted = false;

    KinAccountImpl(KeyPair account, BackupRestore backupRestore, TransactionSender transactionSender,
                   AccountInfoRetriever accountInfoRetriever, BlockchainEventsCreator blockchainEventsCreator) {
        this.account = account;
        this.backupRestore = backupRestore;
        this.transactionSender = transactionSender;
        this.accountInfoRetriever = accountInfoRetriever;
        this.blockchainEvents = blockchainEventsCreator.create(account.getAccountId());
    }

    @Override
    public String getPublicAddress() {
        if (!isDeleted) {
            return account.getAccountId();
        }
        return null;
    }

    @Override
    public Transaction buildTransactionSync(@NonNull String publicAddress, @NonNull BigDecimal amount,
                                            int fee) throws OperationFailedException {
        checkValidAccount();
        return transactionSender.buildTransaction(account, publicAddress, amount, fee);
    }

    @Override
    public Transaction buildTransactionSync(@NonNull String publicAddress, @NonNull BigDecimal amount,
                                            int fee, @Nullable String memo) throws OperationFailedException {
        checkValidAccount();
        return transactionSender.buildTransaction(account, publicAddress, amount, fee, memo);
    }

    @NonNull
    @Override
    public TransactionId sendTransactionSync(Transaction transaction) throws OperationFailedException {
        checkValidAccount();
        return transactionSender.sendTransaction(transaction);
    }

    @NonNull
    @Override
    public TransactionId sendWhitelistTransactionSync(String whitelist) throws OperationFailedException {
        checkValidAccount();
        return transactionSender.sendWhitelistTransaction(whitelist);
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
    public String export(@NonNull String passphrase) throws CryptoException {
        return backupRestore.exportWallet(account, passphrase);
    }

    void markAsDeleted() {
        isDeleted = true;
    }

    private void checkValidAccount() throws AccountDeletedException {
        if (isDeleted) {
            throw new AccountDeletedException();
        }
    }

    @NonNull
    @Override
    public Request<Transaction> buildTransaction(@NonNull final String publicAddress,
                                                 @NonNull final BigDecimal amount, final int fee) {
        return new Request<>(() -> buildTransactionSync(publicAddress, amount, fee));
    }

    @NonNull
    @Override
    public Request<Transaction> buildTransaction(@NonNull final String publicAddress, @NonNull final BigDecimal amount,
                                                 final int fee, @Nullable final String memo) {
        return new Request<>(() -> buildTransactionSync(publicAddress, amount, fee, memo));
    }

    @NonNull
    @Override
    public Request<TransactionId> sendTransaction(final Transaction transaction) {
        return new Request<>(() -> sendTransactionSync(transaction));
    }

    @NonNull
    @Override
    public Request<TransactionId> sendWhitelistTransaction(final String whitelist) {
        return new Request<>(() -> sendWhitelistTransactionSync(whitelist));
    }

    @NonNull
    @Override
    public Request<Balance> getBalance() {
        return new Request<>(this::getBalanceSync);
    }

    @NonNull
    @Override
    public Request<Integer> getStatus() {
        return new Request<>(this::getStatusSync);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        KinAccount account = (KinAccount) obj;
        if (getPublicAddress() == null || account.getPublicAddress() == null) {
            return false;
        }
        return getPublicAddress().equals(account.getPublicAddress());
    }
}
