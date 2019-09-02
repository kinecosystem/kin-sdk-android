package kin.sdk.internal.queue

import com.nhaarman.mockitokotlin2.*
import kin.base.KeyPair
import kin.sdk.internal.data.PendingBalanceUpdater
import kin.sdk.internal.events.EventsManager
import kin.sdk.queue.PendingPayment
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class PaymentQueueManagerTest {

    object Constants {
        const val ONE_MILLI = 1000L
        const val DELAY_BETWEEN_PAYMENTS_MILLIS = 3000L
        const val QUEUE_TIMEOUT_MILLIS = 5000L
        const val MAX_NUM_OF_PAYMENTS = 5
    }

    private var queueScheduler: FakeQueueScheduler = spy() //TODO use a spy in order to be able to verify method calls on that object, is this fine?
    private var txTaskQueueManager: TransactionTasksQueueManager = mock()
    private var pendingBalanceUpdater: PendingBalanceUpdater = mock()
    private var eventsManager: EventsManager = mock()

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
    fun `enqueue, list size is increased by one`() {
        //given
        val pendingPayment = PendingPaymentImpl(destinationAccount, sourceAccount, amount)

        //when
        paymentQueueManager.enqueue(pendingPayment)

        //then
        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(1))
    }

    @Test
    fun `enqueue, then wait for delay between payments, after the delay the list will be empty`() {
        //given
        val pendingPayment = PendingPaymentImpl(destinationAccount, sourceAccount, amount)

        //when
        paymentQueueManager.enqueue(pendingPayment)
        // TODO because we know how much time the task should take then i use sleep for that time plus one milli - is this the correct pattern?
        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS + Constants.ONE_MILLI)

        //then
        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(0))

        val pendingPayments: MutableList<PendingPayment> = mutableListOf()
        pendingPayments.add(pendingPayment)

        //TODO should i really verify it? maybe this shouldn't be verified because it is
        //TODO like a whitebox for us we should verify the outcome and not the way we got to that outcome. no?
        verify(queueScheduler).removePendingTask(any())
        verify(queueScheduler).removeAllPendingTasks()

        verify(txTaskQueueManager).enqueue(pendingPayments)
    }

    @Test
    fun `enqueue, then wait for queue timeout, after the timeout the list will be empty`() {
        //given
        val pendingPayment1 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment2 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment3 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)

        //when
        paymentQueueManager.enqueue(pendingPayment1)
        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS - Constants.ONE_MILLI)
        paymentQueueManager.enqueue(pendingPayment2)
        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS - Constants.ONE_MILLI)
        paymentQueueManager.enqueue(pendingPayment3)

        //then
        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(3))
        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS)
        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(0))

        val pendingPayments: MutableList<PendingPayment> = mutableListOf()
        pendingPayments.add(pendingPayment1)
        pendingPayments.add(pendingPayment2)
        pendingPayments.add(pendingPayment3)

        verify(queueScheduler).removeAllPendingTasks()
        verify(txTaskQueueManager).enqueue(pendingPayments)
    }

    @Test
    fun `enqueue max items, after the queue has been filled then the list will be empty`() {
        //given
        val pendingPayment1 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment2 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment3 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment4 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment5 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)

        //when
        paymentQueueManager.enqueue(pendingPayment1)
        paymentQueueManager.enqueue(pendingPayment2)
        paymentQueueManager.enqueue(pendingPayment3)
        paymentQueueManager.enqueue(pendingPayment4)
        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(4))
        paymentQueueManager.enqueue(pendingPayment5)

        //then
        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(0))

        val pendingPayments: MutableList<PendingPayment> = mutableListOf()
        pendingPayments.add(pendingPayment1)
        pendingPayments.add(pendingPayment2)
        pendingPayments.add(pendingPayment3)
        pendingPayments.add(pendingPayment4)
        pendingPayments.add(pendingPayment5)

        verify(queueScheduler).removeAllPendingTasks()
        verify(txTaskQueueManager).enqueue(pendingPayments)
    }

    @Test
    fun `enqueue items, verify that schedule queue timeout happens only for first item`() {
        //given
        val pendingPayment1 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment2 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment3 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment4 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment5 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment6 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)

        //when
        paymentQueueManager.enqueue(pendingPayment1)
        paymentQueueManager.enqueue(pendingPayment2)
        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS + Constants.ONE_MILLI)
        paymentQueueManager.enqueue(pendingPayment3)
        paymentQueueManager.enqueue(pendingPayment4)
        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS + Constants.ONE_MILLI)
        paymentQueueManager.enqueue(pendingPayment5)
        paymentQueueManager.enqueue(pendingPayment6)

        //then
        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(2))
        verify(queueScheduler, times(2)).removeAllPendingTasks()
        verify(queueScheduler, times(3)).scheduleDelayed(any(), eq(Constants.QUEUE_TIMEOUT_MILLIS))
    }

    //TODO maybe make better method names, look at conventions, also later add tests with pending balance

}