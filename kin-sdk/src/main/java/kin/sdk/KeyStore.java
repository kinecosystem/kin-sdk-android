package kin.sdk;


import java.util.List;

import javax.annotation.Nonnull;

import kin.base.KeyPair;
import kin.sdk.exception.CorruptedDataException;
import kin.sdk.exception.CreateAccountException;
import kin.sdk.exception.CryptoException;
import kin.sdk.exception.DeleteAccountException;
import kin.sdk.exception.LoadAccountException;

interface KeyStore {

    @Nonnull
    List<KeyPair> loadAccounts() throws LoadAccountException;

    void deleteAccount(int index) throws DeleteAccountException;

    KeyPair newAccount() throws CreateAccountException;

    KeyPair importAccount(@Nonnull String json, @Nonnull String passphrase)
        throws CryptoException, CreateAccountException, CorruptedDataException;

    void clearAllAccounts();
}
