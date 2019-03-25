package kin.sdk;


import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;

import kin.base.KeyPair;
import kin.sdk.exception.CryptoException;

public class FakeBackupRestore implements BackupRestore {

    @NotNull
    @Nonnull
    @Override
    public String exportWallet(@NotNull @Nonnull KeyPair keyPair, @NotNull @Nonnull String passphrase) throws CryptoException {
        JSONObject json = new JSONObject();
        try {
            json.put("seed", new String(keyPair.getSecretSeed()));
            json.put("passphrase", passphrase);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @NotNull
    @Nonnull
    @Override
    public KeyPair importWallet(@Nonnull String exportedJson, @Nonnull String passphrase) throws CryptoException {
        JSONObject json = new JSONObject();
        try {
            String seed = json.getString("seed");
            String storedPassphrase = json.getString("passphrase");
            if (storedPassphrase.equals(passphrase)) {
                return KeyPair.fromSecretSeed(seed);
            } else {
                throw new CryptoException("incorrect passohrase");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new CryptoException(e);
        }
    }
}
