package kin.sdk;



import java.math.BigDecimal;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import kin.utils.MainHandler;
import kin.utils.Request;

abstract class AbstractKinAccount implements KinAccount {

    private MainHandler mainHandler;

    AbstractKinAccount(MainHandler mainHandler) {
        this.mainHandler = mainHandler;
    }

    @Nonnull
    @Override
    public Request<Transaction> buildTransaction(@Nonnull final String publicAddress,
                                                 @Nonnull final BigDecimal amount, final int fee) {
        return new Request<>(new Callable<Transaction>() {
            @Override
            public Transaction call() throws Exception {
                return buildTransactionSync(publicAddress, amount, fee);
            }
        }, mainHandler);
    }@Nonnull
    @Override
    public Request<Transaction> buildTransaction(@Nonnull final String publicAddress, @Nonnull final BigDecimal amount,
                                                 final int fee, @Nullable final String memo) {
        return new Request<>(new Callable<Transaction>() {
            @Override
            public Transaction call() throws Exception {
                return buildTransactionSync(publicAddress, amount, fee, memo); 
            }
        }, mainHandler);
    }

    @Nonnull
    @Override
    public Request<TransactionId> sendTransaction(final Transaction transaction) {
        return new Request<>(new Callable<TransactionId>() {
            @Override
            public TransactionId call() throws Exception {
                return sendTransactionSync(transaction);
            }
        }, mainHandler);
    }

    @Nonnull
    @Override
    public Request<TransactionId> sendWhitelistTransaction(final String whitelist) {
        return new Request<>(new Callable<TransactionId>() {
            @Override
            public TransactionId call() throws Exception {
                return sendWhitelistTransactionSync(whitelist);
            }
        }, mainHandler);
    }

    @Nonnull
    @Override
    public Request<Balance> getBalance() {
        return new Request<>(new Callable<Balance>() {
            @Override
            public Balance call() throws Exception {
                return getBalanceSync();
            }
        }, mainHandler);
    }

    @Nonnull
    @Override
    public Request<AccountStatus> getStatus() {
        return new Request<>(new Callable<AccountStatus>() {
            @Override
            public AccountStatus call() throws Exception {
                return getStatusSync();
            }
        }, mainHandler);
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
