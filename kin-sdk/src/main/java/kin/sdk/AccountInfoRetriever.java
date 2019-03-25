package kin.sdk;


import java.io.IOException;
import java.math.BigDecimal;

import javax.annotation.Nonnull;

import kin.base.KeyPair;
import kin.base.Server;
import kin.base.responses.AccountResponse;
import kin.base.responses.HttpResponseException;
import kin.sdk.exception.AccountNotFoundException;
import kin.sdk.exception.OperationFailedException;

class AccountInfoRetriever {

    private final Server server;

    AccountInfoRetriever(Server server) {
        this.server = server;
    }

    /**
     * Get balance for the specified account.
     *
     * @param accountId the account ID to check balance
     * @return the account {@link Balance}
     * @throws AccountNotFoundException if account not created yet
     * @throws OperationFailedException any other error
     */
    Balance getBalance(@Nonnull String accountId) throws OperationFailedException {
        Utils.checkNonnull(accountId, "account");
        Balance balance = null;
        try {
            AccountResponse accountResponse = server.accounts().account(KeyPair.fromAccountId(accountId));
            if (accountResponse == null) {
                throw new OperationFailedException("can't retrieve data for account " + accountId);
            }
            for (AccountResponse.Balance assetBalance : accountResponse.getBalances()) {
                if (assetBalance.getAsset().getType().equalsIgnoreCase("native")) {
                    balance = new BalanceImpl(new BigDecimal(assetBalance.getBalance()));

                }
            }
        } catch (HttpResponseException httpError) {
            if (httpError.getStatusCode() == 404) {
                throw new AccountNotFoundException(accountId);
            } else {
                throw new OperationFailedException(httpError);
            }
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }
        if (balance == null) {
            throw new OperationFailedException(accountId);
        }

        return balance;
    }

    @AccountStatus
    int getStatus(@Nonnull String accountId) throws OperationFailedException {
        try {
            getBalance(accountId);
            return AccountStatus.CREATED;
        } catch (AccountNotFoundException e) {
            return AccountStatus.NOT_CREATED;
        }
    }
}
