package kin.sdk;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;

import kin.base.Server;
import kin.sdk.exception.AccountDeletedException;

import static junit.framework.Assert.assertNull;

@SuppressWarnings({"deprecation", "ConstantConditions"})
public class KinAccountTest {

    private static final String APP_ID = "1a2c";
    private static final int FEE = 100;

    private KinClientInternal kinClient;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() {
        BackupRestore backupRestore = new BackupRestoreImpl();
        Server server = new Server(Environment.TEST.getNetworkUrl(), new KinOkHttpClientFactory(BuildConfig.VERSION_NAME).testClient);
        kinClient = new KinClientInternal(
                new FakeKeyStore(backupRestore),
                Environment.TEST,
                new TransactionSender(server, APP_ID),
                new AccountInfoRetriever(server),
                new GeneralBlockchainInfoRetrieverImpl(server),
                new BlockchainEventsCreator(server),
                backupRestore,
                APP_ID
        );
        kinClient.clearAllAccounts();
    }

    @After
    public void teardown() {
        kinClient.clearAllAccounts();
    }

    @Test(expected = AccountDeletedException.class)
    public void getBalanceSync_DeletedAccount_AccountDeletedException() throws Exception {
        KinAccount kinAccount = kinClient.addAccount();
        kinClient.deleteAccount(0);
        kinAccount.getBalanceSync();
    }

    @Test(expected = AccountDeletedException.class)
    public void getStatusSync_DeletedAccount_AccountDeletedException() throws Exception {
        KinAccount kinAccount = kinClient.addAccount();
        kinClient.deleteAccount(0);
        kinAccount.getStatusSync();
    }

    @Test(expected = AccountDeletedException.class)
    public void sendTransactionSync_DeletedAccount_AccountDeletedException() throws Exception {
        KinAccount kinAccount = kinClient.addAccount();
        kinClient.deleteAccount(0);
        Transaction transaction = kinAccount.buildTransactionSync("GBA2XHZRUAHEL4DZX7XNHR7HLBAUYPRNKLD2PIUKWV2LVVE6OJT4NDLM",
                new BigDecimal(10), FEE);
        kinAccount.sendTransactionSync(transaction);
    }

    @Test(expected = AccountDeletedException.class)
    public void sendWhitelistTransaction_DeletedAccount_AccountDeletedException() throws Exception {
        KinAccount kinAccount = kinClient.addAccount();
        kinClient.deleteAccount(0);
        Transaction transaction = kinAccount.buildTransactionSync("GBA2XHZRUAHEL4DZX7XNHR7HLBAUYPRNKLD2PIUKWV2LVVE6OJT4NDLM",
                new BigDecimal(10), 0);

        String whitelist = new WhitelistServiceForTest().whitelistTransaction(transaction.getWhitelistableTransaction());
        kinAccount.sendWhitelistTransactionSync(whitelist);
    }

    @Test
    public void getPublicAddress_DeletedAccount_EmptyPublicAddress() throws Exception {
        KinAccount kinAccount = kinClient.addAccount();
        kinClient.deleteAccount(0);
        assertNull(kinAccount.getPublicAddress());
    }

}
