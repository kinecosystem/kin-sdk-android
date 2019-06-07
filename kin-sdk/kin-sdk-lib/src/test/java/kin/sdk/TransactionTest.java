package kin.sdk;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import kin.base.Account;
import kin.base.CreateAccountOperation;
import kin.base.KeyPair;
import kin.base.Network;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest = Config.NONE)
public class TransactionTest {

    private static final String ACCOUNT_ID_FROM = "GDJVKIY3UQKMTQZHXR36ZQUNFYO45XLM6IHWO6TYQ53M5KEXBNMJYWVR";
    private static final String SECRET_SEED_FROM = "SB73L5FFTZMN6FHTOOWYEBVFTLUWQEWBLSCI4WLZADRJWENDBYL6QD6P";

    private MockWebServer mockWebServer;
    private Transaction transaction;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockServer();
        Network.useTestNetwork();

        createTransaction();

    }

    private void createTransaction() {
        KeyPair source = KeyPair.fromSecretSeed(SECRET_SEED_FROM);
        KeyPair destination = KeyPair.fromAccountId(ACCOUNT_ID_FROM);
        long sequenceNumber = 2908908335136768L;
        Account account = new Account(source, sequenceNumber);
        kin.base.Transaction baseTransaction = new kin.base.Transaction.Builder(account)
            .addOperation(new CreateAccountOperation.Builder(destination, "2000").build())
            .addFee(100)
            .build();
        baseTransaction.sign(source);
        transaction = new Transaction(baseTransaction);
    }

    private void mockServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }


    @Test
    public void getTransactionEnvelope_success() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));

        String transactionEnvelope = "AAAAANNVIxukFMnDJ7x37MKNLh3O3WzyD2d6eId2zqiXC1icAAAAZAAKVaMAAAABAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAA01UjG6QUycMnvHfswo0uHc7dbPIPZ3p4h3bOqJcLWJwAAAAAC+vCAAAAAAAAAAABlwtYnAAAAEBEIhSY0BYSMy5yrYkIYes9AmH0v729nVGZ1nH8CukJ5rfWoYf6Ebo4hismcjv51xlulVuTRYILuW67ENoHn1YB";
        assertThat(kin.base.Transaction.fromEnvelopeXdr(transactionEnvelope).toEnvelopeXdrBase64(),
            equalTo(transactionEnvelope));
    }

    @Test
    public void decodeTransaction_success() throws Exception {
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_from.json"));
        mockWebServer.enqueue(TestUtils.generateSuccessMockResponse(this.getClass(), "tx_account_to.json"));

        String transactionEnvelope = "AAAAANNVIxukFMnDJ7x37MKNLh3O3WzyD2d6eId2zqiXC1icAAAAZAAKVaMAAAABAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAA01UjG6QUycMnvHfswo0uHc7dbPIPZ3p4h3bOqJcLWJwAAAAAC+vCAAAAAAAAAAABlwtYnAAAAEBEIhSY0BYSMy5yrYkIYes9AmH0v729nVGZ1nH8CukJ5rfWoYf6Ebo4hismcjv51xlulVuTRYILuW67ENoHn1YB";
        Transaction transaction = Transaction.decodeTransaction(transactionEnvelope);
        assertThat("GDJVKIY3UQKMTQZHXR36ZQUNFYO45XLM6IHWO6TYQ53M5KEXBNMJYWVR",
            equalTo(transaction.getSource().getAccountId()));
        assertThat(2908908335136769L, equalTo(transaction.getBaseTransaction().getSequenceNumber()));
        assertThat(100, equalTo(transaction.getFee()));
        assertThat(transactionEnvelope, equalTo(transaction.getTransactionEnvelope()));
    }

    @Test
    public void addSignature_success() {
        KinAccountImpl mockKinAccount = mock(KinAccountImpl.class);
        when(mockKinAccount.getKeyPair()).thenReturn(KeyPair.fromSecretSeed(SECRET_SEED_FROM));

        assertThat(transaction.getBaseTransaction().getSignatures().size(), equalTo(1));
        transaction.addSignature(mockKinAccount);
        assertThat(transaction.getBaseTransaction().getSignatures().size(), equalTo(2));
    }


}
