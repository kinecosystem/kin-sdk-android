package kin.sdk;


import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import kin.base.KeyPair;
import kin.sdk.exception.AccountDeletedException;
import kin.sdk.exception.CryptoException;
import kin.sdk.exception.OperationFailedException;
import kin.utils.MainHandler;


final class KinAccountImpl extends AbstractKinAccount {

    private final KeyPair account;
    private final BackupRestore backupRestore;
    private final TransactionSender transactionSender;
    private final AccountInfoRetriever accountInfoRetriever;
    private final BlockchainEvents blockchainEvents;
    private boolean isDeleted = false;

    KinAccountImpl(KeyPair account, BackupRestore backupRestore, TransactionSender transactionSender,
        AccountInfoRetriever accountInfoRetriever, BlockchainEventsCreator blockchainEventsCreator, MainHandler mainHandler) {
        super(mainHandler);

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
    public Transaction buildTransactionSync(@Nonnull String publicAddress, @Nonnull BigDecimal amount,
                                            int fee) throws OperationFailedException {
        checkValidAccount();
        return transactionSender.buildTransaction(account, publicAddress, amount, fee);
    }

    @Override
    public Transaction buildTransactionSync(@Nonnull String publicAddress, @Nonnull BigDecimal amount,
                                            int fee, @Nullable String memo) throws OperationFailedException {
        checkValidAccount();
        return transactionSender.buildTransaction(account, publicAddress, amount, fee, memo);
    }

    @Nonnull
    @Override
    public TransactionId sendTransactionSync(Transaction transaction) throws OperationFailedException {
        checkValidAccount();
        return transactionSender.sendTransaction(transaction);
    }

    @Nonnull
    @Override
    public TransactionId sendWhitelistTransactionSync(String whitelist) throws OperationFailedException {
        checkValidAccount();
        return transactionSender.sendWhitelistTransaction(whitelist);
    }

    @Nonnull
    @Override
    public Balance getBalanceSync() throws OperationFailedException {
        checkValidAccount();
        return accountInfoRetriever.getBalance(account.getAccountId());
    }

    @Override
    public AccountStatus getStatusSync() throws OperationFailedException {
        checkValidAccount();
        return accountInfoRetriever.getStatus(account.getAccountId());
    }

    @Override
    public ListenerRegistration addBalanceListener(@Nonnull EventListener<Balance> listener) {
        return blockchainEvents.addBalanceListener(listener);
    }

    @Override
    public ListenerRegistration addPaymentListener(@Nonnull EventListener<PaymentInfo> listener) {
        return blockchainEvents.addPaymentListener(listener);
    }

    @Override
    public ListenerRegistration addAccountCreationListener(EventListener<Void> listener) {
        return blockchainEvents.addAccountCreationListener(listener);
    }

    @Override
    public String export(@Nonnull String passphrase) throws CryptoException {
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

}
