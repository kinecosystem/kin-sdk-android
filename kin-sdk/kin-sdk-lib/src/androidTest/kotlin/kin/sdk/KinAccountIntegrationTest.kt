package kin.sdk

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import kin.base.*
import kin.sdk.IntegConsts.TEST_NETWORK_URL
import kin.sdk.exception.AccountNotFoundException
import kin.sdk.exception.InsufficientFeeException
import kin.sdk.exception.InsufficientKinException
import kin.sdk.exception.TransactionFailedException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.rules.ExpectedException
import java.io.IOException
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

@Suppress("FunctionName")
class KinAccountIntegrationTest {

    private val appId = "1a2c"
    private val fee: Int = 100
    private val feeInKin: BigDecimal = BigDecimal.valueOf(0.001)
    private val appIdVersionPrefix = "1"
    private val timeoutDurationSeconds: Long = 15
    private val timeoutDurationSecondsLong: Long = 20

    private lateinit var kinClient: KinClient

    @Rule
    @JvmField
    val expectedEx: ExpectedException = ExpectedException.none()

    private val environment: Environment = Environment(TEST_NETWORK_URL, IntegConsts.TEST_NETWORK_ID)

    @Before
    @Throws(IOException::class)
    fun setup() {
        kinClient = KinClient(InstrumentationRegistry.getTargetContext(), environment, appId)
        kinClient.clearAllAccounts()
    }

    @After
    fun teardown() {
        if (::kinClient.isInitialized) {
            kinClient.clearAllAccounts()
        }
    }

    @Test
    @LargeTest
    fun getBalanceSync_AccountNotCreated_AccountNotFoundException() {
        val kinAccount = kinClient.addAccount()

        expectedEx.expect(AccountNotFoundException::class.java)
        expectedEx.expectMessage(kinAccount.publicAddress.orEmpty())
        kinAccount.balanceSync
    }

