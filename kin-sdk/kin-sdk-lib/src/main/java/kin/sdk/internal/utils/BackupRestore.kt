package kin.sdk.internal.utils

import kin.base.KeyPair
import kin.sdk.exception.CorruptedDataException
import kin.sdk.exception.CryptoException
import kin.sdk.models.AccountBackup
import org.libsodium.jni.NaCl
import org.libsodium.jni.Sodium
import java.io.UnsupportedEncodingException

interface BackupRestore {

    @Throws(CryptoException::class)
    fun exportAccount(keyPair: KeyPair, passphrase: String): AccountBackup

    @Throws(CryptoException::class, CorruptedDataException::class)
    fun importAccount(accountBackup: AccountBackup, passphrase: String): KeyPair
}

internal class BackupRestoreImpl : BackupRestore {
    private companion object {
        private const val SALT_LENGTH_BYTES = 16
        private const val HASH_LENGTH_BYTES = 32

        init {
            try {
                NaCl.sodium()
            } catch (t: Throwable) {
                // do nothing
            }
        }

        private fun generateRandomBytes(len: Int): ByteArray =
            ByteArray(len).also { randomBuffer ->
                Sodium.randombytes_buf(
                    randomBuffer,
                    len
                )
            }

        @Throws(CryptoException::class)
        private fun keyHash(passphraseBytes: ByteArray, saltBytes: ByteArray): ByteArray =
            ByteArray(HASH_LENGTH_BYTES)
                .also { hash ->
                    val keyHashSuccess = Sodium.crypto_pwhash(
                        hash,
                        HASH_LENGTH_BYTES,
                        passphraseBytes,
                        passphraseBytes.size,
                        saltBytes,
                        Sodium.crypto_pwhash_opslimit_interactive(),
                        Sodium.crypto_pwhash_memlimit_interactive(),
                        Sodium.crypto_pwhash_alg_default()
                    ) == 0

                    if (!keyHashSuccess) {
                        throw CryptoException("Generating hash failed.")
                    }
                }

        @Throws(CryptoException::class)
        private fun decryptSecretSeed(seedBytes: ByteArray, keyHash: ByteArray): ByteArray {
            val nonceBytes = seedBytes.copyOfRange(0, Sodium.crypto_secretbox_noncebytes())
            val cipherBytes = seedBytes.copyOfRange(nonceBytes.size, seedBytes.size)

            val decryptedBytes = ByteArray(cipherBytes.size - Sodium.crypto_secretbox_macbytes())
            val decryptionSuccess =
                Sodium.crypto_secretbox_open_easy(
                    decryptedBytes,
                    cipherBytes,
                    cipherBytes.size,
                    nonceBytes,
                    keyHash
                ) == 0

            if (!decryptionSuccess) {
                throw CryptoException("Decrypting data failed.")
            }
            return decryptedBytes
        }

        @Throws(CryptoException::class)
        private fun encryptSecretSeed(hash: ByteArray, secretSeedBytes: ByteArray): ByteArray {
            val cipherText = ByteArray(secretSeedBytes.size + Sodium.crypto_secretbox_macbytes())
            val nonceBytes = Companion.generateRandomBytes(Sodium.crypto_secretbox_noncebytes())

            val encryptionSuccess =
                Sodium.crypto_secretbox_easy(
                    cipherText,
                    secretSeedBytes,
                    secretSeedBytes.size,
                    nonceBytes, hash
                ) == 0

            if (!encryptionSuccess) {
                throw CryptoException("Encrypting data failed.")
            }
            return nonceBytes + cipherText
        }
    }

    @Throws(CryptoException::class)
    override fun exportAccount(keyPair: KeyPair, passphrase: String): AccountBackup {
        val saltBytes = Companion.generateRandomBytes(SALT_LENGTH_BYTES)

        val passphraseBytes = passphrase.toUTF8ByteArray()
        val hash = Companion.keyHash(passphraseBytes, saltBytes)
        val secretSeedBytes = keyPair.rawSecretSeed

        val encryptedSeed = Companion.encryptSecretSeed(hash, secretSeedBytes)

        val salt = saltBytes.bytesToHex()
        val seed = encryptedSeed.bytesToHex()
        return AccountBackup(keyPair.accountId, salt, seed)
    }

    @Throws(CryptoException::class)
    override fun importAccount(accountBackup: AccountBackup, passphrase: String): KeyPair {
        val passphraseBytes = passphrase.toUTF8ByteArray()
        val saltBytes = accountBackup.saltHexString.hexStringToByteArray()
        val keyHash = Companion.keyHash(passphraseBytes, saltBytes)
        val seedBytes = accountBackup.encryptedSeedHexString.hexStringToByteArray()

        val decryptedBytes = Companion.decryptSecretSeed(seedBytes, keyHash)
        return KeyPair.fromSecretSeed(decryptedBytes)
    }

    @Throws(CryptoException::class)
    private fun String.toUTF8ByteArray(): ByteArray =
        try {
            toByteArray(charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            throw CryptoException(e)
        }
}
