package kin.sdk;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import kin.base.FormatException;
import kin.base.KeyPair;
import kin.base.Network;
import kin.base.Server;
import kin.base.responses.HttpResponseException;
import kin.sdk.exception.AccountNotFoundException;
import kin.sdk.exception.IllegalAmountException;
import kin.sdk.exception.InsufficientFeeException;
import kin.sdk.exception.InsufficientKinException;
import kin.sdk.exception.OperationFailedException;
import kin.sdk.exception.TransactionFailedException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.hamcrest.beans.HasPropertyWithValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest = Config.NONE)
public class TransactionSenderTest {

    private static final String ACCOUNT_ID_FROM = "GDKJAMCTGZGD6KM7RBEII6QUYAHQQUGERXKM3ESHBX2UUNTNAVNB3OGX";
    private static final String SECRET_SEED_FROM = "SB6PCLT2WUQF44HVOTEGCXIDYNX2U4BJUPWUX453ODRGD4CXGPJP3HUX";
    private static final String ACCOUNT_ID_TO = "GDJOJJVIWI6YVPUI3PX4BQCC4SQUZTRYIAMV2YBT6QVL54QGQUQSFKGM";
    private static final String SECRET_SEED_TO = "SCJFLXKUY6VQT2LYSP6XDP23WNEP5OITSC3LZEJUJO7GFZM7QLDF2BCN";
    private static final String TX_BODY = "tx=AAAAANSQMFM2TD8pn4hIhHoUwA8IUMSN1M2SRw31SjZtBVodAAAAZABpZ8AAAAAEAAAAAAAAAAEAAAAHMS0xYTJjLQAAAAABAAAAAAAAAAEAAAAA0uSmqLI9ir6I2%2B%2FAwELkoUzOOEAZXWAz9Cq%2B8gaFISIAAAAAAAAAAAACSfAAAAAAAAAAAW0FWh0AAABAl5hewshgOkfTHymdIJTtIK2L%2FUpDcznSSGW3tZPRRGdmnCfOdTnZcfhDkWna0w14W1oVP2GzfZy3GrJPkb2XCA%3D%3D";
    private static final String TX_BODY_WITH_MEMO = "tx=AAAAANSQMFM2TD8pn4hIhHoUwA8IUMSN1M2SRw31SjZtBVodAAAAZABpZ8AAAAAEAAAAAAAAAAEAAAAQMS0xYTJjLWZha2UgbWVtbwAAAAEAAAAAAAAAAQAAAADS5Kaosj2Kvojb78DAQuShTM44QBldYDP0Kr7yBoUhIgAAAAAAAAAAATEtAAAAAAAAAAABbQVaHQAAAEA8ATZESAblmH%2BOaT5RZbdmlv3IgqV7HPkiHGva%2Bhcqv4dNancWW8PcprGwGMUlEU0GqlbszOLXyR1DsQlIcTEC";
    private static final String APP_ID = "1a2c";
    private static final int FEE = 100;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private KeyStore mockKeyStore;
    private Server server;
    private MockWebServer mockWebServer;
    private TransactionSender transactionSender;
    private KeyPair account;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        mockServer();
        Network.useTestNetwork();

