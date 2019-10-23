package kin.sdk;

import android.content.Context;
import android.support.annotation.NonNull;

import kin.base.Server;
import kin.sdk.internal.services.AccountInfoRetrieverImpl;
import kin.sdk.internal.services.GeneralBlockchainInfoRetrieverImpl;
import kin.sdk.internal.services.TransactionSenderImpl;
import kin.sdk.internal.storage.KeyStoreImpl;
import kin.sdk.internal.storage.SharedPrefStore;
import kin.sdk.internal.utils.BackupRestore;
import kin.sdk.internal.utils.BackupRestoreImpl;
import kin.sdk.internal.utils.BlockchainEventsCreator;

import static kin.sdk.internal.utils.Utils.checkNotNull;

/**
 * An account manager for a {@link KinAccount}.
 */
public final class KinClient extends KinClientInternal {

    private static final String STORE_NAME_PREFIX = "KinKeyStore_";

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
        this(context, environment, appId, storeKey, new Server(environment.getNetworkUrl(), new KinOkHttpClientFactory(BuildConfig.VERSION_NAME).client), new BackupRestoreImpl());
        checkNotNull(storeKey, "storeKey");
    }

    public KinClient(@NonNull Context context, @NonNull Environment environment, @NonNull String appId,
                     @NonNull String storeKey, @NonNull Server server, @NonNull BackupRestore backupRestore) {
        super(
                new KeyStoreImpl(
                        new SharedPrefStore(context.getSharedPreferences(STORE_NAME_PREFIX + appId, Context.MODE_PRIVATE)),
                        backupRestore
                ),
                environment,
                new TransactionSenderImpl(server, appId),
                new AccountInfoRetrieverImpl(server),
                new GeneralBlockchainInfoRetrieverImpl(server),
                new BlockchainEventsCreator(server),
                backupRestore,
                appId
        );
        this.storeKey = storeKey;
    }

    public String getStoreKey() {
        return storeKey;
    }
}
