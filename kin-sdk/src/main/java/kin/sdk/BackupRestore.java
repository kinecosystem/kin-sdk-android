package kin.sdk;


import javax.annotation.Nonnull;

import kin.base.KeyPair;
import kin.sdk.exception.CorruptedDataException;
import kin.sdk.exception.CryptoException;

interface BackupRestore {

    @Nonnull
    String exportWallet(@Nonnull KeyPair keyPair, @Nonnull String passphrase)
        throws CryptoException;

    @Nonnull
    KeyPair importWallet(@Nonnull String exportedJson, @Nonnull String passphrase)
        throws CryptoException, CorruptedDataException;
}
