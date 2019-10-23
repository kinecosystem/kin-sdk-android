package kin.sdk.internal.services;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;

import kin.base.KeyPair;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.models.Transaction;
import kin.sdk.models.TransactionId;

public interface TransactionSender {

    Transaction buildTransaction(@NonNull KeyPair from, @NonNull String publicAddress, @NonNull BigDecimal amount,
                                 int fee) throws OperationFailedException;

    Transaction buildTransaction(@NonNull KeyPair from, @NonNull String publicAddress, @NonNull BigDecimal amount,
                                 int fee, @Nullable String memo) throws OperationFailedException;

    TransactionId sendTransaction(Transaction transaction) throws OperationFailedException;

    TransactionId sendWhitelistTransaction(String whitelist) throws OperationFailedException;
}
