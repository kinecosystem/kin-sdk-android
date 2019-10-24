package kin.sdk

import android.content.Context
import kin.base.Server
import kin.sdk.exception.CorruptedDataException
import kin.sdk.exception.CreateAccountException
import kin.sdk.exception.CryptoException
import kin.sdk.exception.DeleteAccountException
import kin.sdk.internal.KinClientInternal
import kin.sdk.internal.KinOkHttpClientFactory
import kin.sdk.internal.services.AccountInfoRetrieverImpl
import kin.sdk.internal.services.GeneralBlockchainInfoRetrieverImpl
import kin.sdk.internal.services.TransactionSenderImpl
import kin.sdk.internal.storage.KeyStoreImpl
import kin.sdk.internal.storage.SharedPrefStore
import kin.sdk.internal.utils.BackupRestore
import kin.sdk.internal.utils.BackupRestoreImpl
import kin.sdk.internal.utils.BlockchainEventsCreatorImpl
import kin.utils.Request

interface IKinClient {
    val storeKey: String
    val environment: Environment
    val appId: String

    /**
     * Returns the number of existing accounts
     */
    val accountCount: Int
    /**
     * Get the current minimum fee that the network charges per operation. This value is expressed in stroops.
     *
     * @return `Request<Integer>` - the minimum fee.
     */
    val minimumFee: Request<Long>
    /**
     * Get the current minimum fee that the network charges per operation. This value is expressed in stroops.
     *
     * **Note:** This method accesses the network, and should not be called on the android main thread.
     *
     * @return the minimum fee.
     */
    val minimumFeeSync: Long

    /**
     * Creates and adds an account.
     *
     * Once created, the account information will be stored securely on the device and can
     * be accessed again via the [.getAccount] method.
     *
     * @return [KinAccount] the account created store the key.
     */
    @Throws(CreateAccountException::class)
    fun addAccount(): KinAccount

    /**
     * Import an account from a JSON-formatted string.
     *
     * @param exportedJson The exported JSON-formatted string.
     * @param passphrase   The passphrase to decrypt the secret key.
     * @return The imported account
     */
    @Throws(CryptoException::class, CreateAccountException::class, CorruptedDataException::class)
    fun importAccount(exportedJson: String, passphrase: String): KinAccount

    /**
     * Returns an account at input index.
     *
     * @return the account at the input index or null if there is no such account
     */
    fun getAccount(index: Int): KinAccount?

    /**
     * @return true if there is an existing account
     */
    fun hasAccount(): Boolean

    /**
     * Deletes the account at input index (if it exists)
     *
     * @return true if the delete was successful or false otherwise
     * @throws DeleteAccountException in case of a delete account exception while trying to delete the account
     */
    @Throws(DeleteAccountException::class)
    fun deleteAccount(index: Int): Boolean

    /**
     * Deletes all accounts.
     */
    fun clearAllAccounts()
}

/**
 * An account manager for a [KinAccount].
 */
class KinClient(
    context: Context,
    environment: Environment,
    appId: String,
    override val storeKey: String,
    server: Server,
    backupRestore: BackupRestore
) : IKinClient, KinClientInternal(
    KeyStoreImpl(
        SharedPrefStore(context.getSharedPreferences(STORE_NAME_PREFIX + appId, Context.MODE_PRIVATE)),
        backupRestore
    ),
    environment,
    TransactionSenderImpl(server, appId), AccountInfoRetrieverImpl(server), GeneralBlockchainInfoRetrieverImpl(server),
    BlockchainEventsCreatorImpl(server),
    backupRestore,
    appId
) {
    companion object {
        private const val STORE_NAME_PREFIX = "KinKeyStore_"
    }

    /**
     * Build KinClient object.
     *
     * @param context     android context
     * @param environment the blockchain network details.
     * @param appId       a 4 character string which represent the application id which will be added to each transaction.
     * <br></br>**Note:** appId must contain only upper and/or lower case letters and/or digits and that the total string length is between 3 to 4.
     * For example 1234 or 2ab3 or bcda, etc.
     * @param storeKey    an optional param which is the key for storing this KinClient data, different keys will store a different accounts.
     */
    @JvmOverloads
    constructor(
        context: Context,
        environment: Environment,
        appId: String,
        storeKey: String = ""
    ) : this(
        context,
        environment,
        appId,
        storeKey,
        Server(environment.networkUrl, KinOkHttpClientFactory(BuildConfig.VERSION_NAME).client),
        BackupRestoreImpl()
    )
}

