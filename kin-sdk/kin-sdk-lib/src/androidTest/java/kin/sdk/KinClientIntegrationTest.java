package kin.sdk;


import android.support.test.InstrumentationRegistry;
import kin.sdk.exception.CreateAccountException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static junit.framework.Assert.*;
import static kin.sdk.IntegConsts.TEST_NETWORK_ID;
import static kin.sdk.IntegConsts.TEST_NETWORK_URL;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;

@SuppressWarnings("deprecation")
public class KinClientIntegrationTest {

    private static final String APP_ID = "1a2c";
    private static final String STORE_KEY_TEST1 = "test";
    private static final String STORE_KEY_TEST2 = "test2";
    private Environment environment;
    private KinClient kinClient1;
    private KinClient kinClient2;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() {
        environment = new Environment(TEST_NETWORK_URL, TEST_NETWORK_ID);
        kinClient1 = createNewKinClient(STORE_KEY_TEST1);
        kinClient2 = createNewKinClient(STORE_KEY_TEST2);
        kinClient1.clearAllAccounts();
        kinClient2.clearAllAccounts();
    }

    private KinClient createNewKinClient(String storeKey) {
        return new KinClient(InstrumentationRegistry.getTargetContext(), environment, APP_ID, storeKey);
    }

    @After
    public void teardown() {
        kinClient1 = createNewKinClient(STORE_KEY_TEST1);
        kinClient2 = createNewKinClient(STORE_KEY_TEST2);
        kinClient1.clearAllAccounts();
        kinClient2.clearAllAccounts();
    }

    @Test
    public void addAccount_NewAccount() throws Exception {
        KinAccount kinAccount = kinClient1.addAccount();

        assertNotNull(kinAccount);
        assertThat(kinAccount.getPublicAddress(), not(isEmptyOrNullString()));
    }

    @Test
    public void createAccount_AddMultipleAccount() throws Exception {
        KinAccount kinAccount = kinClient1.addAccount();
        KinAccount kinAccount2 = kinClient1.addAccount();

        assertNotNull(kinAccount);
        assertNotNull(kinAccount2);
        assertThat(kinAccount.getPublicAddress(), not(isEmptyOrNullString()));
        assertThat(kinAccount2.getPublicAddress(), not(isEmptyOrNullString()));
        assertThat(kinAccount, not(equalTo(kinAccount2)));
    }

    @Test
    public void getAccount_AddMultipleAccount() throws Exception {
        KinAccount kinAccount = kinClient1.addAccount();
        KinAccount kinAccount2 = kinClient1.addAccount();

        KinAccount expectedAccount2 = kinClient1.getAccount(1);
        KinAccount expectedAccount1 = kinClient1.getAccount(0);

        assertNotNull(kinAccount);
        assertNotNull(kinAccount2);
        assertNotNull(expectedAccount1);
        assertNotNull(expectedAccount2);
        assertThat(kinAccount, equalTo(expectedAccount1));
        assertThat(kinAccount2, equalTo(expectedAccount2));
    }

    @Test
    public void getAccount_ExistingAccount_AddMultipleAccount() throws Exception {
        KinAccount kinAccount1 = kinClient1.addAccount();
        buildKinClient();
        KinAccount kinAccount2 = kinClient1.addAccount();
        KinAccount kinAccount3 = kinClient1.addAccount();

        KinAccount expectedAccount3 = kinClient1.getAccount(2);
        KinAccount expectedAccount2 = kinClient1.getAccount(1);
        KinAccount expectedAccount1 = kinClient1.getAccount(0);

        assertNotNull(expectedAccount1);
        assertNotNull(expectedAccount2);
        assertNotNull(expectedAccount3);
        assertThat(kinAccount1, equalTo(expectedAccount1));
        assertThat(kinAccount2, equalTo(expectedAccount2));
        assertThat(kinAccount3, equalTo(expectedAccount3));
    }

    @Test
    public void getAccount_ExistingMultipleAccount() throws Exception {
        KinAccount kinAccount1 = kinClient1.addAccount();
        KinAccount kinAccount2 = kinClient1.addAccount();
        buildKinClient();
        KinAccount expectedAccount2 = kinClient1.getAccount(1);
        KinAccount expectedAccount1 = kinClient1.getAccount(0);

        assertNotNull(expectedAccount1);
        assertNotNull(expectedAccount2);
        assertThat(kinAccount1, equalTo(expectedAccount1));
        assertThat(kinAccount2, equalTo(expectedAccount2));
    }


    @Test
    public void getAccount_NegativeIndex() throws Exception {
        kinClient1.addAccount();

        assertNull(kinClient1.getAccount(-1));
    }

    @Test
    public void getAccount_EmptyKeyStore_Null() throws Exception {
        KinAccount kinAccount = kinClient1.getAccount(0);

        assertNull(kinAccount);
    }

