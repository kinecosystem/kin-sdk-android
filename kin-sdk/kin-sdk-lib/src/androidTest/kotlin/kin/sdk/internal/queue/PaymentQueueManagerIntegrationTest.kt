package kin.sdk.internal.queue

import kin.base.KeyPair
import kin.sdk.internal.data.PendingBalanceUpdaterImpl
import kin.sdk.internal.events.EventsManagerImpl
import kin.sdk.queue.PendingPayment
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class PaymentQueueManagerIntegrationTest {

    object Constants {
        const val ONE_MILLI = 1000L
        const val DELAY_BETWEEN_PAYMENTS_MILLIS = 3000L
        const val QUEUE_TIMEOUT_MILLIS = 5000L
        const val MAX_NUM_OF_PAYMENTS = 5
    }

    private var queueScheduler = FakeIntegrationTestQueueScheduler()
    private var txTaskQueueManager = TransactionTaskQueueManagerImpl()
    private var pendingBalanceUpdater = PendingBalanceUpdaterImpl()
    private var eventsManager = EventsManagerImpl()

    private lateinit var paymentQueueManager: PaymentQueueManager
    private lateinit var destinationAccount: String
    private lateinit var sourceAccount: String
    private val amount: BigDecimal = BigDecimal.TEN

    @Before
    @Throws(Exception::class)
    fun setUp() {
        initPaymentQueueManager()
    }

    private fun initPaymentQueueManager() {
        destinationAccount = KeyPair.random().accountId
        sourceAccount = KeyPair.random().accountId
//        kinAccount = KinAccountImpl(expectedRandomAccount, FakeBackupRestore(), mock(), mock(), mock())
        paymentQueueManager = PaymentQueueManagerImpl(txTaskQueueManager, queueScheduler, pendingBalanceUpdater, eventsManager,
                Constants.DELAY_BETWEEN_PAYMENTS_MILLIS, Constants.QUEUE_TIMEOUT_MILLIS, Constants.MAX_NUM_OF_PAYMENTS)
    }

    @Test
    fun enqueuePayment_ListSizeIncreasedByOne() {
        //given
        val pendingPayment = PendingPaymentImpl(destinationAccount, sourceAccount, amount)

        //when
        paymentQueueManager.enqueue(pendingPayment)

        //then
        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(1))
        val pendingPayments: MutableList<PendingPayment> = mutableListOf()
        pendingPayments.add(pendingPayment)
        assertThat(pendingPayments, `is`(equalTo(paymentQueueManager.paymentQueue)))
    }

    @Test
    fun enqueuePayment_DelayBetweenPayments_ListIsEmpty() {
        //given
        val pendingPayment = PendingPaymentImpl(destinationAccount, sourceAccount, amount)

        //when
        paymentQueueManager.enqueue(pendingPayment)
        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS + Constants.ONE_MILLI)

        //then
        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(0))

        //TODO how to verify stuff in integration tests, like num of method calls and a specific method that was called with specific parameters?

    }
//
//    @Test
//    fun `enqueue wait for queue timeout list is empty only after timeout`() {
//        //given
//        val pendingPayment1 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
//        val pendingPayment2 = PendingPaymentImpl(destinationAccount, sourceAccount, amount.add(BigDecimal.TEN))
//        val pendingPayment3 = PendingPaymentImpl(destinationAccount, sourceAccount, amount.add(BigDecimal.ONE))
//
//        //when
//        paymentQueueManager.enqueue(pendingPayment1)
//        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS - Constants.ONE_MILLI)
//        paymentQueueManager.enqueue(pendingPayment2)
//        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS - Constants.ONE_MILLI)
//        paymentQueueManager.enqueue(pendingPayment3)
//
//        //then
//        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(3))
//        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS)
//        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(0))
//
//        val pendingPayments: MutableList<PendingPayment> = mutableListOf()
//        pendingPayments.add(pendingPayment1)
//        pendingPayments.add(pendingPayment2)
//        pendingPayments.add(pendingPayment3)
//
//        verify(queueScheduler).removeAllPendingTasks()
//        verify(txTaskQueueManager).enqueue(pendingPayments)
//    }
//
//    @Test
//    fun `enqueue max items list is empty only after queue has been filled`() {
//        //given
//        val pendingPayment1 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
//        val pendingPayment2 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
//        val pendingPayment3 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
//        val pendingPayment4 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
//        val pendingPayment5 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
//
//        //when
//        paymentQueueManager.enqueue(pendingPayment1)
//        paymentQueueManager.enqueue(pendingPayment2)
//        paymentQueueManager.enqueue(pendingPayment3)
//        paymentQueueManager.enqueue(pendingPayment4)
//        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(4))
//        paymentQueueManager.enqueue(pendingPayment5)
//
//        //then
//        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(0))
//
//        val pendingPayments: MutableList<PendingPayment> = mutableListOf()
//        pendingPayments.add(pendingPayment1)
//        pendingPayments.add(pendingPayment2)
//        pendingPayments.add(pendingPayment3)
//        pendingPayments.add(pendingPayment4)
//        pendingPayments.add(pendingPayment5)
//
//        verify(queueScheduler).removeAllPendingTasks()
//        verify(txTaskQueueManager).enqueue(pendingPayments)
//    }
//
//    @Test
//    fun `enqueue items schedule queue timeout only for first item each time`() {
//        //given
//        val pendingPayment1 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
//        val pendingPayment2 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
//        val pendingPayment3 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
//        val pendingPayment4 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
//        val pendingPayment5 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
//        val pendingPayment6 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
//
//        //when
//        paymentQueueManager.enqueue(pendingPayment1)
//        paymentQueueManager.enqueue(pendingPayment2)
//        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS + Constants.ONE_MILLI)
//        paymentQueueManager.enqueue(pendingPayment3)
//        paymentQueueManager.enqueue(pendingPayment4)
//        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS + Constants.ONE_MILLI)
//        paymentQueueManager.enqueue(pendingPayment5)
//        paymentQueueManager.enqueue(pendingPayment6)
//
//        //then
//        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(2))
//        verify(queueScheduler, times(2)).removeAllPendingTasks()
//        verify(queueScheduler, times(3)).scheduleDelayed(any(), eq(Constants.QUEUE_TIMEOUT_MILLIS))
//    }

    //TODO maybe make better method names, look at conventions, also later add tests with pending balance

}