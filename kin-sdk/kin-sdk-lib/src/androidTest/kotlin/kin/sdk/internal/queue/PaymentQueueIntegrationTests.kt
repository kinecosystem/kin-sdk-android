package kin.sdk.internal.queue

import com.nhaarman.mockitokotlin2.*
import kin.sdk.*
import kin.sdk.IntegrationTestKinClientInjector.Constants.delayBetweenPaymentsMillis
import kin.sdk.IntegrationTestKinClientInjector.Constants.maxNumOfPayments
import kin.sdk.IntegrationTestKinClientInjector.Constants.queueTimeoutMillis
import kin.sdk.exception.KinException
import kin.sdk.internal.blockchain.TransactionSender
import kin.sdk.internal.data.TransactionIdImpl
import kin.sdk.internal.queue.PaymentQueueIntegrationTests.CONSTANTS.APP_ID
import kin.sdk.internal.queue.PaymentQueueIntegrationTests.CONSTANTS.FEE
import kin.sdk.internal.queue.PaymentQueueIntegrationTests.CONSTANTS.TIMEOUT_DURATION_SECONDS_LONG
import kin.sdk.queue.PaymentQueue
import kin.sdk.queue.PaymentQueueTransactionProcess
import kin.sdk.queue.PendingPayment
import kin.sdk.transactiondata.BatchPaymentTransaction
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PaymentQueueIntegrationTests {

    object CONSTANTS {
        const val APP_ID = "1a2c"
        const val TIMEOUT_DURATION_SECONDS_LONG: Long = 20
        const val FEE: Int = 100
    }

    private val environment: Environment = Environment(IntegConsts.TEST_NETWORK_URL, IntegConsts.TEST_NETWORK_ID)
    private lateinit var kinClient: KinClient

    private val transactionSender: TransactionSender = mock()
    private val transactionInterceptor: TransactionInterceptor<PaymentQueueTransactionProcess> = mock()


    @Before
    fun setup() {
        val paymentQueueConfiguration = PaymentQueueImpl.PaymentQueueConfiguration(50, queueTimeoutMillis, maxNumOfPayments)
        kinClient = getPaymentQueueTestKinClient(environment, APP_ID, transactionSender, paymentQueueConfiguration)
        kinClient.clearAllAccounts()
    }

    @After
    fun teardown() {
        if (::kinClient.isInitialized) {
            kinClient.clearAllAccounts()
        }
    }

    @Test
    fun enqueuePayments_NegativeAmount_IllegalArgumentException() {
        val amount = BigDecimal(-50)
        val kinAccountSender = kinClient.addAccount()
        val kinAccountReceiver = kinClient.addAccount()
        val paymentQueue = kinAccountSender.paymentQueue()

        assertFailsWith<IllegalArgumentException> { paymentQueue.enqueuePayment(kinAccountReceiver.publicAddress.orEmpty(), amount) }
    }

    @Test
    fun enqueuePayments_NegativeFee_IllegalArgumentException() {
        val kinAccountSender = kinClient.addAccount()
        val paymentQueue = kinAccountSender.paymentQueue()
        assertFailsWith<IllegalArgumentException> { paymentQueue.setFee(-FEE) }
    }

    @Test
    fun enqueuePayments_ZeroFee_GotMinimumFee() {
        //TODO captor the fee parameter from build and see that it is the minimum
        val argumentCaptor = argumentCaptor<Int>()
        val kinAccountSender = kinClient.addAccount()
        val kinAccountReceiver = kinClient.addAccount()
        val paymentQueue = kinAccountSender.paymentQueue()

//        `when`(transactionSender.buildBatchPaymentTransaction(any(), any(), any(), any())).thenReturn(null)

        paymentQueue.enqueuePayment(kinAccountReceiver.publicAddress.orEmpty(), BigDecimal(50))
        Thread.sleep(delayBetweenPaymentsMillis + 1000)

        verify(transactionSender).buildBatchPaymentTransaction(any(), any(), argumentCaptor.capture(), eq(null))
        assertThat(kinClient.minimumFeeSync.toInt(), equalTo((argumentCaptor.firstValue)))
    }

    @Test
    fun enqueuePayments_UseInterceptor_VerifyInterceptorCalled() {
        val kinAccountSender = kinClient.addAccount()
        val kinAccountReceiver = kinClient.addAccount()
        val paymentQueue = kinAccountSender.paymentQueue()
        paymentQueue.setTransactionInterceptor(transactionInterceptor)
        paymentQueue.setFee(FEE)

        val latch = CountDownLatch(5)
        paymentQueue.setEventListener(object : PaymentQueue.EventListener {
            override fun onPaymentEnqueued(payment: PendingPayment?) {
            }

            override fun onTransactionSend(transaction: BatchPaymentTransaction?, payments: MutableList<PendingPayment>?) {

            }

            override fun onTransactionSendSuccess(transactionId: TransactionId?, payments: MutableList<PendingPayment>?) {
            }

            override fun onTransactionSendFailed(payments: MutableList<PendingPayment>?, exception: KinException?) {
                // this will also verify that it fails 5 times
                latch.countDown()
            }

        })

        `when`(transactionInterceptor.interceptTransactionSending(any())).thenAnswer {
            Thread.sleep(200)
            null
        }
        for (x in 0 until 5) {
            for (y in 0 until 4) {
                paymentQueue.enqueuePayment(kinAccountReceiver.publicAddress.orEmpty(), BigDecimal(50))
            }
            Thread.sleep(delayBetweenPaymentsMillis + 10)
        }
        assertTrue(latch.await(TIMEOUT_DURATION_SECONDS_LONG, TimeUnit.SECONDS))

        verify(transactionInterceptor, times(5)).interceptTransactionSending(any())
    }

    @Test
    fun enqueuePayments_AddMultipleTasks_TasksInvokedInCorrectOrder() {
        // in this tests we are inserting to the task queue 5 lists of pending payments, each with 4 elements. the first one is taken but the others are not because the first one is in progress.
        // after they merged in the task queue we have 4 lists, the top 3 with 5 elements and the last one with 1.
        // then we check that indeed we send 5 transactions with the correct order and number of elements with their ids.

        val amount = BigDecimal(50)
        val kinAccountSender = kinClient.addAccount()
        val kinAccountReceiver = kinClient.addAccount()
        val paymentQueue = kinAccountSender.paymentQueue()
        paymentQueue.setFee(FEE)

        `when`(transactionSender.sendTransaction(null)).thenAnswer {
            Thread.sleep(1000) // wait for the tasks to accumulate and merged in the task queue
            TransactionIdImpl("fake transaction id")
        }

        val listOfListOfPayments: MutableList<MutableList<PendingPayment>?> = mutableListOf()
        val latch = CountDownLatch(5)
        paymentQueue.setEventListener(object : PaymentQueue.EventListener {
            override fun onPaymentEnqueued(payment: PendingPayment?) {
            }

            override fun onTransactionSend(transaction: BatchPaymentTransaction?, payments: MutableList<PendingPayment>?) {
                listOfListOfPayments.add(payments)
                latch.countDown()
            }

            override fun onTransactionSendSuccess(transactionId: TransactionId?, payments: MutableList<PendingPayment>?) {
            }

            override fun onTransactionSendFailed(payments: MutableList<PendingPayment>?, exception: KinException?) {
            }

        })

        var idCounter = 1
        // create 'maxNumOfPayments' lists, each one with 4 pending payments
        for (x in 0 until maxNumOfPayments) {
            for (y in 0 until 4) {
                paymentQueue.enqueuePayment(kinAccountReceiver.publicAddress.orEmpty(), amount, idCounter)
                idCounter++
            }
            Thread.sleep(delayBetweenPaymentsMillis + 10)
        }

        assertTrue(latch.await(TIMEOUT_DURATION_SECONDS_LONG, TimeUnit.SECONDS))

        val listOfListOfIds = listOfListOfPayments.map { it?.map { pendingPayment -> pendingPayment.metadata() as Int } }
        val expectedListOfListOfIds = listOf(listOf(1, 2, 3, 4), listOf(5, 6, 7, 8, 9), listOf(10, 11, 12, 13, 14), listOf(15, 16, 17, 18, 19), listOf(20))

        assertThat(listOfListOfIds, hasSize(expectedListOfListOfIds.size))
        for (i in listOfListOfIds.indices) {
            assertThat(listOfListOfIds[i], `is`(expectedListOfListOfIds[i]))
        }
    }

    //TODO add tests which includes testings for the events manager, balance updater and transaction params, and tests for negative/exception path (fee to low, all exceptions, etc)
}