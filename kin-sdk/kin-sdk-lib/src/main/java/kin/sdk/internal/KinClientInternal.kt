package kin.sdk.internal

import kin.base.KeyPair
import kin.base.Network
import kin.sdk.Environment
import kin.sdk.KinAccount
import kin.sdk.exception.CorruptedDataException
import kin.sdk.exception.CreateAccountException
import kin.sdk.exception.CryptoException
import kin.sdk.exception.DeleteAccountException
import kin.sdk.exception.LoadAccountException
import kin.sdk.exception.OperationFailedException
import kin.sdk.internal.services.AccountInfoRetriever
import kin.sdk.internal.services.GeneralBlockchainInfoRetriever
import kin.sdk.internal.services.TransactionSender
import kin.sdk.internal.storage.KeyStore
import kin.sdk.internal.utils.BackupRestore
import kin.sdk.internal.utils.BlockchainEventsCreator
import kin.utils.Request
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.Callable
import java.util.logging.Logger

/**
 * An account manager for a [KinAccount].
 */
open class KinClientInternal
/**
 * Build KinClient object.
 *
 * @param keyStore    a place to store keys
 * @param environment the blockchain network details.
 * @param appId       a 4 character string which represent the application id which will be added to each transaction.
 * <br></br>**Note:** appId must contain only upper and/or lower case letters and/or digits and that the total string
 * length is between 3 to 4.
 * For example 1234 or 2ab3 or bcda, etc.
 */
(
    private val keyStore: KeyStore,
    val environment: Environment,
    private val transactionSender: TransactionSender,
    private val accountInfoRetriever: AccountInfoRetriever,
    private val generalBlockchainInfoRetriever: GeneralBlockchainInfoRetriever,
    private val blockchainEventsCreator: BlockchainEventsCreator,
    private val backupRestore: BackupRestore,
    val appId: String
) {
    private val logger = Logger.getLogger(javaClass.simpleName)

    private var kinAccounts: MutableList<KinAccountImpl> = ArrayList(1)

    /**
     * Returns the number of existing accounts
     */
    val accountCount: Int
        get() {
            loadAccounts()
            return kinAccounts.size
        }

    /**
     * Get the current minimum fee that the network charges per operation. This value is expressed in stroops.
     *
     * @return `Request<Integer>` - the minimum fee.
     */
    val minimumFee: Request<Long>
        get() = Request(Callable { generalBlockchainInfoRetriever.minimumFeeSync })

    /**
     * Get the current minimum fee that the network charges per operation. This value is expressed in stroops.
     *
     * **Note:** This method accesses the network, and should not be called on the android main thread.
     *
     * @return the minimum fee.
     */
    val minimumFeeSync: Long
        @Throws(OperationFailedException::class)
        get() = generalBlockchainInfoRetriever.minimumFeeSync

    init {
        validateAppId(appId)
        Network.use(environment.network)
        loadAccounts()
    }

    private fun loadAccounts() {
        var accounts: List<KeyPair>? = null
        try {
            accounts = keyStore.loadAccounts()
        } catch (e: LoadAccountException) {
            e.printStackTrace()
        }

        if (accounts != null && !accounts.isEmpty()) {
            updateKinAccounts(accounts)
        }
    }

    private fun updateKinAccounts(storageAccounts: List<KeyPair>) {
        val accountsMap = HashMap<String, KinAccountImpl>()
        for (kinAccountImpl in kinAccounts) {
            accountsMap[kinAccountImpl.publicAddress!!] = kinAccountImpl
        }

        val newKinAccountsList = ArrayList<KinAccountImpl>()
        for (account in storageAccounts) {
            val inMemoryKinAccount = accountsMap[account.accountId]
            if (inMemoryKinAccount != null) {
                newKinAccountsList.add(inMemoryKinAccount)
            } else {
                newKinAccountsList.add(createNewKinAccount(account))
            }
        }
        kinAccounts = newKinAccountsList
    }

    private fun validateAppId(appId: String) {
        if (appId == "") {
            logger.warning(
                "WARNING: KinClient instance was created without a proper application ID. Is this what you intended to do?")
        } else require(appId.matches("[a-zA-Z0-9]{3,4}".toRegex())) { "appId must contain only upper and/or lower case letters and/or digits and that the total string length is between 3 to 4.\n" + "for example 1234 or 2ab3 or cd2 or fqa, etc." }
    }

    /**
     * Creates and adds an account.
     *
     * Once created, the account information will be stored securely on the device and can
     * be accessed again via the [.getAccount] method.
     *
     * @return [KinAccount] the account created store the key.
     */
    @Throws(CreateAccountException::class)
    fun addAccount(): KinAccount {
        val account = keyStore.newAccount()
        return addKeyPair(account)
    }

    /**
     * Import an account from a JSON-formatted string.
     *
     * @param exportedJson The exported JSON-formatted string.
     * @param passphrase   The passphrase to decrypt the secret key.
     * @return The imported account
     */
    @Throws(CryptoException::class, CreateAccountException::class, CorruptedDataException::class)
    fun importAccount(exportedJson: String, passphrase: String): KinAccount {
        val account = keyStore.importAccount(exportedJson, passphrase)
        val kinAccount = getAccountByPublicAddress(account.accountId)
        return kinAccount ?: addKeyPair(account)
    }

    private fun getAccountByPublicAddress(accountId: String): KinAccount? { //TODO we should make this method public
        loadAccounts()
        var kinAccount: KinAccount? = null
        for (i in kinAccounts.indices) {
            val account = kinAccounts[i]
            if (accountId == account.publicAddress) {
                kinAccount = account
            }
        }
        return kinAccount
    }

    private fun addKeyPair(account: KeyPair): KinAccount {
        val newAccount = createNewKinAccount(account)
        kinAccounts.add(newAccount)
        return newAccount
    }

    /**
     * Returns an account at input index.
     *
     * @return the account at the input index or null if there is no such account
     */
    fun getAccount(index: Int): KinAccount? {
        loadAccounts()
        return if (index >= 0 && kinAccounts.size > index) {
            kinAccounts[index]
        } else null
    }

    /**
     * @return true if there is an existing account
     */
    fun hasAccount(): Boolean {
        return accountCount != 0
    }

    /**
     * Deletes the account at input index (if it exists)
     *
     * @return true if the delete was successful or false otherwise
     * @throws DeleteAccountException in case of a delete account exception while trying to delete the account
     */
    @Throws(DeleteAccountException::class)
    fun deleteAccount(index: Int): Boolean {
        var deleteSuccess = false
        if (index in 0..(accountCount - 1)) {
            kinAccounts[index].publicAddress?.let {
                keyStore.deleteAccount(it)
                val removedAccount = kinAccounts.removeAt(index)
                removedAccount.markAsDeleted()
                deleteSuccess = true
            }
        }
        return deleteSuccess
    }

    /**
     * Deletes all accounts.
     */
    fun clearAllAccounts() {
        keyStore.clearAllAccounts()
        for (kinAccount in kinAccounts) {
            kinAccount.markAsDeleted()
        }
        kinAccounts.clear()
    }

    private fun createNewKinAccount(account: KeyPair): KinAccountImpl {
        return KinAccountImpl(account, backupRestore, transactionSender,
            accountInfoRetriever, blockchainEventsCreator)
    }
}
