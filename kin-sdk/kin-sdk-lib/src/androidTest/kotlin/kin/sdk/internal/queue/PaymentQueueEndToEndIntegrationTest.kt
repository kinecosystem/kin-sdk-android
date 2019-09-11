package kin.sdk.internal.queue

import kin.base.MemoText
import kin.base.Server
import kin.sdk.*
import kin.sdk.exception.KinException
import kin.sdk.queue.PaymentQueue
import kin.sdk.queue.PaymentQueueTransactionProcess
import kin.sdk.queue.PendingPayment
import kin.sdk.transactiondata.BatchPaymentTransaction
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.io.IOException
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue
import kotlin.test.fail


class PaymentQueueEndToEndIntegrationTest {

    private val appId = "1a2c"
    private val fee: Int = 100
    private val feeInKin: BigDecimal = BigDecimal.valueOf(0.001)
    private val timeoutDurationSecondsLong: Long = 20
    private val environment: Environment = Environment(IntegConsts.TEST_NETWORK_URL, IntegConsts.TEST_NETWORK_ID)
    private lateinit var kinClient: KinClient


    @Before
    fun setup() {
        kinClient = getPaymentQueueTestKinClient(environment, appId)
        kinClient.clearAllAccounts()
    }

    @After
    fun teardown() {
        if (::kinClient.isInitialized) {
            kinClient.clearAllAccounts()
        }
    }

    @Test
    fun enqueuePayments_DelayBetweenPayments_Success() {
        val amount = BigDecimal(50)
        val kinAccountSender = kinClient.addAccount()
        fakeKinOnBoard.createAccount(kinAccountSender.publicAddress.orEmpty(), 250)
        val kinAccountReceiver1 = addAndCreateAccount()
        val kinAccountReceiver2 = addAndCreateAccount()
        val kinAccountReceiver3 = addAndCreateAccount()

        val latch = CountDownLatch(1)
        val paymentQueue = kinAccountSender.paymentQueue()
        setEventListener(paymentQueue, latch)

        paymentQueue.setFee(fee)
        paymentQueue.enqueuePayment(kinAccountReceiver2.publicAddress.orEmpty(), amount, "2")
        paymentQueue.enqueuePayment(kinAccountReceiver1.publicAddress.orEmpty(), amount, "1")
        paymentQueue.enqueuePayment(kinAccountReceiver3.publicAddress.orEmpty(), amount, "3")
        paymentQueue.enqueuePayment(kinAccountReceiver1.publicAddress.orEmpty(), amount, "4")
        Thread.sleep(50)
        assertThat(paymentQueue.pendingPaymentsCount(), equalTo(4))

        assertTrue(latch.await(timeoutDurationSecondsLong, TimeUnit.SECONDS))

        assertThat(kinAccountReceiver1.balanceSync.value(), equalTo(BigDecimal("100.00000")))
        assertThat(kinAccountReceiver2.balanceSync.value(), equalTo(BigDecimal("50.00000")))
        assertThat(kinAccountReceiver3.balanceSync.value(), equalTo(BigDecimal("50.00000")))
        assertThat(kinAccountSender.balanceSync.value(), equalTo(BigDecimal("50.00000").subtract(feeInKin.multiply(BigDecimal(4)))))
    }

    @Test
    fun enqueuePayments_MaxQueueElements_Intercept_Success() {
        val amount = BigDecimal(50)
        val pendingPayments = mutableListOf<PendingPayment>()
        val kinAccountSender = kinClient.addAccount()
        fakeKinOnBoard.createAccount(kinAccountSender.publicAddress.orEmpty(), 350)

        val kinAccountReceiver1 = addAndCreateAccount()
        val kinAccountReceiver2 = addAndCreateAccount()
        val kinAccountReceiver3 = addAndCreateAccount()

        val latch = CountDownLatch(1)
        val paymentQueue = kinAccountSender.paymentQueue()
        setEventListener(paymentQueue, latch)

        paymentQueue.setFee(fee)

        val memo = "fake memo"
        val expectedMemo = addAppIdToMemo(memo, appId)


        var transactionId: TransactionId? = null
        paymentQueue.setTransactionInterceptor(object : TransactionInterceptor<PaymentQueueTransactionProcess> {
            override fun interceptTransactionSending(process: PaymentQueueTransactionProcess?): TransactionId? {
                val transaction = process?.transaction(memo)
                // simulate some work
                Thread.sleep(5000)
                transactionId = process?.send(transaction)
                return transactionId
            }
        })

        paymentQueue.enqueuePayment(kinAccountReceiver1.publicAddress.orEmpty(), amount, "1")
        paymentQueue.enqueuePayment(kinAccountReceiver2.publicAddress.orEmpty(), amount, "2")
        paymentQueue.enqueuePayment(kinAccountReceiver3.publicAddress.orEmpty(), amount, "3")
        paymentQueue.enqueuePayment(kinAccountReceiver1.publicAddress.orEmpty(), amount, "4")
        paymentQueue.enqueuePayment(kinAccountReceiver3.publicAddress.orEmpty(), amount, "5")
        Thread.sleep(100)
        assertThat(paymentQueue.pendingPaymentsCount(), equalTo(0))

        assertTrue(latch.await(timeoutDurationSecondsLong, TimeUnit.SECONDS))

        assertThat(kinAccountReceiver1.balanceSync.value(), equalTo(BigDecimal("100.00000")))
        assertThat(kinAccountReceiver2.balanceSync.value(), equalTo(BigDecimal("50.00000")))
        assertThat(kinAccountReceiver3.balanceSync.value(), equalTo(BigDecimal("100.00000")))
        assertThat(kinAccountSender.balanceSync.value(), equalTo(BigDecimal("100.00000").subtract(feeInKin.multiply(BigDecimal(5)))))

        val server = Server(IntegConsts.TEST_NETWORK_URL)
        val transactionResponse = server.transactions().transaction(transactionId?.id())
        val actualMemo = transactionResponse.memo
        assertThat((actualMemo as MemoText).text, equalTo(expectedMemo))
    }


    private fun setEventListener(paymentQueue: PaymentQueue, latch: CountDownLatch) {
        paymentQueue.setEventListener(object : PaymentQueue.EventListener {
            override fun onPaymentEnqueued(payment: PendingPayment?) {
            }

            override fun onTransactionSend(transaction: BatchPaymentTransaction?, payments: MutableList<PendingPayment>?) {
                assertThat(paymentQueue.transactionInProgress(), equalTo(true))
            }

            override fun onTransactionSendSuccess(transactionId: TransactionId?, payments: MutableList<PendingPayment>?) {
                latch.countDown()
            }

            override fun onTransactionSendFailed(payments: MutableList<PendingPayment>?, exception: KinException?) {
                latch.countDown()
            }

        })
    }

    //TODO include events manager and balance updater to the happy path and transaction params happy path

    private fun addAndCreateAccount(): KinAccount {
        val kinAccount = kinClient.addAccount()
        kinAccount.publicAddress?.let {
            fakeKinOnBoard.createAccount(it)
        } ?: kotlin.run {
            fail("public address is null")
        }
        return kinAccount
    }

    companion object {
        private lateinit var fakeKinOnBoard: FakeKinOnBoard

        @BeforeClass
        @JvmStatic
        @Throws(IOException::class)
        fun setupKinOnBoard() {
            fakeKinOnBoard = FakeKinOnBoard()
        }
    }

}