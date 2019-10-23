package kin.sdk.internal.storage;


import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public class SharedPrefStore implements Store {

    private final SharedPreferences sharedPref;

    public SharedPrefStore(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
    }

    @Override
    public void saveString(@NonNull String key, @NonNull String value) {
        sharedPref.edit()
                .putString(key, value)
                .apply();
    }

    @Override
    @Nullable
    public String getString(@NonNull String key) {
        return sharedPref.getString(key, null);
    }

    @Override
    public void clear(@NonNull String key) {
        sharedPref.edit().remove(key).apply();
    }
}