    @Test
    @LargeTest
    fun getStatusSync_AccountNotCreated_StatusNotCreated() {
        val kinAccount = kinClient.addAccount()

        val status = kinAccount.statusSync
        assertThat(status, equalTo(AccountStatus.NOT_CREATED))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun getBalanceSync_FundedAccount_GotBalance() {
        val kinAccount = kinClient.addAccount()

        fakeKinOnBoard.createAccount(kinAccount.publicAddress.orEmpty())

        assertThat(kinAccount.balanceSync.value(), equalTo(BigDecimal("0.00000")))
        fakeKinOnBoard.fundWithKin(kinAccount.publicAddress.orEmpty(), "3.14159")
        assertThat(kinAccount.balanceSync.value(), equalTo(BigDecimal("3.14159")))

    }

    @Test
    @LargeTest
    fun getAggregatedBalance_FundAccounts_GotAggregatedBalance() {
        val kinAccount1 = kinClient.addAccount()
        val kinAccount2 = kinClient.addAccount()
        val masterAccount = kinClient.addAccount()

        fakeKinOnBoard.createAccount(kinAccount1.publicAddress.orEmpty(), "10.14159")
        fakeKinOnBoard.createAccount(kinAccount2.publicAddress.orEmpty(), "100.00301")
        fakeKinOnBoard.createAccount(masterAccount.publicAddress.orEmpty(), "20.02500")

        linkAccount(kinAccount1, masterAccount, "some package id 1")
        linkAccount(kinAccount2, masterAccount, "some package id 2")

        // multiply the fee in 4 because there were 4 operations(2 in each linkAccount call)
        assertThat(masterAccount.aggregatedBalanceSync.value(), equalTo(BigDecimal("130.16960")
                .subtract(feeInKin.multiply(BigDecimal(4)))))
    }

    @Test
    @LargeTest
    fun getAggregatedBalance_UseAccountThatIsNotAMasterAccount_GotOnlyTheAccountBalance() {
        val kinAccount1 = kinClient.addAccount()
        val kinAccount2 = kinClient.addAccount()
        val masterAccount = kinClient.addAccount()

        fakeKinOnBoard.createAccount(kinAccount1.publicAddress.orEmpty(), "10.14159")
        fakeKinOnBoard.createAccount(kinAccount2.publicAddress.orEmpty(), "100.00301")
        fakeKinOnBoard.createAccount(masterAccount.publicAddress.orEmpty(), "20.02500")

        linkAccount(kinAccount1, masterAccount, "some package id 1")
        linkAccount(kinAccount2, masterAccount, "some package id 2")

        // multiply the fee in 4 because there were 4 operations(2 in each linkAccount call)
        assertThat(kinAccount1.aggregatedBalanceSync.value(), equalTo(BigDecimal("10.14159")
                .subtract(feeInKin.multiply(BigDecimal(2)))))
    }

    @Test
    @LargeTest
    fun getAggregatedBalance_AccountNotCreated_AccountNotFoundException() {
        val masterAccount = kinClient.addAccount()

        expectedEx.expect(AccountNotFoundException::class.java)
        expectedEx.expectMessage(masterAccount.publicAddress.orEmpty())

        masterAccount.aggregatedBalanceSync
    }

    @Test
    @LargeTest
    fun getAggregatedBalance_withExternalAccount_GotAggregatedBalance() {
        val kinAccount = kinClient.addAccount()
        val masterAccountPublicAddress = "GANSSGNRVNCFECSGMODO2BQGDMI5XND2NMTMOVYQFMDNB45UD7VDF2D5"

        fakeKinOnBoard.createAccount(kinAccount.publicAddress.orEmpty(), "10.14159")

        val aggregatedBalance= kinAccount.getAggregatedBalanceSync(masterAccountPublicAddress)
        assertThat(aggregatedBalance.value(), equalTo(BigDecimal("130.16960")
                .subtract(feeInKin.multiply(BigDecimal(4)))))
    }

    @Test
    @LargeTest
    fun getAggregatedBalance_withExternalAccountNotValid_GotAggregatedBalance() {
        val kinAccount = kinClient.addAccount()
        val masterAccountPublicAddress = ""

        fakeKinOnBoard.createAccount(kinAccount.publicAddress.orEmpty(), "10.14159")

        expectedEx.expect(IllegalArgumentException::class.java)
        expectedEx.expectMessage("public address not valid")

        kinAccount.getAggregatedBalanceSync(masterAccountPublicAddress)
    }

    @Test
    @LargeTest
    fun linkAccount_MasterAccountNotCreated_OpNoSourceAccount() {
        val exception = assertFailsWith<TransactionFailedException> {

            val kinAccount = kinClient.addAccount()
            val masterAccount = kinClient.addAccount()

            onboardSingleAccount(kinAccount, 10.14159)

            linkAccount(kinAccount, masterAccount, "some package id")
        }

        assertThat(exception.transactionResultCode, equalTo("tx_failed"))
        assertThat(exception.operationsResultCodes?.get(1), equalTo("op_no_source_account"))
    }

    @Test
    @LargeTest
    fun getControlledAccounts_LinkAccounts_GotAllControlledAccounts() {
        val kinAccount1 = kinClient.addAccount()
        val kinAccount2 = kinClient.addAccount()
        val kinAccount3 = kinClient.addAccount()
        val masterAccount = kinClient.addAccount()

        fakeKinOnBoard.createAccount(kinAccount1.publicAddress.orEmpty(), "10.14159")
        fakeKinOnBoard.createAccount(kinAccount2.publicAddress.orEmpty(), "100.00301")
        fakeKinOnBoard.createAccount(kinAccount3.publicAddress.orEmpty(), "5.00000")
        fakeKinOnBoard.createAccount(masterAccount.publicAddress.orEmpty(), "20.02500")

        linkAccount(kinAccount1, masterAccount, "some package id 1")
        linkAccount(kinAccount2, masterAccount, "some package id 2")
        linkAccount(kinAccount3, masterAccount, "some package id 3")

        val controlledAccounts = masterAccount.controlledAccountsSync
        assertThat(controlledAccounts, hasSize(4))
        // sort the array because we don't know in what order we are getting it
        controlledAccounts.sortBy { it.balance.value() }
        assertThat(controlledAccounts[0].balance.value(), equalTo(BigDecimal("5.00000").subtract(feeInKin.multiply(BigDecimal(2)))))
        assertThat(controlledAccounts[0].publicAddress, equalTo(kinAccount3.publicAddress.orEmpty()))
        assertThat(controlledAccounts[1].balance.value(), equalTo(BigDecimal("10.14159").subtract(feeInKin.multiply(BigDecimal(2)))))
        assertThat(controlledAccounts[1].publicAddress, equalTo(kinAccount1.publicAddress.orEmpty()))
        assertThat(controlledAccounts[2].balance.value(), equalTo(BigDecimal("20.02500")))
        assertThat(controlledAccounts[2].publicAddress, equalTo(masterAccount.publicAddress.orEmpty()))
        assertThat(controlledAccounts[3].balance.value(), equalTo(BigDecimal("100.00301").subtract(feeInKin.multiply(BigDecimal(2)))))
        assertThat(controlledAccounts[3].publicAddress, equalTo(kinAccount2.publicAddress.orEmpty()))
    }

    @Test
    @LargeTest
    fun getControlledAccounts_NoAccounts_EmptyListOfAccounts() {
        val masterAccount = kinClient.addAccount()

        fakeKinOnBoard.createAccount(masterAccount.publicAddress.orEmpty(), "20.02500")

        val controlledAccounts = masterAccount.controlledAccountsSync
        assertThat(controlledAccounts, hasSize(0))
    }

    @Test
    @LargeTest
    fun getControlledAccounts_NoAccounts_AccountNotFoundException() {
        val masterAccount = kinClient.addAccount()

        expectedEx.expect(AccountNotFoundException::class.java)
        expectedEx.expectMessage(masterAccount.publicAddress.orEmpty())

        masterAccount.controlledAccountsSync
    }

    @Test
    @LargeTest
    fun getAccountData_CreateAccount_DataCorrect() {
        val kinAccount = kinClient.addAccount()

        fakeKinOnBoard.createAccount(kinAccount.publicAddress.orEmpty(), "10.14159")
        val accountData = kinAccount.accountDataSync
        assertThat(accountData.publicAddress(), equalTo(kinAccount.publicAddress))
        assertThat(accountData.balances()[0].balance, equalTo("10.14159"))
    }

    @Test
    @LargeTest
    fun getAccountData_NoAccount_AccountNotFoundException() {
        val kinAccount = kinClient.addAccount()

        expectedEx.expect(AccountNotFoundException::class.java)
        expectedEx.expectMessage(kinAccount.publicAddress.orEmpty())
        kinAccount.accountDataSync
    }

    @Test
    @LargeTest
    fun linkAccount_AccountNotCreated_AccountNotFoundException() {
        val kinAccount = kinClient.addAccount()
        val masterAccount = kinClient.addAccount()

        onboardSingleAccount(masterAccount, 20.02500)

        expectedEx.expect(AccountNotFoundException::class.java)
        expectedEx.expectMessage(kinAccount.publicAddress.orEmpty())

        linkAccount(kinAccount, masterAccount, "some package id")
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun getStatusSync_CreateAccount_StatusCreated() {
        val kinAccount = kinClient.addAccount()

        fakeKinOnBoard.createAccount(kinAccount.publicAddress.orEmpty())

        assertThat(kinAccount.balanceSync.value(), equalTo(BigDecimal("0.00000")))
        val status = kinAccount.statusSync
        assertThat(status, equalTo(AccountStatus.CREATED))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendTransaction_WithMemo() {
        val (kinAccountSender, kinAccountReceiver) = onboardAccounts(senderFundAmount = 100)

        val memo = "fake memo"
        val expectedMemo = addAppIdToMemo(memo)

        val transactionId = sendTransactionAndAssert(kinAccountSender, kinAccountReceiver, memo)

        val server = Server(TEST_NETWORK_URL)
        val transactionResponse = server.transactions().transaction(transactionId.id())
        val actualMemo = transactionResponse.memo
        assertThat((actualMemo as MemoText).text, equalTo(expectedMemo))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendTransaction_WithoutMemoJustPrefix() {
        val (kinAccountSender, kinAccountReceiver) = onboardAccounts(senderFundAmount = 100)
        val expectedMemo = addAppIdToMemo("")
        val transactionId = sendTransactionAndAssert(kinAccountSender, kinAccountReceiver, null)
        val server = Server(TEST_NETWORK_URL)
        val transactionResponse = server.transactions().transaction(transactionId.id())
        val actualMemo = transactionResponse.memo
        assertThat((actualMemo as MemoText).text, equalTo(expectedMemo))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendTransaction_WithoutMemo() {
        val (kinAccountSender, kinAccountReceiver) = onboardAccounts(
                senderFundAmount = 100,
                kinClient = KinClient(InstrumentationRegistry.getTargetContext(), environment, null))

        val transactionId = sendTransactionAndAssert(kinAccountSender, kinAccountReceiver, null)
        val server = Server(TEST_NETWORK_URL)
        val transactionResponse = server.transactions().transaction(transactionId.id())
        val actualMemo = transactionResponse.memo
        assertTrue { actualMemo is MemoNone }
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendTransaction_WithoutMemoPrefix() {
        val (kinAccountSender, kinAccountReceiver) = onboardAccounts(
                senderFundAmount = 100,
                kinClient = KinClient(InstrumentationRegistry.getTargetContext(), environment, null))

        val memo = "fake memo"
        val transactionId = sendTransactionAndAssert(kinAccountSender, kinAccountReceiver, memo)
        val server = Server(TEST_NETWORK_URL)
        val transactionResponse = server.transactions().transaction(transactionId.id())
        val actualMemo = transactionResponse.memo
        assertThat<Memo>(actualMemo, `is`<Memo>(instanceOf<Memo>(MemoText::class.java)))
        assertThat((actualMemo as MemoText).text, equalTo(memo))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendTransaction_ReceiverAccountNotCreated_AccountNotFoundException() {
        val kinAccountSender = kinClient.addAccount()
        val kinAccountReceiver = kinClient.addAccount()

        fakeKinOnBoard.createAccount(kinAccountSender.publicAddress.orEmpty())

        expectedEx.expect(AccountNotFoundException::class.java)
        expectedEx.expectMessage(kinAccountReceiver.publicAddress)
        val transaction = kinAccountSender.buildPaymentTransactionSync(kinAccountReceiver.publicAddress.orEmpty(), BigDecimal("21.123"), fee)
        kinAccountSender.sendTransactionSync(transaction)

    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendTransaction_SenderAccountNotCreated_AccountNotFoundException() {
        val kinAccountSender = kinClient.addAccount()
        val kinAccountReceiver = kinClient.addAccount()

        fakeKinOnBoard.createAccount(kinAccountReceiver.publicAddress.orEmpty())

        expectedEx.expect(AccountNotFoundException::class.java)
        expectedEx.expectMessage(kinAccountSender.publicAddress)
        val transaction = kinAccountSender.buildPaymentTransactionSync(kinAccountReceiver.publicAddress.orEmpty(), BigDecimal("21.123"), fee)
        kinAccountSender.sendTransactionSync(transaction)
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendWhitelistTransaction_SenderAccountNotCreated_AccountNotFoundException() {
        val kinAccountSender = kinClient.addAccount()
        val kinAccountReceiver = kinClient.addAccount()

        fakeKinOnBoard.createAccount(kinAccountReceiver.publicAddress.orEmpty())

        expectedEx.expect(AccountNotFoundException::class.java)
        expectedEx.expectMessage(kinAccountSender.publicAddress)

        val transaction = kinAccountSender.buildPaymentTransactionSync(kinAccountReceiver.publicAddress.orEmpty(), BigDecimal("21.123"), fee)
        val whitelist = WhitelistServiceForTest().whitelistTransaction(transaction.whitelistableTransaction())
        kinAccountSender.sendWhitelistTransactionSync(whitelist)
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendWhitelistTransaction_ReceiverAccountNotCreated_AccountNotFoundException() {
        val kinAccountSender = kinClient.addAccount()
        val kinAccountReceiver = kinClient.addAccount()

        fakeKinOnBoard.createAccount(kinAccountSender.publicAddress.orEmpty())

        expectedEx.expect(AccountNotFoundException::class.java)
        expectedEx.expectMessage(kinAccountReceiver.publicAddress)

        val transaction = kinAccountSender.buildPaymentTransactionSync(kinAccountReceiver.publicAddress.orEmpty(), BigDecimal("21.123"), fee)
        val whitelist = WhitelistServiceForTest().whitelistTransaction(transaction.whitelistableTransaction())
        kinAccountSender.sendWhitelistTransactionSync(whitelist)

    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendTransaction_NotEnoughFee_InsufficientFeeException() {
        val (kinAccountSender, kinAccountReceiver) = onboardAccounts()

        expectedEx.expect(InsufficientFeeException::class.java)
        val minFee: Int = Math.toIntExact(kinClient.minimumFeeSync)
        val transaction = kinAccountSender.buildPaymentTransactionSync(kinAccountReceiver.publicAddress.orEmpty(), BigDecimal("21.123"), minFee - 1)
        kinAccountSender.sendTransactionSync(transaction)
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendWhitelistTransaction_FeeNotReduce() {
        val (kinAccountSender, kinAccountReceiver) = onboardAccounts(senderFundAmount = 100)

        val minFee: Int = Math.toIntExact(kinClient.minimumFeeSync)
        val transaction = kinAccountSender.buildPaymentTransactionSync(kinAccountReceiver.publicAddress.orEmpty(),
                BigDecimal("20"), minFee + 100000)
        val whitelist = WhitelistServiceForTest().whitelistTransaction(transaction.whitelistableTransaction())
        kinAccountSender.sendWhitelistTransactionSync(whitelist)
        assertThat(kinAccountSender.balanceSync.value(), equalTo(BigDecimal("80.00000")))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendWhitelistTransaction_Success() {
        val (kinAccountSender, kinAccountReceiver) = onboardAccounts(senderFundAmount = 100)

        val minFee: Int = Math.toIntExact(kinClient.minimumFeeSync)
        val transaction = kinAccountSender.buildPaymentTransactionSync(kinAccountReceiver.publicAddress.orEmpty(),
                BigDecimal("20"), minFee)
        val whitelist = WhitelistServiceForTest().whitelistTransaction(transaction.whitelistableTransaction())
        kinAccountSender.sendWhitelistTransactionSync(whitelist)
        assertThat(kinAccountSender.balanceSync.value(), equalTo(BigDecimal("80.00000")))
        assertThat(kinAccountReceiver.balanceSync.value(), equalTo(BigDecimal("20.00000")))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendTransaction_Success() {
        val (kinAccountSender, kinAccountReceiver) = onboardAccounts(senderFundAmount = 100)
        val transactionId = sendTransactionAndAssert(kinAccountSender, kinAccountReceiver, "fake memo")
        assertNotNull(transactionId)
        assertThat(transactionId.id(), not(isEmptyString()))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun createPaymentListener_ListenToReceiver_PaymentEvent() {
        listenToPayments(false)
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun createPaymentListener_ListenToSender_PaymentEvent() {
        listenToPayments(true)
    }

    @Throws(Exception::class)
    private fun listenToPayments(sender: Boolean) {
        //create and sets 2 accounts (receiver/sender), fund one account, and then
        //send transaction from the funded account to the other, observe this transaction using listeners
        val fundingAmount = BigDecimal("100")
        val transactionAmount = BigDecimal("21.123")

        val (kinAccountSender, kinAccountReceiver) = onboardAccounts(0, 0)

        //register listeners for testing
        val actualPaymentsResults = ArrayList<PaymentInfo>()
        val actualBalanceResults = ArrayList<Balance>()
        val accountToListen = if (sender) kinAccountSender else kinAccountReceiver

        val eventsCount = if (sender) 4 else 2 ///in case of observing the sender we'll get 2 events (1 for funding 1 for the
        //transaction) in case of receiver - only 1 event. multiply by 2, as we 2 listeners (balance and payment)
        val latch = CountDownLatch(eventsCount)
        val paymentListener = accountToListen.addPaymentListener { data ->
            actualPaymentsResults.add(data)
            latch.countDown()
        }
        val balanceListener = accountToListen.addBalanceListener { data ->
            actualBalanceResults.add(data)
            latch.countDown()
        }

        //send the transaction we want to observe
        fakeKinOnBoard.fundWithKin(kinAccountSender.publicAddress.orEmpty(), "100")
        val memo = "memo"
        val expectedMemo = addAppIdToMemo(memo)
        val transaction = kinAccountSender.buildPaymentTransactionSync(kinAccountReceiver.publicAddress.orEmpty(), transactionAmount, fee, memo)
        val expectedTransactionId = kinAccountSender.sendTransactionSync(transaction)

        //verify data notified by listeners
        val transactionIndex = if (sender) 1 else 0 //in case of observing the sender we'll get 2 events (1 for funding 1 for the
        //transaction) in case of receiver - only 1 event
        assertTrue(latch.await(timeoutDurationSeconds, TimeUnit.SECONDS))
        paymentListener.remove()
        balanceListener.remove()
        val paymentInfo = actualPaymentsResults[transactionIndex]
        assertThat(paymentInfo.amount(), equalTo(transactionAmount))
        assertThat(paymentInfo.destinationPublicKey(), equalTo(kinAccountReceiver.publicAddress))
        assertThat(paymentInfo.sourcePublicKey(), equalTo(kinAccountSender.publicAddress))
        assertThat(paymentInfo.fee(), equalTo(100L))
        assertThat(paymentInfo.memo(), equalTo(expectedMemo))
        assertThat(paymentInfo.hash().id(), equalTo(expectedTransactionId.id()))

        val balance = actualBalanceResults[transactionIndex]
        assertThat(balance.value(),
                equalTo(if (sender) fundingAmount.subtract(feeInKin).subtract(transactionAmount) else transactionAmount))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun createPaymentListener_RemoveListener_NoEvents() {
        val (kinAccountSender, kinAccountReceiver) = onboardAccounts(senderFundAmount = 100)

        val latch = CountDownLatch(1)
        val listenerRegistration = kinAccountReceiver.addPaymentListener {
            fail("should not get eny event!")
        }
        listenerRegistration.remove()

        val transaction = kinAccountSender.buildPaymentTransactionSync(kinAccountReceiver.publicAddress.orEmpty(), BigDecimal("21.123"), fee, null)
        kinAccountSender.sendTransactionSync(transaction)
        latch.await(timeoutDurationSecondsLong, TimeUnit.SECONDS)
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendTransaction_NotEnoughKin_InsufficientKinException() {
        val (kinAccountSender, kinAccountReceiver) = onboardAccounts()

        expectedEx.expect(InsufficientKinException::class.java)
        val transaction = kinAccountSender.buildPaymentTransactionSync(kinAccountReceiver.publicAddress.orEmpty(), BigDecimal("21.123"), fee)
        kinAccountSender.sendTransactionSync(transaction)
    }

    @Test
    @Throws(Exception::class)
    fun accountLinkingUsingDifferentKinClients_SendTransaction_TransactionSuccess() {
        var anotherKinClient = KinClient(InstrumentationRegistry.getTargetContext(), environment, "zxcv")
        val masterAccount = anotherKinClient.addAccount()
        val controlledAccount = kinClient.addAccount()
        val destinationAccount = kinClient.addAccount()
        onboardSingleAccount(controlledAccount, 100.0)
        onboardSingleAccount(masterAccount, 100.0)
        onboardSingleAccount(destinationAccount, 100.0)

        linkAccount(controlledAccount, masterAccount, "some package id ")

        val transaction = masterAccount.transactionBuilderSync
                .setFee(fee)
                .setMemo(MemoText.text("master to destination"))
                .addOperation(PaymentOperation.Builder(
                        KeyPair.fromAccountId(destinationAccount.publicAddress), AssetTypeNative(), "21.12300")
                        .setSourceAccount(KeyPair.fromAccountId(controlledAccount.publicAddress))
                        .build())
                .build()
        val transactionId = masterAccount.sendTransactionSync(transaction)

        assertThat(transactionId.id(), not(isEmptyOrNullString()))
        // The controlled account need to reduced the fee from the link account transaction.
        assertThat(controlledAccount.balanceSync.value(), equalTo(BigDecimal("78.87700").subtract(feeInKin.multiply(BigDecimal(2)))))
        assertThat(masterAccount.balanceSync.value(), equalTo(BigDecimal("100.00000").subtract(feeInKin)))
        assertThat(destinationAccount.balanceSync.value(), equalTo(BigDecimal("121.12300")))
        val packageIdInBase64 = masterAccount.accountDataSync.data()[controlledAccount.publicAddress]
        assertThat(String(kin.base.codec.Base64.decodeBase64(packageIdInBase64)), containsString("some package id"))
    }

    private fun linkAccount(controlledAccount: KinAccount, masterAccount: KinAccount, managerDataValue: String) {
        val transactionBuilder = controlledAccount.transactionBuilderSync
        val signerKey = Signer.ed25519PublicKey(KeyPair.fromAccountId(masterAccount.publicAddress))
        val managerDataKey = controlledAccount.publicAddress
        val transaction = transactionBuilder
                .setFee(fee)
                .setMemo(MemoText.text("account linking"))
                .addOperation(SetOptionsOperation.Builder().setSigner(signerKey, 1).build())
                .addOperation(ManageDataOperation.Builder(managerDataKey, managerDataValue.toByteArray())
                        .setSourceAccount(KeyPair.fromAccountId(masterAccount.publicAddress))
                        .build())
                .build()
        // Simulate getting a transaction envelope and decode it.
        val transactionEnvelope = transaction.transactionEnvelope()
        val externalTransaction = RawTransaction.decodeRawTransaction(transactionEnvelope)
        // Sign with the master and sending the linking transaction from the master account
        externalTransaction.addSignature(masterAccount)
        masterAccount.sendTransactionSync(externalTransaction)
    }

    private fun onboardAccounts(senderFundAmount: Int = 0,
                                receiverFundAmount: Int = 0,
                                kinClient: KinClient = this.kinClient): Pair<KinAccount, KinAccount> {
        val kinAccountSender = kinClient.addAccount()
        val kinAccountReceiver = kinClient.addAccount()
        fakeKinOnBoard.createAccount(kinAccountSender.publicAddress.orEmpty(), senderFundAmount.toString())
        fakeKinOnBoard.createAccount(kinAccountReceiver.publicAddress.orEmpty(), receiverFundAmount.toString())
        return Pair(kinAccountSender, kinAccountReceiver)
    }

    private fun onboardSingleAccount(account: KinAccount, fundAmount: Double) {
        fakeKinOnBoard.createAccount(account.publicAddress.orEmpty())
        fakeKinOnBoard.fundWithKin(account.publicAddress.orEmpty(), fundAmount.toString())
    }

    private fun addAppIdToMemo(memo: String): String {
        return appIdVersionPrefix.plus("-").plus(appId).plus("-").plus(memo)
    }

    private fun sendTransactionAndAssert(kinAccountSender: KinAccount, kinAccountReceiver: KinAccount, memo: String?): TransactionId {
        val transaction = kinAccountSender.buildPaymentTransactionSync(kinAccountReceiver.publicAddress.orEmpty(),
                BigDecimal("21.123"), fee, memo)
        val transactionId = kinAccountSender.sendTransactionSync(transaction)
        assertThat(kinAccountSender.balanceSync.value(), equalTo(BigDecimal("78.87700").subtract(feeInKin)))
        assertThat(kinAccountReceiver.balanceSync.value(), equalTo(BigDecimal("21.12300")))
        return transactionId
    }

    companion object {
        //TODO why not to do just @BeforeClass and inside do fakeKinOnBoard = FakeKinOnBoard(), why companion object?
        private lateinit var fakeKinOnBoard: FakeKinOnBoard

        @BeforeClass
        @JvmStatic
        @Throws(IOException::class)
        fun setupKinOnBoard() {
            fakeKinOnBoard = FakeKinOnBoard()
        }
    }
}
