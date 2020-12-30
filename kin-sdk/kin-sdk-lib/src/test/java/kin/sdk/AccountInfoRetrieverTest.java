package kin.sdk;

import org.hamcrest.Matchers;
import org.hamcrest.beans.HasPropertyWithValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import kin.base.Server;
import kin.base.responses.HttpResponseException;
import kin.sdk.exception.AccountNotFoundException;
import kin.sdk.exception.OperationFailedException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest = Config.NONE)
public class AccountInfoRetrieverTest {

    private static final String ACCOUNT_ID = "GBQUCJ755LJBUFFKFZCTV7XFA6JUR5NAAEJF66SPCN3XROHVKSG3VVUY";

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private Server server;
    private MockWebServer mockWebServer;

    @Before
    public void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String url = mockWebServer.url("").toString();

        server = new Server(url);
    }

    @Test
    public void getBalance_Success() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "balance_res_success.json"));

        Balance balance = getBalance(ACCOUNT_ID);

        Assert.assertEquals("9999.9999800", balance.value().toPlainString());
    }

    @Test
    public void getStatus_Created_StatusCreated() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "balance_res_success.json"));

        int status = getStatus(ACCOUNT_ID);

        assertThat(status, equalTo(AccountStatus.CREATED));
    }

    @Test
    public void getBalance_VerifyQueryAccountID() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "balance_res_success.json"));

        getBalance(ACCOUNT_ID);

        assertThat(mockWebServer.takeRequest().getRequestUrl().toString(), containsString(ACCOUNT_ID));
    }

    @Test
    public void getBalance_IOException_OperationFailedException() throws Exception {
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(IOException.class));

        getBalance(ACCOUNT_ID);
    }

    @Test
    public void getStatus_IOException_OperationFailedException() throws Exception {
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(isA(IOException.class));

        getStatus(ACCOUNT_ID);
    }

    @Test
    public void getBalance_NullResponse_OperationFailedException() throws Exception {
        TestUtils.enqueueEmptyResponse(mockWebServer);

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectMessage(ACCOUNT_ID);

        getBalance(ACCOUNT_ID);
    }

    @Test
    public void getStatus_NullResponse_OperationFailedException() throws Exception {
        TestUtils.enqueueEmptyResponse(mockWebServer);

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectMessage(ACCOUNT_ID);

        getStatus(ACCOUNT_ID);
    }

    @Test
    public void getBalance_AccountNotExists_AccountNotFoundException() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
        );
        expectedEx.expect(AccountNotFoundException.class);
        expectedEx.expect(new HasPropertyWithValue<>("accountId", equalTo(ACCOUNT_ID)));

        getBalance(ACCOUNT_ID);
    }

    @Test
    public void getStatus_AccountNotExists_StatusNotCreated() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
        );

        int status = getStatus(ACCOUNT_ID);
        assertThat(status, equalTo(AccountStatus.NOT_CREATED));
    }

    @Test
    public void getBalance_HttpResponseError_OperationFailedException() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
        );

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(Matchers.<Throwable>instanceOf(HttpResponseException.class));
        getBalance(ACCOUNT_ID);
    }

    @Test
    public void getStatus_HttpResponseError_OperationFailedException() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
        );

        expectedEx.expect(OperationFailedException.class);
        expectedEx.expectCause(Matchers.<Throwable>instanceOf(HttpResponseException.class));
        getStatus(ACCOUNT_ID);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void getBalance_NullInput_IllegalArgumentException() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
        );
        expectedEx.expect(IllegalArgumentException.class);
        AccountInfoRetriever accountInfoRetriever = new AccountInfoRetriever(server);
        accountInfoRetriever.getBalance(null);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void getStatus_NullInput_IllegalArgumentException() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
        );
        expectedEx.expect(IllegalArgumentException.class);
        AccountInfoRetriever accountInfoRetriever = new AccountInfoRetriever(server);
        accountInfoRetriever.getStatus(null);
    }

    private Balance getBalance(String accountId) throws OperationFailedException {
        AccountInfoRetriever accountInfoRetriever = new AccountInfoRetriever(server);
        return accountInfoRetriever.getBalance(accountId);
    }

    private int getStatus(String accountId) throws OperationFailedException {
        AccountInfoRetriever accountInfoRetriever = new AccountInfoRetriever(server);
        return accountInfoRetriever.getStatus(accountId);
    }

}
