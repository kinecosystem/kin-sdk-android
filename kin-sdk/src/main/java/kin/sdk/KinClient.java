package kin.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import kin.base.KeyPair;
import kin.base.Network;
import kin.base.Server;
import kin.sdk.exception.CorruptedDataException;
import kin.sdk.exception.CreateAccountException;
import kin.sdk.exception.CryptoException;
import kin.sdk.exception.DeleteAccountException;
import kin.sdk.exception.LoadAccountException;
import kin.sdk.exception.OperationFailedException;
import kin.utils.MainHandler;
import kin.utils.Request;

import static kin.sdk.Utils.checkNonnull;
import static kin.sdk.Utils.checkNotEmpty;

/**
 * An account manager for a {@link KinAccount}.
 */
public class KinClient
{
    private static final int TRANSACTIONS_TIMEOUT = 30;
    private final Environment environment;
    private final KeyStore keyStore;
    private final TransactionSender transactionSender;
    private final AccountInfoRetriever accountInfoRetriever;
    private final GeneralBlockchainInfoRetrieverImpl generalBlockchainInfoRetriever;
    private final BlockchainEventsCreator blockchainEventsCreator;
    private final BackupRestore backupRestore;
    @Nonnull
    private final List<KinAccountImpl> kinAccounts = new ArrayList<>(1);
    private final KinStorageFactory kinStorageFactory;
    private final MainHandler mainHandler;

    /**
     * For more details please look at {@link #KinClient(KinStorageFactory kinStorageFactory, MainHandler mainHandler, Environment environment,String appId, String storeKey)}
     */
    public KinClient(@Nonnull KinStorageFactory kinStorageFactory,
            @Nonnull MainHandler mainHandler,
            @Nonnull Environment environment,
            String appId) {
        this(kinStorageFactory, mainHandler, environment, appId,"");
    }

    /**
     * Build KinClient object.
     * @param kinStorageFactory factory to get required storage for the kin sdk
     * @param mainHandler provides access to the platform's main thread
     * @param environment the blockchain network details.
     * @param appId a 4 character string which represent the application id which will be added to each transaction.
     *              <br><b>Note:</b> appId must contain only upper and/or lower case letters and/or digits and that the total string length is between 3 to 4.
     *              For example 1234 or 2ab3 or bcda, etc.</br>
     * @param storeKey an optional param which is the key for storing this KinClient data, different keys will store a different accounts.
     */
    public KinClient(@Nonnull KinStorageFactory kinStorageFactory,
            @Nonnull MainHandler mainHandler,
            @Nonnull Environment environment,
            @Nonnull String appId,
            @Nonnull String storeKey) {
        checkNonnull(storeKey, "storeKey");
        checkNonnull(kinStorageFactory, "context");
        checkNonnull(environment, "environment");
        validateAppId(appId);
        this.environment = environment;
        this.backupRestore = new BackupRestoreImpl();
        Server server = initServer();
        this.kinStorageFactory = kinStorageFactory;
        this.mainHandler = mainHandler;
        keyStore = new KeyStoreImpl(kinStorageFactory.getStore(StoreType.KEY_STORE, storeKey), backupRestore);
        transactionSender = new TransactionSender(server, appId);
        accountInfoRetriever = new AccountInfoRetriever(server);
        generalBlockchainInfoRetriever = new GeneralBlockchainInfoRetrieverImpl(server);
        blockchainEventsCreator = new BlockchainEventsCreator(server);
        loadAccounts();
    }

    // VisibleForTesting
    KinClient(Environment environment,
            KeyStore keyStore,
            TransactionSender transactionSender,
            AccountInfoRetriever accountInfoRetriever,
            GeneralBlockchainInfoRetrieverImpl generalBlockchainInfoRetriever,
            BlockchainEventsCreator blockchainEventsCreator,
            BackupRestore backupRestore,
            KinStorageFactory kinStorageFactory,
            MainHandler mainHandler) {
        this.environment = environment;
        this.keyStore = keyStore;
        this.transactionSender = transactionSender;
        this.accountInfoRetriever = accountInfoRetriever;
        this.generalBlockchainInfoRetriever = generalBlockchainInfoRetriever;
        this.blockchainEventsCreator = blockchainEventsCreator;
        this.backupRestore = backupRestore;
        this.kinStorageFactory = kinStorageFactory;
        this.mainHandler = mainHandler;
        loadAccounts();
    }

