package kin.sdk.models

import android.support.annotation.IntDef
import kin.sdk.models.AccountStatus.Companion.CREATED
import kin.sdk.models.AccountStatus.Companion.NOT_CREATED

@Retention(AnnotationRetention.SOURCE)
@IntDef(NOT_CREATED, CREATED)
annotation class AccountStatus {
    companion object {

        /**
         * Account was not created on blockchain network, account should be created and funded by a different account on
         * the blockchain.
         */
        const val NOT_CREATED = 0
        /**
         * Account was created, account is ready to use with kin.
         */
        const val CREATED = 2
    }
}
