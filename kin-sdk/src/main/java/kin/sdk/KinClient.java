package kin.sdk;

import javax.annotation.Nonnull;

import kin.sdk.exception.CorruptedDataException;
import kin.sdk.exception.CreateAccountException;
import kin.sdk.exception.CryptoException;
import kin.sdk.exception.DeleteAccountException;
import kin.sdk.exception.OperationFailedException;
import kin.utils.Request;

public interface KinClient
{
    /**
     * Creates and adds an account.
     * <p>Once created, the account information will be stored securely on the device and can
     * be accessed again via the {@link #getAccount(int)} method.</p>
     *
     * @return {@link KinAccount} the account created store the key.
     */
    @Nonnull
    KinAccount addAccount() throws CreateAccountException;

    /**
     * Import an account from a JSON-formatted string.
     *
     * @param exportedJson The exported JSON-formatted string.
     * @param passphrase The passphrase to decrypt the secret key.
     * @return The imported account
     */
    @Nonnull
    KinAccount importAccount(@Nonnull String exportedJson, @Nonnull String passphrase)
        throws CryptoException, CreateAccountException, CorruptedDataException;

    /**
     * Returns an account at input index.
     *
     * @return the account at the input index or null if there is no such account
     */
    KinAccount getAccount(int index);

    /**
     * @return true if there is an existing account
     */
    boolean hasAccount();

    /**
     * Returns the number of existing accounts
     */
    int getAccountCount();

    /**
     * Deletes the account at input index (if it exists)
     */
    void deleteAccount(int index) throws DeleteAccountException;

    /**
     * Deletes all accounts.
     */
    void clearAllAccounts();

    Environment getEnvironment();

    /**
     * Get the current minimum fee that the network charges per operation.
     * This value is expressed in stroops.
     *
     * @return {@code Request<Integer>} - the minimum fee.
     */
    Request<Long> getMinimumFee();

    /**
     * Get the current minimum fee that the network charges per operation.
     * This value is expressed in stroops.
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @return the minimum fee.
     */
    long getMinimumFeeSync() throws OperationFailedException;
}