        transactionSender = new TransactionSender(server, APP_ID);
        account = KeyPair.fromSecretSeed(SECRET_SEED_FROM);
    }

    private void mockServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String url = mockWebServer.url("").toString();
        server = new Server(url);
    }

    @Test
    public void sendTransaction_success() throws Exception {
        //send transaction fetch first to account details, then from account details, and finally perform tx,
        //here we mock all 3 responses from server to achieve success operation
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_success_res.json"));

        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("1.5"), FEE);
        TransactionId transactionId = transactionSender.sendTransaction(transaction);

        assertEquals("8f1e0cd1d922f4c57cc1898ececcf47375e52ec4abf77a7e32d0d9bb4edecb69", transactionId.id());

        //verify sent requests data
        assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID_FROM));
        assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID_TO));
        assertThat(mockWebServer.takeRequest().getBody().readUtf8(), equalTo(TX_BODY));
    }

    @Test
    public void sendTransaction_WithMemo_success() throws Exception {
        //send transaction fetch first to account details, then from account details, and finally perform tx,
        //here we mock all 3 responses from server to achieve success operation
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_success_res.json"));
        String fakeMemo = "fake memo";

        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"), FEE, fakeMemo);
        TransactionId transactionId = transactionSender.sendTransaction(transaction);

        assertEquals("8f1e0cd1d922f4c57cc1898ececcf47375e52ec4abf77a7e32d0d9bb4edecb69", transactionId.id());

        //verify sent requests data
        assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID_FROM));
        assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID_TO));
        assertThat(mockWebServer.takeRequest().getBody().readUtf8(), equalTo(TX_BODY_WITH_MEMO));
    }

    @Test
    public void sendTransaction_ToAccountNotExist() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        expectedEx.expect(AccountNotFoundException.class);
        expectedEx.expect(new HasPropertyWithValue<>("accountId", equalTo(ACCOUNT_ID_TO)));

        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("1.5"), FEE);
        transactionSender.sendTransaction(transaction);
    }

	@Test
	public void sendTransaction_Http_307_Response_Success() throws Exception {
        MockWebServer mockWebServerHttp307 = new MockWebServer();
        mockWebServerHttp307.start();
        String location = mockWebServerHttp307.url("").toString();

		//send transaction fetch first to account details, then from account details, and finally perform tx which will return 307
		// and then 200. here we mock all 4 responses from server to achieve success operation
		mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
		mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
		// No need for a real location because any way it is local host
        mockWebServer
            .enqueue(TestUtils.generateSuccessHttp307MockResponse(location));
        mockWebServerHttp307.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_success_res.json"));

		Transaction transaction = transactionSender
			.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("1.5"), FEE);
		TransactionId transactionId = transactionSender.sendTransaction(transaction);

		assertEquals("8f1e0cd1d922f4c57cc1898ececcf47375e52ec4abf77a7e32d0d9bb4edecb69", transactionId.id());

		//verify sent requests data
		assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID_FROM));
		assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID_TO));
		assertThat(mockWebServer.takeRequest().getBody().readUtf8(), equalTo(TX_BODY));
        assertThat(mockWebServerHttp307.takeRequest().getBody().readUtf8(), equalTo(TX_BODY));
	}

    @Test
    public void sendTransaction_FromAccountNotExist() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        expectedEx.expect(AccountNotFoundException.class);
        expectedEx.expect(new HasPropertyWithValue<>("accountId", equalTo(ACCOUNT_ID_FROM)));

        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("1.5"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    public void sendTransaction_GeneralStellarError() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(new MockResponse()
            .setBody(TestUtils.loadResource(this.getClass(), "tx_failure_res_general_stellar_error.json"))
            .setResponseCode(400)
        );

        expectedEx.expect(TransactionFailedException.class);
        expectedEx.expect(new HasPropertyWithValue<>("transactionResultCode", equalTo("tx_failed")));
        expectedEx.expect(new HasPropertyWithValue<>("operationsResultCodes", contains("op_malformed")));
        expectedEx.expect(new HasPropertyWithValue<>("operationsResultCodes", hasSize(1)));

        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"), FEE);
        transactionSender.sendTransaction(transaction);
    }


    @Test
    public void sendTransaction_Underfunded_InsufficientKinException() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(new MockResponse()
            .setBody(TestUtils.loadResource(this.getClass(), "tx_failure_res_underfunded.json"))
            .setResponseCode(400)
        );

        expectedEx.expect(InsufficientKinException.class);

        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    public void sendTransaction_InsufficientFeeException() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody(TestUtils.loadResource(this.getClass(), "tx_failure_res_insufficient_fee.json"))
                .setResponseCode(400)
        );

        expectedEx.expect(InsufficientFeeException.class);

        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    public void sendTransaction_InsufficientBalanceException() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody(TestUtils.loadResource(this.getClass(), "tx_failure_res_insufficient_balance.json"))
                .setResponseCode(400)
        );

        expectedEx.expect(InsufficientKinException.class);

        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    public void sendTransaction_FirstQuery_HttpResponseError() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        testHttpResponseCode(500);
    }

    @Test
    public void sendTransaction_SecondQuery_HttpResponseError() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(500)
        );

        testHttpResponseCode(500);
    }

    @Test
    public void sendTransaction_FirstQuery_ConnectionException() throws Exception {
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(IOException.class));

        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    public void sendTransaction_SecondQuery_ConnectionException() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        expectedEx.expect(OperationFailedException.class);

        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    public void sendTransaction_ThirdQuery_ConnectionException() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        expectedEx.expect(OperationFailedException.class);

        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test(timeout = 500)
    public void sendTransaction_changeTimeOut() throws Exception {
        String url = mockWebServer.url("").toString();
        server = new Server(url, 100, TimeUnit.MILLISECONDS);
        transactionSender = new TransactionSender(server, APP_ID);

        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(SocketTimeoutException.class));
        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    public void sendTransaction_FirstQuery_NullResponse() throws Exception {
        TestUtils.enqueueEmptyResponse(mockWebServer);

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectMessage(ACCOUNT_ID_FROM);

        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    public void sendTransaction_SecondQuery_NullResponse() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        TestUtils.enqueueEmptyResponse(mockWebServer);

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectMessage(ACCOUNT_ID_TO);

        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    public void sendTransaction_ThirdQuery_NullResponse() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));
        TestUtils.enqueueEmptyResponse(mockWebServer);

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectMessage("transaction");

        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void sendTransaction_TooLongMemo() throws Exception {
        String tooLongMemo = "memo string can be only 21 characters";
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Memo cannot be longer that 21 bytes(UTF-8 characters)");
        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"), FEE, tooLongMemo);
        transactionSender.sendTransaction(transaction);
        assertThat(mockWebServer.getRequestCount(), equalTo(0));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void sendTransaction_NullAccount() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("account");
        Transaction transaction = transactionSender.buildTransaction(null, ACCOUNT_ID_TO, new BigDecimal("200"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void sendTransaction_NullPublicAddress() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("public address");
        Transaction transaction = transactionSender.buildTransaction(account, null, new BigDecimal("200"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void sendTransaction_NullAmount() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("amount");
        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, null, FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    public void sendTransaction_EmptyPublicAddress() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("public address");
        Transaction transaction = transactionSender.buildTransaction(account, "", new BigDecimal("200"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    public void sendTransaction_NegativeAmount() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Amount");
        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("-200"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    public void sendTransaction_NegativeFee() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Fee");
        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"), -FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    public void sendTransaction_AmountExceedNumOfDecimalPlaces() throws Exception {
        expectedEx.expect(IllegalAmountException.class);
        expectedEx.expectMessage("amount can't have more then 5 digits after the decimal point");
        Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("20.012345"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void sendTransaction_InvalidPublicIdLength() throws Exception {
        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(FormatException.class));
        expectedEx.expectMessage("public address");
        Transaction transaction = transactionSender.buildTransaction(account, "ABCDEF", new BigDecimal("200"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void sendTransaction_InvalidChecksumPublicId() throws Exception {
        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(FormatException.class));
        expectedEx.expectMessage("public address");
        Transaction transaction = transactionSender.buildTransaction(account, "GDKJAMCTGZGD6KM7RBEII6QUYAHQQUGERXKM3ESHBX2UUNTNAVNB3OG3", new BigDecimal("200"), FEE);
        transactionSender.sendTransaction(transaction);
    }

    @SuppressWarnings("SameParameterValue")
    private void testHttpResponseCode(int resCode) {
        try {
            Transaction transaction = transactionSender.buildTransaction(account, ACCOUNT_ID_TO, new BigDecimal("200"), FEE);
            transactionSender.sendTransaction(transaction);
            fail("Expected OperationFailedException");
        } catch (Exception ex) {
            Assert.assertThat(ex, is(instanceOf(OperationFailedException.class)));
            Assert.assertThat(ex.getCause(), is(instanceOf(HttpResponseException.class)));
            Assert.assertThat(((HttpResponseException) ex.getCause()).getStatusCode(), is(resCode));
        }
    }

}