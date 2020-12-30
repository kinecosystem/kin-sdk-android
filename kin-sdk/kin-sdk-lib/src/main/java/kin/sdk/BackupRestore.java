package kin.sdk;


import android.support.annotation.NonNull;

import kin.base.KeyPair;
import kin.sdk.exception.CorruptedDataException;
import kin.sdk.exception.CryptoException;

interface BackupRestore {

    @NonNull
    String exportWallet(@NonNull KeyPair keyPair, @NonNull String passphrase)
            throws CryptoException;

    @NonNull
    KeyPair importWallet(@NonNull String exportedJson, @NonNull String passphrase)
            throws CryptoException, CorruptedDataException;
}
