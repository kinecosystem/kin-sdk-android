package kin.sdk;


import android.support.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import kin.base.KeyPair;
import kin.sdk.exception.CorruptedDataException;
import kin.sdk.exception.CryptoException;
import kin.sdk.internal.utils.BackupRestore;
import kin.sdk.models.AccountBackup;

public class FakeBackupRestore implements BackupRestore {

    @NonNull
    @Override
    public AccountBackup exportAccount(@NonNull KeyPair keyPair, @NonNull String passphrase) throws CryptoException {
        JSONObject json = new JSONObject();
        try {
            json.put("seed", new String(keyPair.getSecretSeed()));
            json.put("passphrase", passphrase);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new AccountBackup(json.toString());
    }

    @NonNull
    @Override
    public KeyPair importAccount(@NotNull AccountBackup accountBackup, @NotNull String passphrase) throws CryptoException, CorruptedDataException {
        JSONObject json = new JSONObject();
        try {
            String seed = json.getString("seed");
            String storedPassphrase = json.getString("passphrase");
            if (storedPassphrase.equals(passphrase)) {
                return KeyPair.fromSecretSeed(seed);
            } else {
                throw new CryptoException("incorrect passphrase");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new CryptoException(e);
        }
    }
}
