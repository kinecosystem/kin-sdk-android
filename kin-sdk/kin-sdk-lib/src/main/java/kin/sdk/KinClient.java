package kin.sdk;

import android.content.Context;
import android.support.annotation.NonNull;

import kin.base.Server;
import kin.sdk.exception.CorruptedDataException;
import kin.sdk.exception.CreateAccountException;
import kin.sdk.exception.CryptoException;
import kin.sdk.exception.DeleteAccountException;
import kin.sdk.exception.OperationFailedException;
import kin.utils.Request;

import static kin.sdk.Utils.checkNotNull;

/**
 * An account manager for a {@link KinAccount}.
 */
public class KinClient {

    private static final String STORE_NAME_PREFIX = "KinKeyStore_";

    private final KinClientInternal kinClientInternal;
    private final String storeKey;

    /**
     * For more details please look at {@link #KinClient(Context context, Environment environment, String appId, String storeKey)}
     */
    public KinClient(@NonNull Context context, @NonNull Environment environment, String appId) {
        this(context, environment, appId, "");
    }

    /**
     * Build KinClient object.
     *
     * @param context     android context
     * @param environment the blockchain network details.
     * @param appId       a 4 character string which represent the application id which will be added to each transaction.
     *                    <br><b>Note:</b> appId must contain only upper and/or lower case letters and/or digits and that the total string length is between 3 to 4.
     *                    For example 1234 or 2ab3 or bcda, etc.</br>
     * @param storeKey    an optional param which is the key for storing this KinClient data, different keys will store a different accounts.
     */
    public KinClient(@NonNull Context context, @NonNull Environment environment, @NonNull String appId, @NonNull String storeKey) {
        checkNotNull(storeKey, "storeKey");
        this.storeKey = storeKey;

        BackupRestore backupRestore = new BackupRestoreImpl();

        // Init Server
        Server server = new Server(environment.getNetworkUrl(), new KinOkHttpClientFactory(BuildConfig.VERSION_NAME).client);

        kinClientInternal = new KinClientInternal(
                createKeyStore(context, storeKey, backupRestore),
                environment,
                new TransactionSender(server, appId),
                new AccountInfoRetriever(server),
                new GeneralBlockchainInfoRetrieverImpl(server),
                new BlockchainEventsCreator(server),
                backupRestore,
                appId
        );
    }

    private KeyStore createKeyStore(Context context, String id, BackupRestore backupRestore) {
        SharedPrefStore store = new SharedPrefStore(context.getSharedPreferences(STORE_NAME_PREFIX + id, Context.MODE_PRIVATE));
        return new KeyStoreImpl(store, backupRestore);
    }

    /**
     * Creates and adds an account.
     * <p>Once created, the account information will be stored securely on the device and can
     * be accessed again via the {@link #getAccount(int)} method.</p>
     *
     * @return {@link KinAccount} the account created store the key.
     */
    public @NonNull
    KinAccount addAccount() throws CreateAccountException {
        return kinClientInternal.addAccount();
    }

    /**
     * Import an account from a JSON-formatted string.
     *
     * @param exportedJson The exported JSON-formatted string.
     * @param passphrase   The passphrase to decrypt the secret key.
     * @return The imported account
     */
    @NonNull
    public KinAccount importAccount(@NonNull String exportedJson, @NonNull String passphrase)
            throws CryptoException, CreateAccountException, CorruptedDataException {
        return kinClientInternal.importAccount(exportedJson, passphrase);
    }

    /**
     * Returns an account at input index.
     *
     * @return the account at the input index or null if there is no such account
     */
    public KinAccount getAccount(int index) {
        return kinClientInternal.getAccount(index);
    }

    /**
     * @return true if there is an existing account
     */
    public boolean hasAccount() {
        return kinClientInternal.hasAccount();
    }

    /**
     * Returns the number of existing accounts
     */
    public int getAccountCount() {
        return kinClientInternal.getAccountCount();
    }

    /**
     * Deletes the account at input index (if it exists)
     *
     * @return true if the delete was successful or false otherwise
     * @throws DeleteAccountException in case of a delete account exception while trying to delete the account
     */
    public boolean deleteAccount(int index) throws DeleteAccountException {
        return kinClientInternal.deleteAccount(index);
    }

    /**
     * Deletes all accounts.
     */
    public void clearAllAccounts() {
        kinClientInternal.clearAllAccounts();
    }

    public Environment getEnvironment() {
        return kinClientInternal.getEnvironment();
    }

    /**
     * Get the current minimum fee that the network charges per operation.
     * This value is expressed in stroops.
     *
     * @return {@code Request<Integer>} - the minimum fee.
     */
    public Request<Long> getMinimumFee() {
        return kinClientInternal.getMinimumFee();
    }

    /**
     * Get the current minimum fee that the network charges per operation.
     * This value is expressed in stroops.
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @return the minimum fee.
     */
    public long getMinimumFeeSync() throws OperationFailedException {
        return kinClientInternal.getMinimumFeeSync();
    }

    public String getAppId() {
        return kinClientInternal.getAppId();
    }

    public String getStoreKey() {
        return storeKey;
    }
}
