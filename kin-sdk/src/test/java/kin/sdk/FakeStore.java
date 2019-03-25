package kin.sdk;


import java.util.HashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class FakeStore implements Store {

    private HashMap<String, String> map = new HashMap<>();

    FakeStore() {
    }

    @Override
    public void saveString(@Nonnull String key, @Nonnull String value) {
        map.put(key, value);
    }

    @Nullable
    @Override
    public String getString(@Nonnull String key) {
        return map.get(key);
    }

    @Override
    public void clear(@Nonnull String key) {
        map.remove(key);
    }
}
