package kin.sdk;

import android.content.Context;

public class KinStorageFactoryImpl implements KinStorageFactory
{
    private static final String KEY_STORE_NAME_PREFIX = "KinKeyStore_";

    private Context context;

    public KinStorageFactoryImpl(Context context)
    {
        this.context = context;
    }

    @Override
    public Store getStore(StoreType storeType, String storePrefix)
    {
        switch (storeType) {
            case KEY_STORE: {
                return new SharedPrefStore(
                        context.getSharedPreferences(KEY_STORE_NAME_PREFIX + storePrefix,
                                Context.MODE_PRIVATE));
            }
        }
        return null;
    }
}