    private Server initServer() {
        Network.use(environment.getNetwork());
        return new Server(environment.getNetworkUrl(), TRANSACTIONS_TIMEOUT, TimeUnit.SECONDS);
    }

    private void loadAccounts() {
        List<KeyPair> accounts = null;
        try {
            accounts = keyStore.loadAccounts();
        } catch (LoadAccountException e) {
            e.printStackTrace();
        }
        if (accounts != null && !accounts.isEmpty()) {
            for (KeyPair account : accounts) {
                kinAccounts.add(createNewKinAccount(account));
            }
        }
    }

    private void validateAppId(String appId) {
        checkNotEmpty(appId, "appId");
        if (!appId.matches("[a-zA-Z0-9]{3,4}")) {
            throw new IllegalArgumentException("appId must contain only upper and/or lower case letters and/or digits and that the total string length is between 3 to 4.\n" +
                    "for example 1234 or 2ab3 or cd2 or fqa, etc.");
        }
    }

    /**
     * Creates and adds an account.
     * <p>Once created, the account information will be stored securely on the device and can
     * be accessed again via the {@link #getAccount(int)} method.</p>
     *
     * @return {@link KinAccount} the account created store the key.
     */
    public @Nonnull
    KinAccount addAccount() throws CreateAccountException {
        KeyPair account = keyStore.newAccount();
        return addKeyPair(account);
    }

    /**
     * Import an account from a JSON-formatted string.
     *
     * @param exportedJson The exported JSON-formatted string.
     * @param passphrase The passphrase to decrypt the secret key.
     * @return The imported account
     */
    public @Nonnull
    KinAccount importAccount(@Nonnull String exportedJson, @Nonnull String passphrase)
        throws CryptoException, CreateAccountException, CorruptedDataException {
        KeyPair account = keyStore.importAccount(exportedJson, passphrase);
        return addKeyPair(account);
    }

    @Nonnull
    private KinAccount addKeyPair(KeyPair account) {
        KinAccountImpl newAccount = createNewKinAccount(account);
        kinAccounts.add(newAccount);
        return newAccount;
    }

    /**
     * Returns an account at input index.
     *
     * @return the account at the input index or null if there is no such account
     */
    public KinAccount getAccount(int index) {
        if (index >= 0 && kinAccounts.size() > index) {
            return kinAccounts.get(index);
        }
        return null;
    }

    /**
     * @return true if there is an existing account
     */
    public boolean hasAccount() {
        return getAccountCount() != 0;
    }

    /**
     * Returns the number of existing accounts
     */
    public int getAccountCount() {
        return kinAccounts.size();
    }

    /**
     * Deletes the account at input index (if it exists)
     */
    public void deleteAccount(int index) throws DeleteAccountException {
        if (index >= 0 && getAccountCount() > index) {
            keyStore.deleteAccount(index);
            KinAccountImpl removedAccount = kinAccounts.remove(index);
            removedAccount.markAsDeleted();
        }
    }

    /**
     * Deletes all accounts.
     */
    public void clearAllAccounts() {
        keyStore.clearAllAccounts();
        for (KinAccountImpl kinAccount : kinAccounts) {
            kinAccount.markAsDeleted();
        }
        kinAccounts.clear();
    }

    public Environment getEnvironment() {
        return environment;
    }

    @Nonnull
    private KinAccountImpl createNewKinAccount(KeyPair account) {
        return new KinAccountImpl(account, backupRestore, transactionSender,
                accountInfoRetriever, blockchainEventsCreator, mainHandler);
    }

    /**
     * Get the current minimum fee that the network charges per operation.
     * This value is expressed in stroops.
     *
     * @return {@code Request<Integer>} - the minimum fee.
     */
    public Request<Long> getMinimumFee() {
        return new Request<>(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return generalBlockchainInfoRetriever.getMinimumFeeSync();
            }
        }, mainHandler);
    }

    /**
     * Get the current minimum fee that the network charges per operation.
     * This value is expressed in stroops.
     * <p><b>Note:</b> This method accesses the network, and should not be called on the android main thread.</p>
     *
     * @return the minimum fee.
     */
    public long getMinimumFeeSync() throws OperationFailedException {
        return generalBlockchainInfoRetriever.getMinimumFeeSync();
    }

}
