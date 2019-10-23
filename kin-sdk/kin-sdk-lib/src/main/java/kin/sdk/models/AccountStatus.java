package kin.sdk.models;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static kin.sdk.models.AccountStatus.CREATED;
import static kin.sdk.models.AccountStatus.NOT_CREATED;

@Retention(SOURCE)
@IntDef({NOT_CREATED, CREATED})
public @interface AccountStatus {

    /**
     * Account was not created on blockchain network, account should be created and funded by a different account on
     * the blockchain.
     */
    int NOT_CREATED = 0;
    /**
     * Account was created, account is ready to use with kin.
     */
    int CREATED = 2;
}
