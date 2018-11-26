package kin.sdk;

import static junit.framework.Assert.assertNull;
import android.support.test.InstrumentationRegistry;
import java.math.BigDecimal;
import kin.sdk.exception.AccountDeletedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SuppressWarnings({"deprecation", "ConstantConditions"})
public class KinAccountTest {

    private static final String APP_ID = "1a2c";
    private static final int FEE = 100;

    private KinClient kinClient;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() {
        kinClient = new KinClient(InstrumentationRegistry.getTargetContext(), Environment.TEST, APP_ID);
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
