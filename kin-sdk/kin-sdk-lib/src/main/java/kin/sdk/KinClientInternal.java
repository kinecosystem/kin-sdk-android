package kin.sdk;

import static kin.sdk.Utils.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import kin.base.KeyPair;
import kin.base.Network;
import kin.base.Server;
import kin.sdk.exception.CorruptedDataException;
import kin.sdk.exception.CreateAccountException;
import kin.sdk.exception.CryptoException;
import kin.sdk.exception.DeleteAccountException;
import kin.sdk.exception.LoadAccountException;
import kin.sdk.exception.OperationFailedException;
import kin.utils.Request;

/**
 * An account manager for a {@link KinAccount}.
 */
class KinClientInternal {

    private final Environment environment;
    private final KeyStore keyStore;
    private final TransactionSender transactionSender;
    private final AccountInfoRetriever accountInfoRetriever;
    private final GeneralBlockchainInfoRetrieverImpl generalBlockchainInfoRetriever;
    private final BlockchainEventsCreator blockchainEventsCreator;
    private final BackupRestore backupRestore;
    private final String appId;
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());

    @NonNull
    private List<KinAccountImpl> kinAccounts = new ArrayList<>(1);

    /**
     * Build KinClient object.
     * @param keyStore a place to store keys
     * @param environment the blockchain network details.
     * @param appId a 4 character string which represent the application id which will be added to each transaction.
     *              <br><b>Note:</b> appId must contain only upper and/or lower case letters and/or digits and that the total string length is between 3 to 4.
     *              For example 1234 or 2ab3 or bcda, etc.</br>
     */
    public KinClientInternal(@NonNull KeyStore keyStore, @NonNull Environment environment, @NonNull String appId, @NonNull BackupRestore backupRestore) {
        checkNotNull(keyStore, "keyStore");
        checkNotNull(environment, "environment");
        validateAppId(appId);
        this.environment = environment;
        this.backupRestore = backupRestore;
        Server server = initServer();
        this.appId = appId;
        this.keyStore = keyStore;
        transactionSender = new TransactionSender(server, appId);
        accountInfoRetriever = new AccountInfoRetriever(server);
        generalBlockchainInfoRetriever = new GeneralBlockchainInfoRetrieverImpl(server);
        blockchainEventsCreator = new BlockchainEventsCreator(server);
        loadAccounts();
    }

    @VisibleForTesting
    KinClientInternal(Environment environment, KeyStore keyStore, TransactionSender transactionSender,
        AccountInfoRetriever accountInfoRetriever, GeneralBlockchainInfoRetrieverImpl generalBlockchainInfoRetriever,
        BlockchainEventsCreator blockchainEventsCreator, BackupRestore backupRestore, String appId) {
        this.environment = environment;
        this.keyStore = keyStore;
        this.transactionSender = transactionSender;
        this.accountInfoRetriever = accountInfoRetriever;
        this.generalBlockchainInfoRetriever = generalBlockchainInfoRetriever;
        this.blockchainEventsCreator = blockchainEventsCreator;
        this.backupRestore = backupRestore;
        this.appId = appId;
        loadAccounts();
    }

    private Server initServer() {
        Network.use(environment.getNetwork());
        return new Server(environment.getNetworkUrl(), new KinOkHttpClientFactory(BuildConfig.VERSION_NAME).client);
    }

    private void loadAccounts() {
        List<KeyPair> accounts = null;
        try {
            accounts = keyStore.loadAccounts();
        } catch (LoadAccountException e) {
            e.printStackTrace();
        }
        if (accounts != null && !accounts.isEmpty()) {
            updateKinAccounts(accounts);
        }
    }

    private void updateKinAccounts(List<KeyPair> storageAccounts) {
        Map<String, KinAccountImpl> accountsMap = new HashMap<>();
        for (KinAccountImpl kinAccountImpl : kinAccounts) {
            accountsMap.put(kinAccountImpl.getPublicAddress(), kinAccountImpl);
        }

        List<KinAccountImpl> newKinAccountsList = new ArrayList<>();
        for (KeyPair account : storageAccounts) {
            KinAccountImpl inMemoryKinAccount = accountsMap.get(account.getAccountId());
            if (inMemoryKinAccount != null) {
                newKinAccountsList.add(inMemoryKinAccount);
            } else {
                newKinAccountsList.add(createNewKinAccount(account));
            }
        }
        kinAccounts = newKinAccountsList;
    }

    private void validateAppId(String appId) {
        if (appId == null || appId.equals("")) {
            logger.warning("WARNING: KinClient instance was created without a proper application ID. Is this what you intended to do?");
        }
        else if (!appId.matches("[a-zA-Z0-9]{3,4}")) {
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
    public @NonNull
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
    @NonNull
    public KinAccount importAccount(@NonNull String exportedJson, @NonNull String passphrase)
        throws CryptoException, CreateAccountException, CorruptedDataException {
        KeyPair account = keyStore.importAccount(exportedJson, passphrase);
        KinAccount kinAccount = getAccountByPublicAddress(account.getAccountId());
        return kinAccount != null ? kinAccount : addKeyPair(account);
    }

    @Nullable
    private KinAccount getAccountByPublicAddress(String accountId) { //TODO we should make this method public
        loadAccounts();
        KinAccount kinAccount = null;
        for (int i = 0; i < kinAccounts.size(); i++) {
            final KinAccount account = kinAccounts.get(i);
            if (accountId.equals(account.getPublicAddress())) {
                kinAccount = account;
            }
        }
        return kinAccount;
    }

    @NonNull
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
        loadAccounts();
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
        loadAccounts();
        return kinAccounts.size();
    }

    /**
     * Deletes the account at input index (if it exists)
     * @return true if the delete was successful or false otherwise
     * @throws DeleteAccountException in case of a delete account exception while trying to delete the account
     */
    public boolean deleteAccount(int index) throws DeleteAccountException {
        boolean deleteSuccess = false;
        if (index >= 0 && getAccountCount() > index) {
            String accountToDelete = kinAccounts.get(index).getPublicAddress();
            keyStore.deleteAccount(accountToDelete);
            KinAccountImpl removedAccount = kinAccounts.remove(index);
            removedAccount.markAsDeleted();
            deleteSuccess = true;
        }
        return deleteSuccess;
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

    @NonNull
    private KinAccountImpl createNewKinAccount(KeyPair account) {
        return new KinAccountImpl(account, backupRestore, transactionSender,
                accountInfoRetriever, blockchainEventsCreator);
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
        });
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

    public String getAppId() {
        return appId;
    }
}
