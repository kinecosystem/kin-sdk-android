package kin.sdk.exception;


import javax.annotation.Nonnull;

/**
 * Account was not created on the blockchain
 */
public class AccountNotFoundException extends OperationFailedException {

    private final String accountId;

    public AccountNotFoundException(@Nonnull String accountId) {
        super("Account " + accountId + " was not found");
        this.accountId = accountId;
    }

    @Nonnull
    public String getAccountId() {
        return accountId;
    }
}
