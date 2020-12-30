package kin.sdk;


import android.support.annotation.NonNull;

import java.io.IOException;
import java.math.BigDecimal;
<<<<<<< HEAD
import java.util.ArrayList;
import java.util.List;
=======

>>>>>>> master
import kin.base.KeyPair;
import kin.base.Server;
import kin.base.responses.AccountResponse;
import kin.base.responses.AggregatedBalanceResponse;
import kin.base.responses.ControlledAccountsResponse;
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
    Balance getBalance(@NonNull String accountId) throws OperationFailedException {
        Utils.checkNotNull(accountId, "account");
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

    Balance getAggregatedBalance(@NonNull String accountId) throws OperationFailedException {
        Utils.checkNotNull(accountId, "account");
        Balance balance = null;

        try {
            AggregatedBalanceResponse aggregatedBalanceResponse = server.accounts()
                .aggregateBalance(KeyPair.fromAccountId(accountId));
            if (aggregatedBalanceResponse == null || aggregatedBalanceResponse.getAggregatedBalance() == null) {
                throw new OperationFailedException("can't retrieve data for account " + accountId);
            }
            String aggregateBalance = aggregatedBalanceResponse.getAggregatedBalance().getAggregateBalance();
            if (aggregateBalance != null && !aggregateBalance.isEmpty()) {
                balance = new BalanceImpl(new BigDecimal(aggregateBalance));
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

    List<ControlledAccount> getControlledAccounts(@NonNull String accountId) throws OperationFailedException {
        Utils.checkNotNull(accountId, "account");
        List<ControlledAccount> controlledAccounts = new ArrayList<>();

        try {
            ControlledAccountsResponse controlledAccountsResponse = server.accounts()
                .controlledAccounts(KeyPair.fromAccountId(accountId));
            if (controlledAccountsResponse == null || controlledAccountsResponse.getControlledAccounts() == null) {
                throw new OperationFailedException("can't retrieve data for account " + accountId);
            }

            for (kin.base.responses.ControlledAccountsResponse.ControlledAccount account :
                controlledAccountsResponse.getControlledAccounts()) {
                ControlledAccount controlledAccount = new ControlledAccount(
                    new BalanceImpl(new BigDecimal(account.getBalance())), account.getAccountId());
                controlledAccounts.add(controlledAccount);
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

        return controlledAccounts;
    }

    /**
     * Get the account data for the specified account.
     *
     * @param accountId the account ID to check balance
     * @return the account data {@link AccountData}
     * @throws AccountNotFoundException if account not created yet
     * @throws OperationFailedException any other error
     */
    AccountData getAccountData(@NonNull String accountId) throws OperationFailedException {
        Utils.checkNotNull(accountId, "account");
        AccountData accountData;

        try {
            AccountResponse accountResponse = server.accounts().account(KeyPair.fromAccountId(accountId));
            if (accountResponse == null) {
                throw new OperationFailedException("can't retrieve data for account " + accountId);
            }
            accountData = new AccountData(accountResponse.getKeypair().getAccountId(),
                accountResponse.getSequenceNumber(), accountResponse.getPagingToken(),
                accountResponse.getSubentryCount(), accountResponse.getThresholds(),
                accountResponse.getFlags(), accountResponse.getBalances(), accountResponse.getSigners(),
                accountResponse.getData());
        } catch (HttpResponseException httpError) {
            if (httpError.getStatusCode() == 404) {
                throw new AccountNotFoundException(accountId);
            } else {
                throw new OperationFailedException(httpError);
            }
        } catch (IOException e) {
            throw new OperationFailedException(e);
        }

        return accountData;
    }

    @AccountStatus
    int getStatus(@NonNull String accountId) throws OperationFailedException {
        try {
            getBalance(accountId);
            return AccountStatus.CREATED;
        } catch (AccountNotFoundException e) {
            return AccountStatus.NOT_CREATED;
        }
    }

}
