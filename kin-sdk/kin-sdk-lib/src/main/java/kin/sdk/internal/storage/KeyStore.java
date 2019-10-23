package kin.sdk.internal.storage;


import android.support.annotation.NonNull;

import java.util.List;

import kin.base.KeyPair;
import kin.sdk.exception.CorruptedDataException;
import kin.sdk.exception.CreateAccountException;
import kin.sdk.exception.CryptoException;
import kin.sdk.exception.DeleteAccountException;
import kin.sdk.exception.LoadAccountException;

public interface KeyStore {

    @NonNull
    List<KeyPair> loadAccounts() throws LoadAccountException;

    void deleteAccount(String publicAddress) throws DeleteAccountException;

    KeyPair newAccount() throws CreateAccountException;

    KeyPair importAccount(@NonNull String json, @NonNull String passphrase)
            throws CryptoException, CreateAccountException, CorruptedDataException;

    void clearAllAccounts();
}
