package kin.sdk.internal.account;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

import kin.sdk.Balance;
import kin.sdk.KinAccount;
import kin.sdk.TransactionId;
import kin.sdk.TransactionInterceptor;
import kin.sdk.queue.PaymentQueue;
import kin.sdk.queue.TransactionProcess;
import kin.sdk.transactiondata.PaymentTransaction;
import kin.sdk.transactiondata.TransactionParams;
import kin.utils.Request;

abstract class AbstractKinAccount implements KinAccount {

    private final PaymentQueue paymentQueue;

    AbstractKinAccount(PaymentQueue paymentQueue) {
        this.paymentQueue = paymentQueue;
    }

    @NonNull
    @Override
    public Request<PaymentTransaction> buildTransaction(@NonNull final String publicAddress,
                                                        @NonNull final BigDecimal amount,
                                                        final int fee) {
        return new Request<>(new Callable<PaymentTransaction>() {
            @Override
            public PaymentTransaction call() throws Exception {
                return buildTransactionSync(publicAddress, amount, fee);
            }
        });
    }

    @NonNull
    @Override
    public Request<PaymentTransaction> buildTransaction(@NonNull final String publicAddress,
                                                        @NonNull final BigDecimal amount,
                                                        final int fee,
                                                        @Nullable final String memo) {
        return new Request<>(new Callable<PaymentTransaction>() {
            @Override
            public PaymentTransaction call() throws Exception {
                return buildTransactionSync(publicAddress, amount, fee, memo);
            }
        });
    }

    @NonNull
    @Override
    public Request<TransactionId> sendTransaction(final PaymentTransaction transaction) {
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

    @Override
    public Request<TransactionId> sendTransaction(final TransactionParams transactionParams) {
        return sendTransaction(transactionParams, null);
    }

    @Override
    public Request<TransactionId> sendTransaction(final TransactionParams transactionParams,
                                                  final TransactionInterceptor<TransactionProcess> interceptor) {
        return new Request<>(new Callable<TransactionId>() {
            @Override
            public TransactionId call() {
//                return ((PaymentQueueImpl) paymentQueue).enqueueTransactionParams
//                (transactionParams, interceptor);
                // TODO: 2019-09-09 implement
                return null;
            }

        });
    }

    @Override
    public Request<Balance> getPendingBalance() {
        return new Request<>(new Callable<Balance>() {
            @Override
            public Balance call() {
                return getPendingBalanceSync();
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