    @Test
    public void getAccount_ExistingAccount_SameAccount() throws Exception {
        KinAccount kinAccount1 = kinClient1.addAccount();
        buildKinClient();

        KinAccount kinAccount = kinClient1.getAccount(0);

        assertEquals(kinAccount1, kinAccount);
    }

    @Test
    public void hasAccount_EmptyKeyStore_False() throws Exception {
        assertFalse(kinClient1.hasAccount());
    }

    @Test
    public void hasAccount_ExistingAccount_True() throws Exception {
        kinClient1.addAccount();
        buildKinClient();

        assertTrue(kinClient1.hasAccount());
    }

    @Test
    public void hasAccount_ExistingMultipleAccounts_True() throws Exception {
        kinClient1.addAccount();
        kinClient1.addAccount();
        buildKinClient();

        assertTrue(kinClient1.hasAccount());
    }

    @Test
    public void deleteAccount() throws Exception {
        kinClient1.addAccount();
        buildKinClient();

        assertTrue(kinClient1.hasAccount());
        kinClient1.deleteAccount(0);
        assertFalse(kinClient1.hasAccount());
    }

    private void buildKinClient() {
        kinClient1 = new KinClient(InstrumentationRegistry.getTargetContext(), environment, APP_ID, STORE_KEY_TEST1);
    }

    @Test
    public void deleteAccount_MultipleAccounts() throws Exception {
        kinClient1.addAccount();
        kinClient1.addAccount();
        buildKinClient();

        kinClient1.deleteAccount(0);

        assertTrue(kinClient1.hasAccount());
        kinClient1.deleteAccount(0);
        assertFalse(kinClient1.hasAccount());
    }

    @Test
    public void deleteAccount_atEndOfList() throws Exception {
        KinAccount kinAccount1 = kinClient1.addAccount();
        kinClient1.addAccount();
        buildKinClient();

        kinClient1.deleteAccount(1);

        assertTrue(kinClient1.hasAccount());
        assertThat(kinAccount1, equalTo(kinClient1.getAccount(0)));
    }

    @Test
    public void deleteAccount_NegativeIndex() throws Exception {
        kinClient1.addAccount();
        kinClient1.addAccount();

        kinClient1.deleteAccount(-1);

        assertNotNull(kinClient1.getAccount(0));
        assertNotNull(kinClient1.getAccount(1));
        assertThat(kinClient1.getAccountCount(), equalTo(2));
    }

    @Test
    public void getAccountCount() throws Exception {
        kinClient1.addAccount();
        kinClient1.addAccount();
        kinClient1.addAccount();
        buildKinClient();

        assertThat(kinClient1.getAccountCount(), equalTo(3));
        kinClient1.deleteAccount(2);
        kinClient1.deleteAccount(1);
        assertThat(kinClient1.getAccountCount(), equalTo(1));
        kinClient1.addAccount();
        assertThat(kinClient1.getAccountCount(), equalTo(2));
        kinClient1.deleteAccount(1);
        kinClient1.deleteAccount(0);
        assertThat(kinClient1.getAccountCount(), equalTo(0));
    }

    @Test
    public void clearAllAccounts() throws CreateAccountException {
        kinClient1.addAccount();
        kinClient1.addAccount();
        kinClient1.addAccount();
        buildKinClient();

        kinClient1.clearAllAccounts();

        assertThat(kinClient1.getAccountCount(), equalTo(0));
    }

    @Test
    public void getEnvironment() throws Exception {
        String url = "https://www.myawesomeserver.com";
        Environment environment = new Environment(url, Environment.TEST.getNetworkPassphrase());
        kinClient1 = new KinClient(InstrumentationRegistry.getTargetContext(), environment, APP_ID, STORE_KEY_TEST1);
        Environment actualEnvironment = kinClient1.getEnvironment();

        assertNotNull(actualEnvironment);
        assertFalse(actualEnvironment.isMainNet());
        assertEquals(url, actualEnvironment.getNetworkUrl());
        assertEquals(Environment.TEST.getNetworkPassphrase(), actualEnvironment.getNetworkPassphrase());
    }

    @Test
    public void multipleKinClients_AddAccount_DifferentAccounts() throws Exception {
        KinAccount account = kinClient1.addAccount();

        kinClient2 = createNewKinClient(STORE_KEY_TEST2);
        assertThat(kinClient2.getAccount(0), nullValue());

        KinAccount account2 = kinClient2.addAccount();

        kinClient1 = createNewKinClient(STORE_KEY_TEST1);
        assertThat(kinClient1.getAccount(0), equalTo(account));
        assertThat(kinClient2.getAccount(0), equalTo(account2));
        assertThat(account, not(equalTo(account2)));
    }

    @Test
    public void multipleKinClients_DeleteAccount_DifferentAccounts() throws Exception {
        KinAccount account = kinClient2.addAccount();

        kinClient1.clearAllAccounts();
        kinClient2 = createNewKinClient(STORE_KEY_TEST2);

        assertThat(kinClient2.getAccount(0), equalTo(account));
    }

