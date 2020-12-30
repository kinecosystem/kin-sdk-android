package kin.sdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
<<<<<<< HEAD
import kin.utils.Request;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
=======

import java.math.BigDecimal;
import java.util.concurrent.Callable;

import kin.utils.Request;
>>>>>>> master

abstract class AbstractKinAccount implements KinAccount {

    @NonNull
    @Override
    public Request<PaymentTransaction> buildPaymentTransaction(@NonNull final String publicAddress,
                                                 @NonNull final BigDecimal amount, final int fee) {
        return new Request<>(new Callable<PaymentTransaction>() {
            @Override
            public PaymentTransaction call() throws Exception {
                return buildPaymentTransactionSync(publicAddress, amount, fee);
            }
        });
    }

    @NonNull
    @Override
    public Request<PaymentTransaction> buildPaymentTransaction(@NonNull final String publicAddress, @NonNull final BigDecimal amount,
                                                 final int fee, @Nullable final String memo) {
        return new Request<>(new Callable<PaymentTransaction>() {
            @Override
            public PaymentTransaction call() throws Exception {
                return buildPaymentTransactionSync(publicAddress, amount, fee, memo);
            }
        });
    }

    @NonNull
    @Override
    public Request<TransactionBuilder> getTransactionBuilder() {
        return new Request<>(new Callable<TransactionBuilder>() {
            @Override
<<<<<<< HEAD
            public TransactionBuilder call() throws Exception {
                return getTransactionBuilderSync();
=======
            public Transaction call() throws Exception {
                return buildTransactionSync(publicAddress, amount, fee, memo);
>>>>>>> master
            }
        });
    }

    @NonNull
    @Override
    public Request<TransactionId> sendTransaction(final TransactionBase transaction) {
        return new Request<>(new Callable<TransactionId>() {
            @Override
            public TransactionId call() throws Exception {
                return sendTransactionSync(transaction);
            }
        });
    }

    @NonNull
    @Override
    public Request<TransactionId> sendWhitelistTransaction(final String whitelist) {
        return new Request<>(new Callable<TransactionId>() {
            @Override
            public TransactionId call() throws Exception {
                return sendWhitelistTransactionSync(whitelist);
            }
        });
    }

    @NonNull
    @Override
    public Request<Balance> getBalance() {
        return new Request<>(new Callable<Balance>() {
            @Override
            public Balance call() throws Exception {
                return getBalanceSync();
            }
        });
    }

    @NonNull
    @Override
    public Request<AccountData> getAccountData() {
        return new Request<>(new Callable<AccountData>() {
            @Override
            public AccountData call() throws Exception {
                return getAccountDataSync();
            }
        });
    }

    @NonNull
    @Override
    public Request<Integer> getStatus() {
        return new Request<>(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return getStatusSync();
            }
        });
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
