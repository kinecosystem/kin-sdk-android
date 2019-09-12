package kin.sdk

import android.support.test.InstrumentationRegistry
import kin.sdk.internal.blockchain.TransactionSender
import kin.sdk.internal.queue.PaymentQueueImpl


private const val appIdVersionPrefix = "1"

fun onboardAccounts(kinClient: KinClient, fakeKinOnBoard: FakeKinOnBoard, senderFundAmount: Int = 0,
                    receiverFundAmount: Int = 0): Pair<KinAccount, KinAccount> {
    val kinAccountSender = kinClient.addAccount()
    val kinAccountReceiver = kinClient.addAccount()
    fakeKinOnBoard.createAccount(kinAccountSender.publicAddress.orEmpty(), senderFundAmount)
    fakeKinOnBoard.createAccount(kinAccountReceiver.publicAddress.orEmpty(), receiverFundAmount)
    return Pair(kinAccountSender, kinAccountReceiver)
}

fun addAppIdToMemo(memo: String, appId: String): String {
    return appIdVersionPrefix.plus("-").plus(appId).plus("-").plus(memo)
}

fun getPaymentQueueTestKinClient(environment: Environment, appId: String, txSender: TransactionSender? = null,
                                 configuration: PaymentQueueImpl.PaymentQueueConfiguration? = null): KinClient {
    val injector = IntegrationTestKinClientInjector(InstrumentationRegistry.getTargetContext(),
            environment, appId, "", txSender, configuration)
    return KinClient(injector)
}

