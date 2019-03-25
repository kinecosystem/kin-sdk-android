package kin.sdk;

enum StoreType
{
    KEY_STORE
}

public interface KinStorageFactory
{
    Store getStore(StoreType storeType, String storePrefix);
}
