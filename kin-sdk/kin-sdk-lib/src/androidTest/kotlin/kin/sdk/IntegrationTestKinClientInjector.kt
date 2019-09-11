package kin.sdk

import android.content.Context
import kin.sdk.internal.blockchain.TransactionSender
import kin.sdk.internal.queue.PaymentQueueImpl

class IntegrationTestKinClientInjector(context: Context?, environment: Environment?, appId: String?, storeKey: String?,
                                       private val txSender: TransactionSender? = null,
                                       private val configuration: PaymentQueueImpl.PaymentQueueConfiguration?) :
        KinClientInjector(context, environment, appId, storeKey) {


    override fun getPaymentQueueConfiguration(): PaymentQueueImpl.PaymentQueueConfiguration {
        return configuration
                ?: PaymentQueueImpl.PaymentQueueConfiguration(Constants.delayBetweenPaymentsMillis, Constants.queueTimeoutMillis, Constants.maxNumOfPayments)
    }

    override fun getTransactionSender(): TransactionSender {
        return txSender ?: super.getTransactionSender()
    }

    object Constants {
        const val delayBetweenPaymentsMillis: Long = 250
        const val queueTimeoutMillis: Long = 2000
        const val maxNumOfPayments: Int = 5
    }

}