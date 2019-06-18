package kin.sdk;

import kin.base.*;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest = Config.NONE)
public class TransactionTest {

    private static final String ACCOUNT_ID_FROM = "GDJVKIY3UQKMTQZHXR36ZQUNFYO45XLM6IHWO6TYQ53M5KEXBNMJYWVR";
    private static final String SECRET_SEED_FROM = "SB73L5FFTZMN6FHTOOWYEBVFTLUWQEWBLSCI4WLZADRJWENDBYL6QD6P";

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private MockWebServer mockWebServer;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockServer();
        Network.useTestNetwork();
    }

    private RawTransaction createTransaction() {
        KeyPair source = KeyPair.fromSecretSeed(SECRET_SEED_FROM);
        return buildRawTransaction(source, 100, MemoText.text("fake memo"));
    }

    private RawTransaction buildRawTransaction(KeyPair source, int fee, Memo memo) {
        KeyPair destination = KeyPair.fromAccountId(ACCOUNT_ID_FROM);
        long sequenceNumber = 2908908335136768L;
        Account account = new Account(source, sequenceNumber);
        return new TransactionBuilder(source, account, "test")
            .addOperation(new CreateAccountOperation.Builder(destination, "2000").build())
            .setFee(fee)
            .setMemo(memo)
            .build();
    }


    private void mockServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @Test
    public void getTransactionEnvelope_success() {
        RawTransaction transaction = createTransaction();
        String transactionEnvelope = "AAAAANNVIxukFMnDJ7x37MKNLh3O3WzyD2d6eId2zqiXC1icAAAAZAAKVaMAAAABAAAAAAAAAAEAAAAQMS10ZXN0LWZha2UgbWVtbwAAAAEAAAAAAAAAAAAAAADTVSMbpBTJwye8d+zCjS4dzt1s8g9neniHds6olwtYnAAAAAAL68IAAAAAAAAAAAGXC1icAAAAQFUTzwPlsEzibdVe1wjk9Bcz4TOsvY8FAc66aCfusHBEUa7vDyxwiV/ia79PWqhr+0vlXmkMI4xS14JVYcQ3Dwg=";

        assertThat(transaction.transactionEnvelope(), equalTo(transactionEnvelope));
    }

    @Test
    public void decodeTransaction_success() throws Exception {
        String transactionEnvelope = "AAAAANNVIxukFMnDJ7x37MKNLh3O3WzyD2d6eId2zqiXC1icAAAAZAAKVaMAAAABAAAAAAAAAAEAAAAQMS10ZXN0LWZha2UgbWVtbwAAAAEAAAAAAAAAAAAAAADTVSMbpBTJwye8d+zCjS4dzt1s8g9neniHds6olwtYnAAAAAAL68IAAAAAAAAAAAGXC1icAAAAQFUTzwPlsEzibdVe1wjk9Bcz4TOsvY8FAc66aCfusHBEUa7vDyxwiV/ia79PWqhr+0vlXmkMI4xS14JVYcQ3Dwg=";
        RawTransaction transaction = RawTransaction.decodeRawTransaction(transactionEnvelope);

        assertThat("GDJVKIY3UQKMTQZHXR36ZQUNFYO45XLM6IHWO6TYQ53M5KEXBNMJYWVR", equalTo(transaction.source()));
        assertThat(2908908335136769L, equalTo(transaction.sequenceNumber()));
        assertThat(100, equalTo(transaction.fee()));
        assertThat("1-test-fake memo", equalTo(((MemoText)transaction.memo()).getText()));
        assertThat("bdf973d35e8f45c6d498b4c4f282f7d6e79942f58a3caa01944f620b8f776f92", equalTo(transaction.id().id()));
        assertThat(1, equalTo(transaction.operations().length));
        assertThat(1, equalTo(transaction.signatures().size()));
        assertThat(transactionEnvelope, equalTo(transaction.transactionEnvelope()));

    }

    @Test
    public void decodeTransaction_OnePaymentOperation_getPaymentTransaction() throws Exception {
        KeyPair source = KeyPair.fromSecretSeed(SECRET_SEED_FROM);
        KeyPair destination = KeyPair.fromAccountId(ACCOUNT_ID_FROM);
        long sequenceNumber = 2908908335136768L;
        Account account = new Account(source, sequenceNumber);
        TransactionBase transactionBase = new TransactionBuilder(source, account, "test")
            .addOperation(new PaymentOperation.Builder(destination, new AssetTypeNative(), "1000").build())
            .setFee(100)
            .setMemo(MemoText.text("fake memo"))
            .build();

        TransactionBase decodedTransaction = TransactionBase.decodeTransaction(transactionBase.transactionEnvelope());
        assertThat(decodedTransaction, instanceOf(PaymentTransaction.class));
    }

    @Test
    public void decodeTransaction_moreThenOnePaymentOperation_getRawTransaction() throws Exception {
        KeyPair source = KeyPair.fromSecretSeed(SECRET_SEED_FROM);
        KeyPair destination = KeyPair.fromAccountId(ACCOUNT_ID_FROM);
        long sequenceNumber = 2908908335136768L;
        Account account = new Account(source, sequenceNumber);
        TransactionBase transactionBase = new TransactionBuilder(source, account, "test")
            .addOperation(new PaymentOperation.Builder(destination, new AssetTypeNative(), "1000").build())
            .addOperation(new PaymentOperation.Builder(destination, new AssetTypeNative(), "2000").build())
            .setFee(100)
            .setMemo(MemoText.text("fake memo"))
            .build();

        TransactionBase decodedTransaction = TransactionBase.decodeRawTransaction(transactionBase.transactionEnvelope());
        assertThat(decodedTransaction, instanceOf(RawTransaction.class));
    }

    @Test
    public void decodeTransaction_noPaymentOperation_getRawTransaction() throws Exception {
        String transactionEnvelope = "AAAAANNVIxukFMnDJ7x37MKNLh3O3WzyD2d6eId2zqiXC1icAAAAZAAKVaMAAAABAAAAAAAAAAEAAAAQMS10ZXN0LWZha2UgbWVtbwAAAAEAAAAAAAAAAAAAAADTVSMbpBTJwye8d+zCjS4dzt1s8g9neniHds6olwtYnAAAAAAL68IAAAAAAAAAAAGXC1icAAAAQFUTzwPlsEzibdVe1wjk9Bcz4TOsvY8FAc66aCfusHBEUa7vDyxwiV/ia79PWqhr+0vlXmkMI4xS14JVYcQ3Dwg=";
        RawTransaction transaction = TransactionBase.decodeRawTransaction(transactionEnvelope);

        assertThat(transaction, instanceOf(RawTransaction.class));
        assertThat(transactionEnvelope, equalTo(transaction.transactionEnvelope()));

    }

    @Test
    public void addSignature_success() {
        RawTransaction transaction = createTransaction();
        KinAccountImpl mockKinAccount = mock(KinAccountImpl.class);
        when(mockKinAccount.getKeyPair()).thenReturn(KeyPair.fromSecretSeed(SECRET_SEED_FROM));

        assertThat(transaction.signatures().size(), equalTo(1));
        transaction.addSignature(mockKinAccount);
        assertThat(transaction.signatures().size(), equalTo(2));
    }

    @Test
    public void getTransactionBuilder_buildSuccess() {
        RawTransaction transaction = createTransaction();
        assertThat(((MemoText) transaction.memo()).getText(), equalTo("1-test-fake memo"));
        assertThat(transaction.fee(), equalTo(100));
        assertThat(transaction.operations().length, equalTo(1));
        assertThat(transaction.operations()[0], instanceOf(CreateAccountOperation.class));
        assertThat(transaction.sequenceNumber(), equalTo(2908908335136769L));
        assertThat(transaction.signatures().size(), equalTo(1));
    }

    @Test
    public void getTransactionBuilder_memoNotValid_IllegalArgumentException() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Memo cannot be longer that 21 bytes(UTF-8 characters)");

        KeyPair source = KeyPair.fromSecretSeed(SECRET_SEED_FROM);
        buildRawTransaction(source, 100, MemoText.text("memo is not valid, to long"));
    }

    @Test
    public void getTransactionBuilder_noMemoText_buildSuccess() {
        KeyPair source = KeyPair.fromSecretSeed(SECRET_SEED_FROM);
        RawTransaction rawTransaction = buildRawTransaction(source, 100, MemoNone.none());
        assertThat(rawTransaction.memo(), instanceOf(MemoNone.class));
    }

    @Test
    public void getTransactionBuilder_feeIsNotValid_IllegalArgumentException() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Fee can't be negative");

        KeyPair source = KeyPair.fromSecretSeed(SECRET_SEED_FROM);
        buildRawTransaction(source,-10, MemoText.text("fake memo"));
    }

    @Test
    public void getTransactionBuilder_noOperations_IllegalArgumentException() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("At least one operation required");

        KeyPair source = KeyPair.fromSecretSeed(SECRET_SEED_FROM);
        long sequenceNumber = 2908908335136768L;
        Account account = new Account(source, sequenceNumber);
        new TransactionBuilder(source, account, "test")
            .setFee(100)
            .setMemo(MemoText.text("fake memo"))
            .build();
    }

}
