package kin.sdk;

import android.content.Context;

import javax.annotation.Nonnull;

import kin.utils.MainHandler;

public class AndroidKinClient extends KinClientImpl
{
    /**
     * Build KinClient object.
     *
     * @param context     Android Context
     * @param environment the blockchain network details.
     * @param appId       a 4 character string which represent the application id which will be added to each transaction.
     *                    <br><b>Note:</b> appId must contain only upper and/or lower case letters and/or digits and that the total string length is between 3 to 4.
     *                    For example 1234 or 2ab3 or bcda, etc.</br>
     * @param storeKey    an optional param which is the key for storing this KinClient data, different keys will store a different accounts.
     */
    public AndroidKinClient(@Nonnull Context context,
            @Nonnull Environment environment,
            @Nonnull String appId,
            @Nonnull String storeKey)
    {
        this(new KinStorageFactoryImpl(context),
                new AndroidMainHandler(), environment,
                appId, storeKey);
    }

    private AndroidKinClient(@Nonnull KinStorageFactory kinStorageFactory,
            @Nonnull MainHandler mainHandler,
            @Nonnull Environment environment,
            @Nonnull String appId,
            @Nonnull String storeKey)
    {
        super(kinStorageFactory, mainHandler, environment, appId, storeKey);
    }
}
