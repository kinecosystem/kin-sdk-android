package kin.sdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.List;

import kin.base.KeyPair;
import kin.sdk.exception.CreateAccountException;

public class FakeKeyStore extends KeyStoreImpl {

    FakeKeyStore() {
        this(new BackupRestoreImpl());
    }

    FakeKeyStore(@NonNull List<KeyPair> accountList) {
        this();
        for (KeyPair keyPair : accountList) {
            try {
                addKeyPairToStorage(keyPair);
            } catch (CreateAccountException e) {
                e.printStackTrace();
            }
        }
    }

    FakeKeyStore(@NonNull BackupRestore backupRestore) {
        super(new Store() {
            private HashMap<String, String> storage = new HashMap<>();

            @Override
            public void saveString(@NonNull String key, @NonNull String value) {
                storage.put(key, value);
            }

            @Nullable
            @Override
            public String getString(@NonNull String key) {
                return storage.get(key);
            }

            @Override
            public void clear(@NonNull String key) {
                storage.remove(key);
            }
        }, backupRestore);
    }
}
