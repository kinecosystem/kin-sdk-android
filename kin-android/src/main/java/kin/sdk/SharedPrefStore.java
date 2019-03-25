package kin.sdk;


import android.content.SharedPreferences;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


class SharedPrefStore implements Store {

    private final SharedPreferences sharedPref;

    SharedPrefStore(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
    }

    @Override
    public void saveString(@Nonnull String key, @Nonnull String value) {
        sharedPref.edit()
            .putString(key, value)
            .apply();
    }

    @Override
    @Nullable
    public String getString(@Nonnull String key) {
        return sharedPref.getString(key, null);
    }

    @Override
    public void clear(@Nonnull String key) {
        sharedPref.edit().remove(key).apply();
    }
}
