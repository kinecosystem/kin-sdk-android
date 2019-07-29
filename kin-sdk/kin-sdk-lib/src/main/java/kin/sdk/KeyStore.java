package kin.sdk;

import androidx.annotation.NonNull;
import kin.base.KeyPair;
import kin.sdk.exception.*;

import java.util.List;

interface KeyStore {

    @NonNull
    List<KeyPair> loadAccounts() throws LoadAccountException;

    void deleteAccount(String publicAddress) throws DeleteAccountException;

    KeyPair newAccount() throws CreateAccountException;

    KeyPair importAccount(@NonNull String json, @NonNull String passphrase)
        throws CryptoException, CreateAccountException, CorruptedDataException;

    void clearAllAccounts();
}
