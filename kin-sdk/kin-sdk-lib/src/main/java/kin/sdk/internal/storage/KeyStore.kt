package kin.sdk.internal.storage

import kin.base.KeyPair
import kin.sdk.exception.CorruptedDataException
import kin.sdk.exception.CreateAccountException
import kin.sdk.exception.CryptoException
import kin.sdk.exception.DeleteAccountException
import kin.sdk.exception.LoadAccountException
import kin.sdk.internal.utils.BackupRestore
import kin.sdk.internal.utils.forEachJsonObject
import kin.sdk.internal.utils.toJsonObjectList
import kin.sdk.models.AccountBackup
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

interface KeyStore {

    @Throws(LoadAccountException::class)
    fun loadAccounts(): List<KeyPair>

    @Throws(DeleteAccountException::class)
    fun deleteAccount(publicAddress: String)

    @Throws(CreateAccountException::class)
    fun newAccount(): KeyPair

    @Throws(CryptoException::class, CreateAccountException::class, CorruptedDataException::class)
    fun importAccount(json: String, passphrase: String): KeyPair

    fun clearAllAccounts()
}

internal open class KeyStoreImpl(private val store: Store, private val backupRestore: BackupRestore) : KeyStore {

    companion object {
        private const val ENCRYPTION_VERSION_NAME = "none"
        private const val STORE_KEY_ACCOUNTS = "accounts"
        private const val VERSION_KEY = "encryptor_ver"
        private const val JSON_KEY_ACCOUNTS_ARRAY = "accounts"
        private const val JSON_KEY_PUBLIC_KEY = "public_key"
        private const val JSON_KEY_ENCRYPTED_SEED = "seed"
    }

    @Throws(LoadAccountException::class)
    override fun loadAccounts(): List<KeyPair> {
        val accounts = ArrayList<KeyPair>()
        try {
            loadJsonArray()?.let { jsonArray ->
                jsonArray.forEachJsonObject { it.getString(JSON_KEY_ENCRYPTED_SEED) }
                    .mapTo(accounts) { KeyPair.fromSecretSeed(it) }
            }
        } catch (e: JSONException) {
            throw LoadAccountException(e.message, e)
        }

        return accounts
    }

    @Throws(JSONException::class)
    private fun loadJsonArray(): JSONArray? {
        val version = store.getString(VERSION_KEY)
        //ensure current version, drop data if it's a different version
        if (ENCRYPTION_VERSION_NAME == version) {
            val seedsJson = store.getString(STORE_KEY_ACCOUNTS)
            if (seedsJson != null) {
                val json = JSONObject(seedsJson)
                return json.getJSONArray(JSON_KEY_ACCOUNTS_ARRAY)
            }
        } else {
            store.clear(STORE_KEY_ACCOUNTS)
            store.saveString(VERSION_KEY, ENCRYPTION_VERSION_NAME)
        }
        return null
    }

    @Throws(DeleteAccountException::class)
    override fun deleteAccount(publicAddress: String) {
        val json = JSONObject()
        try {
            loadJsonArray()?.let { jsonArray ->
                json.put(JSON_KEY_ACCOUNTS_ARRAY, jsonArray.toJsonObjectList()
                    .filter { it.get(JSON_KEY_PUBLIC_KEY) != publicAddress })
            }
        } catch (e: JSONException) {
            throw DeleteAccountException(e)
        }

        store.saveString(STORE_KEY_ACCOUNTS, json.toString())
    }

    @Throws(CreateAccountException::class)
    override fun newAccount(): KeyPair {
        return addKeyPairToStorage(KeyPair.random())
    }

    @Throws(CreateAccountException::class)
    protected fun addKeyPairToStorage(newKeyPair: KeyPair): KeyPair {
        try {
            val encryptedSeed = String(newKeyPair.secretSeed)
            val publicKey = newKeyPair.accountId
            val accounts = store.getString(STORE_KEY_ACCOUNTS)
            if (accounts.isNullOrEmpty() || !accounts.contains(publicKey)) {
                val accountsJson = addKeyPairToAccountsJson(encryptedSeed, publicKey)
                store.saveString(STORE_KEY_ACCOUNTS, accountsJson.toString())
            }
            return newKeyPair
        } catch (e: JSONException) {
            throw CreateAccountException(e)
        }
    }

    @Throws(CryptoException::class, CreateAccountException::class, CorruptedDataException::class)
    override fun importAccount(json: String, passphrase: String): KeyPair {
        val keyPair = backupRestore.importAccount(AccountBackup(json), passphrase)
        return addKeyPairToStorage(keyPair)
    }

    @Throws(JSONException::class)
    private fun addKeyPairToAccountsJson(encryptedSeed: String, accountId: String): JSONObject {
        val jsonArray = (loadJsonArray() ?: JSONArray()).put(
            JSONObject().apply {
                put(JSON_KEY_ENCRYPTED_SEED, encryptedSeed)
                put(JSON_KEY_PUBLIC_KEY, accountId)
            })
        return JSONObject().apply {
            put(JSON_KEY_ACCOUNTS_ARRAY, jsonArray)
        }
    }

    override fun clearAllAccounts() {
        store.clear(STORE_KEY_ACCOUNTS)
    }
}
