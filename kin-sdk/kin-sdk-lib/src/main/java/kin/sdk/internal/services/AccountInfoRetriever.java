package kin.sdk.internal.services;


import android.support.annotation.NonNull;

import kin.sdk.exception.AccountNotFoundException;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.models.AccountStatus;
import kin.sdk.models.Balance;

public interface AccountInfoRetriever {

    /**
     * Get balance for the specified account.
     *
     * @param accountId the account ID to check balance
     * @return the account {@link Balance}
     * @throws AccountNotFoundException if account not created yet
     * @throws OperationFailedException any other error
     */
    Balance getBalance(@NonNull String accountId) throws OperationFailedException;

    @AccountStatus
    int getStatus(@NonNull String accountId) throws OperationFailedException;
}