    @Test
    public void backupRestore() throws Exception {
        for (int i = 0; i < 50; i++) {
            KinAccount kinAccount = kinClient1.addAccount();
            String uuid = UUID.randomUUID().toString();
            String exported = kinAccount.export(uuid);
            KinAccount kinAccount2 = kinClient2.importAccount(exported, uuid);
            assertThat(kinAccount2.getPublicAddress(), equalTo(kinAccount.getPublicAddress()));
        }
    }

    @Test
    public void importAccount_CreateKinClientAgain_AddOnlyIfNotExists() throws Exception {
        kinClient1.addAccount();
        kinClient1.addAccount();
        kinClient1.addAccount();
        KinAccount kinAccount = kinClient1.addAccount();
        String passphrase = UUID.randomUUID().toString();
        String exported = kinAccount.export(passphrase);
        kinClient1.importAccount(exported, passphrase);
        KinClient anotherKinClient = createNewKinClient(STORE_KEY_TEST1);
        assertEquals(4, anotherKinClient.getAccountCount());
        assertEquals(4, kinClient1.getAccountCount());
    }

    @Test
    public void importAccount_AddOnlyIfNotExists() throws Exception {
        kinClient1.addAccount();
        kinClient1.addAccount();
        kinClient1.addAccount();
        KinAccount kinAccount = kinClient1.addAccount();
        String passphrase = UUID.randomUUID().toString();
        String exported = kinAccount.export(passphrase);
        kinClient1.importAccount(exported, passphrase);
        assertEquals(4, kinClient1.getAccountCount());
    }

    @Test
    public void importAccount_AddNewAccount() throws Exception {
        KinAccount kinAccount = kinClient1.addAccount();
        String passphrase = UUID.randomUUID().toString();
        String exported = kinAccount.export(passphrase);
        kinClient1.importAccount(exported, passphrase);
        assertEquals(1, kinClient1.getAccountCount());

        kinClient1.clearAllAccounts();
        assertEquals(0, kinClient1.getAccountCount());

        kinClient1.importAccount(exported, passphrase);
        assertEquals(1, kinClient1.getAccountCount());
    }

    @Test
    public void createMultipleKinClients_DeleteFormFirstAddFromSecond_SameAccounts() throws Exception {
        KinAccount kinAccount1 = kinClient1.addAccount();
        kinClient1.addAccount();

        KinClient anotherKinClientSameStoreKey = createNewKinClient(STORE_KEY_TEST1);
        KinAccount kinAccount3 = anotherKinClientSameStoreKey.addAccount();
        KinAccount kinAccount4 = anotherKinClientSameStoreKey.addAccount();

        kinClient1.deleteAccount(1);
        KinAccount kinAccount5 = anotherKinClientSameStoreKey.addAccount();
        KinAccount kinAccount6 = anotherKinClientSameStoreKey.addAccount();

        String expectedAccount1 = kinAccount1.getPublicAddress();
        String expectedAccount3 = kinAccount3.getPublicAddress();
        String expectedAccount4 = kinAccount4.getPublicAddress();
        String expectedAccount5 = kinAccount5.getPublicAddress();
        String expectedAccount6 = kinAccount6.getPublicAddress();

        assertThat(kinClient1.getAccountCount(), equalTo(5));
        assertThat(anotherKinClientSameStoreKey.getAccountCount(), equalTo(5));

        assertThat(anotherKinClientSameStoreKey.getAccount(0).getPublicAddress(), equalTo(expectedAccount1));
        assertThat(anotherKinClientSameStoreKey.getAccount(1).getPublicAddress(), equalTo(expectedAccount3));
        assertThat(anotherKinClientSameStoreKey.getAccount(2).getPublicAddress(), equalTo(expectedAccount4));
        assertThat(anotherKinClientSameStoreKey.getAccount(3).getPublicAddress(), equalTo(expectedAccount5));
        assertThat(anotherKinClientSameStoreKey.getAccount(4).getPublicAddress(), equalTo(expectedAccount6));
    }

    @Test
    public void createMultipleKinClients_DeleteFromFirstAndThenFromSecond_SameAccounts() throws Exception {
        KinAccount kinAccount1 = kinClient1.addAccount();
        kinClient1.addAccount();
        kinClient1.addAccount();

        KinClient anotherKinClientSameStoreKey = createNewKinClient(STORE_KEY_TEST1);

        kinClient1.deleteAccount(1);
        anotherKinClientSameStoreKey.deleteAccount(1);

        assertThat(kinClient1.getAccountCount(), equalTo(1));
        assertThat(anotherKinClientSameStoreKey.getAccountCount(), equalTo(1));

        assertThat(kinClient1.getAccount(0).getPublicAddress(), equalTo(kinAccount1.getPublicAddress()));
        assertThat(anotherKinClientSameStoreKey.getAccount(0).getPublicAddress(),
                equalTo(kinAccount1.getPublicAddress()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void initKinClient_NullId_Exception() {
        kinClient1 = createNewKinClient(null);
    }
}
