package kin.sdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import kin.base.KeyPair;
import kin.sdk.exception.AccountDeletedException;
import kin.sdk.exception.CryptoException;
import kin.sdk.exception.OperationFailedException;

import java.math.BigDecimal;
import java.util.List;


final class KinAccountImpl extends AbstractKinAccount {

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
    public PaymentTransaction buildPaymentTransactionSync(@NonNull String publicAddress, @NonNull BigDecimal amount,
                                            int fee) throws OperationFailedException {
        checkValidAccount();
        return transactionSender.buildPaymentTransaction(account, publicAddress, amount, fee);
    }

    @Override
    public PaymentTransaction buildPaymentTransactionSync(@NonNull String publicAddress, @NonNull BigDecimal amount,
                                            int fee, @Nullable String memo) throws OperationFailedException {
        checkValidAccount();
        return transactionSender.buildPaymentTransaction(account, publicAddress, amount, fee, memo);
    }

    @Override
    public TransactionBuilder getTransactionBuilderSync() throws OperationFailedException {
        checkValidAccount();
        return transactionSender.getTransactionBuilder(account);
    }

    @NonNull
    @Override
    public TransactionId sendTransactionSync(TransactionBase transaction) throws OperationFailedException {
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

    @NonNull
    @Override
    public Balance getAggregatedBalanceSync() throws OperationFailedException {
        checkValidAccount();
        return accountInfoRetriever.getAggregatedBalance(account.getAccountId());
    }

    @NonNull
    @Override
    public Balance getAggregatedBalanceSync(String publicAddress) throws OperationFailedException {
        if (publicAddress == null || publicAddress.length() == 0) {
            throw new IllegalArgumentException("public address not valid");
        }
        return accountInfoRetriever.getAggregatedBalance(publicAddress);
    }

    @NonNull
    @Override
    public List<ControlledAccount> getControlledAccountsSync() throws OperationFailedException {
        checkValidAccount();
        return accountInfoRetriever.getControlledAccounts(account.getAccountId());
    }

    @NonNull
    @Override
    public AccountData getAccountDataSync() throws OperationFailedException {
        checkValidAccount();
        return accountInfoRetriever.getAccountData(account.getAccountId());
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

    KeyPair getKeyPair() {
        return account;
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
