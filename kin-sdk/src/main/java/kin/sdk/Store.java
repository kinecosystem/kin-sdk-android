package kin.sdk;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

interface Store {

    void saveString(@Nonnull String key, @Nonnull String value);

    @Nullable
    String getString(@Nonnull String key);

    void clear(@Nonnull String key);
}
