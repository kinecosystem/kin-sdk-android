package kin.sdk;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

interface Store {

    void saveString(@NonNull String key, @NonNull String value);

    @Nullable
    String getString(@NonNull String key);

    void clear(@NonNull String key);
}
