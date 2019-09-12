package kin.sdk;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import kin.base.Network;
import kin.base.Server;
import kin.sdk.internal.backuprestore.BackupRestoreImpl;
import kin.sdk.internal.blockchain.AccountInfoRetriever;
import kin.sdk.internal.blockchain.GeneralBlockchainInfoRetrieverImpl;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.internal.blockchain.events.BlockchainEventsCreator;
import kin.sdk.internal.queue.PaymentQueueImpl;
import kin.sdk.internal.storage.KeyStore;
import kin.sdk.internal.storage.KeyStoreImpl;
import kin.sdk.internal.storage.SharedPrefStore;

import static kin.sdk.internal.Utils.checkNotNull;
import static kin.sdk.internal.Utils.validateAppId;

public class KinClientInjector {

    private static final long delayBetweenPaymentsMillis = 3000; // TODO: 2019-08-15 get those
    // number from somewhere
    private static final long queueTimeoutMillis = 10000;        // TODO: 2019-08-15 get those
    // number from somewhere
    private static final int maxNumOfPayments = 100;             // TODO: 2019-08-15 get those
    // number from somewhere
    private static final String STORE_NAME_PREFIX = "KinKeyStore_";
    private static final int TRANSACTIONS_TIMEOUT = 30;

    private Context context;
    private Environment environment;
    private String appId;
    private String storeKey;
    private Server server;
    private BackupRestoreImpl backupRestore;

    public KinClientInjector(Context context, Environment environment, String appId,
                             String storeKey) {
        checkNotNull(storeKey, "storeKey");
        checkNotNull(context, "context");
        checkNotNull(environment, "environment");
        validateAppId(appId);
        this.context = context;
        this.environment = environment;
        this.appId = appId;
        this.storeKey = storeKey;
    }

    KeyStore getKeyStore() {
        return initKeyStore(context.getApplicationContext(), storeKey);
    }

    TransactionSender getTransactionSender() {
        return new TransactionSender(getServer(), appId);
    }

    BackupRestoreImpl getBackupRestore() {
        if (backupRestore == null) {
            backupRestore = new BackupRestoreImpl();
        }
        return backupRestore;
    }

    AccountInfoRetriever getAccountInfoRetriever() {
        return new AccountInfoRetriever(getServer());
    }

    GeneralBlockchainInfoRetrieverImpl getGeneralBlockchainInfoRetriever() {
        return new GeneralBlockchainInfoRetrieverImpl(getServer());
    }

    BlockchainEventsCreator getBlockchainEventsCreator() {
        return new BlockchainEventsCreator(getServer());
    }

    PaymentQueueImpl.PaymentQueueConfiguration getPaymentQueueConfiguration() {
        return new PaymentQueueImpl.PaymentQueueConfiguration(delayBetweenPaymentsMillis,
                queueTimeoutMillis, maxNumOfPayments);
    }

    Server getServer() {
        if (server != null) {
            return server;
        } else {
            return initServer();
        }
    }

    void setServer(Server server) {
        this.server = server;
    }

    String getStoreNamePrefix() {
        return STORE_NAME_PREFIX;
    }

    int getTransactionTimeout() {
        return TRANSACTIONS_TIMEOUT;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public String getAppId() {
        return appId;
    }

    public String getStoreKey() {
        return storeKey;
    }

    private KeyStore initKeyStore(Context context, String id) {
        SharedPrefStore store = new SharedPrefStore(
                context.getSharedPreferences(getStoreNamePrefix() + id, Context.MODE_PRIVATE));
        return new KeyStoreImpl(store, getBackupRestore());
    }

    private Server initServer() {
        Network.use(environment.getNetwork());
        setServer(new Server(environment.getNetworkUrl(), getTransactionTimeout(),
                TimeUnit.SECONDS));
        return new Server(environment.getNetworkUrl(), getTransactionTimeout(), TimeUnit.SECONDS);
    }

}
