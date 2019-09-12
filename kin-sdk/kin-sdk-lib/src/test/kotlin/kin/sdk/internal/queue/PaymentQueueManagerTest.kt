package kin.sdk.internal.queue

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
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
        const val DELAY_BETWEEN_PAYMENTS_MILLIS = 100L
        const val EPSILON_IN_MILLIS = DELAY_BETWEEN_PAYMENTS_MILLIS / 2
        const val QUEUE_TIMEOUT_MILLIS = DELAY_BETWEEN_PAYMENTS_MILLIS * 4
        const val MAX_NUM_OF_PAYMENTS = 5
    }

    private var queueScheduler: FakeQueueScheduler = FakeQueueScheduler()
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
        val configuration = PaymentQueueImpl.PaymentQueueConfiguration(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS,
                Constants.QUEUE_TIMEOUT_MILLIS, Constants.MAX_NUM_OF_PAYMENTS)
        paymentQueueManager = PaymentQueueManagerImpl(txTaskQueueManager, queueScheduler,
                pendingBalanceUpdater, eventsManager, configuration)
    }

    @Test
    fun `enqueue, list size is increased by one`() {
        //given
        val pendingPayment = PendingPaymentImpl(destinationAccount, sourceAccount, amount)

        //when
        paymentQueueManager.enqueue(pendingPayment)
        Thread.sleep(Constants.EPSILON_IN_MILLIS)

        //then
        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(1))
    }

    @Test
    fun `enqueue, then wait for delay between payments, after the delay the list will be empty`() {
        //given
        val pendingPayment = PendingPaymentImpl(destinationAccount, sourceAccount, amount)

        //when
        paymentQueueManager.enqueue(pendingPayment)
        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS + Constants.EPSILON_IN_MILLIS)

        //then
        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(0))

        val pendingPayments: MutableList<PendingPayment> = mutableListOf()
        pendingPayments.add(pendingPayment)

        assertThat(queueScheduler.numOfTasks, equalTo(0))

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
        Thread.sleep(Constants.EPSILON_IN_MILLIS)
        paymentQueueManager.enqueue(pendingPayment2)
        Thread.sleep(Constants.EPSILON_IN_MILLIS)
        paymentQueueManager.enqueue(pendingPayment3)
        Thread.sleep(Constants.EPSILON_IN_MILLIS)

        //then
        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(3))
        Thread.sleep(Constants.QUEUE_TIMEOUT_MILLIS)
        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(0))

        val pendingPayments: MutableList<PendingPayment> = mutableListOf()
        pendingPayments.add(pendingPayment1)
        pendingPayments.add(pendingPayment2)
        pendingPayments.add(pendingPayment3)

        assertThat(queueScheduler.numOfTasks, equalTo(0))

        verify(txTaskQueueManager).enqueue(pendingPayments)
    }

    @Test
    fun `enqueue max items, after the queue has been filled then the list will be empty`() {
        //given
        val pendingPayment1 = PendingPaymentImpl(destinationAccount, sourceAccount, amount.add(BigDecimal(1)))
        val pendingPayment2 = PendingPaymentImpl(destinationAccount, sourceAccount, amount.add(BigDecimal(2)))
        val pendingPayment3 = PendingPaymentImpl(destinationAccount, sourceAccount, amount.add(BigDecimal(3)))
        val pendingPayment4 = PendingPaymentImpl(destinationAccount, sourceAccount, amount.add(BigDecimal(4)))
        val pendingPayment5 = PendingPaymentImpl(destinationAccount, sourceAccount, amount.add(BigDecimal(5)))

        //when
        paymentQueueManager.enqueue(pendingPayment1)
        paymentQueueManager.enqueue(pendingPayment2)
        paymentQueueManager.enqueue(pendingPayment3)
        paymentQueueManager.enqueue(pendingPayment4)
        Thread.sleep(Constants.EPSILON_IN_MILLIS)

        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(4))
        paymentQueueManager.enqueue(pendingPayment5)
        Thread.sleep(Constants.EPSILON_IN_MILLIS)

        //then
        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(0))

        val pendingPayments: MutableList<PendingPayment> = mutableListOf()
        pendingPayments.add(pendingPayment1)
        pendingPayments.add(pendingPayment2)
        pendingPayments.add(pendingPayment3)
        pendingPayments.add(pendingPayment4)
        pendingPayments.add(pendingPayment5)

        assertThat(queueScheduler.numOfTasks, equalTo(0))
        verify(txTaskQueueManager).enqueue(pendingPayments)
    }

    @Test
    fun `enqueue items, verify that schedule queue timeout did not occurred`() {
        //given
        val pendingPayment1 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment2 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment3 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment4 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment5 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment6 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment7 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment8 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment9 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)
        val pendingPayment10 = PendingPaymentImpl(destinationAccount, sourceAccount, amount)

        //when
        paymentQueueManager.enqueue(pendingPayment1)
        paymentQueueManager.enqueue(pendingPayment2)
        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS + Constants.EPSILON_IN_MILLIS)
        paymentQueueManager.enqueue(pendingPayment3)
        paymentQueueManager.enqueue(pendingPayment4)
        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS + Constants.EPSILON_IN_MILLIS)
        paymentQueueManager.enqueue(pendingPayment5)
        paymentQueueManager.enqueue(pendingPayment6)
        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS + Constants.EPSILON_IN_MILLIS)
        paymentQueueManager.enqueue(pendingPayment7)
        paymentQueueManager.enqueue(pendingPayment8)
        Thread.sleep(Constants.DELAY_BETWEEN_PAYMENTS_MILLIS + Constants.EPSILON_IN_MILLIS)
        paymentQueueManager.enqueue(pendingPayment9)
        paymentQueueManager.enqueue(pendingPayment10)
        Thread.sleep(Constants.EPSILON_IN_MILLIS)

        //then
        assertThat(paymentQueueManager.pendingPaymentCount, equalTo(2))
        // 2 tasks should remain, the queue timeout task and the delay task which was added after enqueue of pendingPayment10.
        assertThat(queueScheduler.numOfTasks, equalTo(2))
    }

    //TODO maybe make better method names, look at conventions, also later add tests with pending balance

}