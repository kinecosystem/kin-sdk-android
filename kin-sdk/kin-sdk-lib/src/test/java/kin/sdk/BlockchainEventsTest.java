package kin.sdk;

import static kin.sdk.TestUtils.loadResource;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import com.here.oksse.ServerSentEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import kin.base.KeyPair;
import kin.base.Network;
import kin.base.Server;
import kin.base.requests.TransactionsRequestBuilder;
import kin.base.responses.GsonSingleton;
import kin.base.responses.TransactionResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class BlockchainEventsTest {

    private static final String ACCOUNT_ID = "GBLUDU6Y6KVM5MCJWOLPVVSJEVICGEGXHOOHEAPWRSXJ7XVMBFKISOLR";
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    @Mock
    private Server server;
    @Mock
    private TransactionsRequestBuilder mockTransactionsRequestBuilder;
    @Mock
    private ServerSentEvent mockServerSentEvent;
    private BlockchainEvents blockchainEvents;
    private ConcurrentLinkedQueue<TransactionResponse> responsesQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean isCancelled = false;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        mockServer();
        Network.useTestNetwork();

        blockchainEvents = new BlockchainEvents(server, ACCOUNT_ID);
        createResponsesQueue();
    }

    private void mockServer() throws IOException {
        when(server.transactions()).thenReturn(mockTransactionsRequestBuilder);
        when(mockTransactionsRequestBuilder.forAccount((KeyPair) any())).thenReturn(mockTransactionsRequestBuilder);
        when(mockTransactionsRequestBuilder.cursor(anyString())).thenReturn(mockTransactionsRequestBuilder);
    }

    //using MockWebServer to mock real network responses is the ideal, unfortunately, streaming mocking
    // is not supported by MockWebServer
    private void createResponsesQueue() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                isCancelled = true;
                return null;
            }
        }).when(mockServerSentEvent).close();
        when(mockTransactionsRequestBuilder
            .stream(ArgumentMatchers.<kin.base.requests.EventListener<TransactionResponse>>any()))
            .then(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    final kin.base.requests.EventListener<TransactionResponse> listener = invocation
                        .getArgument(0);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!isCancelled) {
                                TransactionResponse response = responsesQueue.poll();
                                if (response == null) {
                                    sleep();
                                } else {
                                    listener.onEvent(response);
                                }
                            }
                        }

                        private void sleep() {
                            try {
                                Thread.sleep(250);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    return mockServerSentEvent;
                }
            });
    }

    private void enqueueTransactionsResponses() throws InterruptedException {
        responsesQueue.add(createTransactionResponse("payment_listener_tx_response1.json"));
        responsesQueue.add(createTransactionResponse("payment_listener_tx_response2.json"));
    }

    private void enqueueCreateAccountResponses() throws InterruptedException {
        responsesQueue.add(createTransactionResponse("create_account_tx_response1.json"));
    }

    private TransactionResponse createTransactionResponse(String res) {
        return GsonSingleton.getInstance()
            .fromJson(loadResource(BlockchainEventsTest.this.getClass(), res),
                TransactionResponse.class);
    }

    @Test
    public void addPaymentListener() throws Exception {
        enqueueTransactionsResponses();

        final CountDownLatch latch = new CountDownLatch(1);
        final List<PaymentInfo> actualResults = new ArrayList<>();
        blockchainEvents.addPaymentListener(new EventListener<PaymentInfo>() {
            @Override
            public void onEvent(PaymentInfo data) {
                actualResults.add(data);
                if (actualResults.size() == 2) {
                    latch.countDown();
                }
            }
        });
        assertTrue(latch.await(1, TimeUnit.SECONDS));

        assertThat(actualResults.size(), equalTo(2));
        PaymentInfo payment1 = actualResults.get(0);
        PaymentInfo payment2 = actualResults.get(1);
        assertThat(payment1.hash().id(),
            equalTo("3eb3024a9c03451e7c8b8d3ba525a3a241e286cb694a262444d46d92e7605f22"));
        assertThat(payment1.sourcePublicKey(), equalTo("GBLUDU6Y6KVM5MCJWOLPVVSJEVICGEGXHOOHEAPWRSXJ7XVMBFKISOLR"));
        assertThat(payment1.destinationPublicKey(),
            equalTo("GANPYEGVH3ZVQFMQVVYFRP7U3HKE5LIJ3345ORI26G2OAX7HV66VIE7F"));
        assertThat(payment1.amount(), equalTo(new BigDecimal("250")));
        assertThat(payment1.createdAt(), equalTo("2018-11-19T15:59:07Z"));
        assertThat(payment1.fee(), equalTo(100L));
        assertThat(payment1.memo(), equalTo("1-test-test1"));

        assertThat(payment2.hash().id(),
            equalTo("c4ad29472150a741c0924086a76fea1aac326261afacee05c0b36be7e8fb5727"));
        assertThat(payment2.sourcePublicKey(), equalTo("GBLUDU6Y6KVM5MCJWOLPVVSJEVICGEGXHOOHEAPWRSXJ7XVMBFKISOLR"));
        assertThat(payment2.destinationPublicKey(),
            equalTo("GANPYEGVH3ZVQFMQVVYFRP7U3HKE5LIJ3345ORI26G2OAX7HV66VIE7F"));
        assertThat(payment2.amount(), equalTo(new BigDecimal("250")));
        assertThat(payment2.createdAt(), equalTo("2018-11-19T16:36:42Z"));
        assertThat(payment2.fee(), equalTo(100L));
        assertThat(payment2.memo(), equalTo("1-test-test1"));
    }

    @Test
    public void addAccountCreationListener() throws Exception {
        enqueueCreateAccountResponses();

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] eventFired = {false};
        blockchainEvents.addAccountCreationListener(new EventListener<Void>() {
            @Override
            public void onEvent(Void data) {
                eventFired[0] = true;
                latch.countDown();
            }
        });
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertThat(eventFired[0], equalTo(true));
    }

    @Test
    public void addPaymentListener_StopListener_NoEvents() throws Exception {
        enqueueTransactionsResponses();
        final CountDownLatch latch = new CountDownLatch(1);
        final int[] eventsCount = {0};
        ListenerRegistration listenerRegistration = blockchainEvents
            .addPaymentListener(new EventListener<PaymentInfo>() {
                @Override
                public void onEvent(PaymentInfo data) {
                    eventsCount[0]++;
                    if (eventsCount[0] == 2) {
                        latch.countDown();
                    }
                }
            });
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        listenerRegistration.remove();
        enqueueTransactionsResponses();
        enqueueTransactionsResponses();
        Thread.sleep(500);
        assertThat(eventsCount[0], equalTo(2));
    }

    @Test
    public void addAccountCreationListener_StopListener_NoEvents() throws Exception {
        final int[] eventsCount = {0};
        ListenerRegistration listenerRegistration = blockchainEvents
            .addAccountCreationListener(new EventListener<Void>() {
                @Override
                public void onEvent(Void data) {
                    eventsCount[0]++;
                }
            });
        listenerRegistration.remove();
        enqueueCreateAccountResponses();
        Thread.sleep(500);
        assertThat(eventsCount[0], equalTo(0));
    }

    @Test
    public void addAccountCreationListener_MultipleTransactions_SingleEvent() throws Exception {
        enqueueCreateAccountResponses();
        enqueueTransactionsResponses();

        final int[] eventsCount = {0};
        blockchainEvents.addAccountCreationListener(new EventListener<Void>() {
            @Override
            public void onEvent(Void data) {
                eventsCount[0]++;
            }
        });
        Thread.sleep(500);
        assertThat(eventsCount[0], equalTo(1));
    }

    @Test
    public void addBalanceListener() throws Exception {
        enqueueTransactionsResponses();

        final CountDownLatch latch = new CountDownLatch(1);
        final List<Balance> actualResults = new ArrayList<>();
        blockchainEvents.addBalanceListener(new EventListener<Balance>() {
            @Override
            public void onEvent(Balance data) {
                actualResults.add(data);
                if (actualResults.size() == 2) {
                    latch.countDown();
                }
            }
        });
        assertTrue(latch.await(1, TimeUnit.SECONDS));

        assertThat(actualResults.size(), equalTo(2));
        Balance balance1 = actualResults.get(0);
        Balance balance2 = actualResults.get(1);

        assertThat(balance1, notNullValue());
        assertThat(balance2, notNullValue());
        //expected balances values are the ones encoded at transactions responses jsons (see enqueueTransactionsResponses)
        assertThat(balance1.value(), equalTo(new BigDecimal("11549.998")));
        assertThat(balance2.value(), equalTo(new BigDecimal("11299.997")));
    }

    @Test
    public void addBalanceListener_StopListener_NoEvents() throws Exception {
        final int[] eventsCount = {0};
        ListenerRegistration listenerRegistration = blockchainEvents
            .addBalanceListener(new EventListener<Balance>() {
                @Override
                public void onEvent(Balance data) {
                    eventsCount[0]++;
                }
            });
        listenerRegistration.remove();
        enqueueCreateAccountResponses();
        Thread.sleep(500);
        assertThat(eventsCount[0], equalTo(0));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void addPaymentListener_NullListener_IllegalArgumentException() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("listener");
        blockchainEvents.addPaymentListener(null);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void addAccountCreationListener_NullListener_IllegalArgumentException() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("listener");
        blockchainEvents.addAccountCreationListener(null);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void addBalanceListener_NullListener_IllegalArgumentException() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("listener");
        blockchainEvents.addBalanceListener(null);
    }
}
