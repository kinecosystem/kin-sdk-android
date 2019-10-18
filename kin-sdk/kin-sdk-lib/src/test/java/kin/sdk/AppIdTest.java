package kin.sdk;


import org.junit.Assert;
import org.junit.Test;

import kin.base.Server;

public class AppIdTest {

    private static final String APP_ID = "1a2c";
    private static final String STORE_KEY_TEST = "test";

    private KinClientInternal createNewKinClient(String appId) {
        Server server = new Server(IntegConsts.TEST_NETWORK_URL, new KinOkHttpClientFactory("test").testClient);
        return new KinClientInternal(
                new FakeKeyStore(),
                new Environment(IntegConsts.TEST_NETWORK_URL, IntegConsts.TEST_NETWORK_ID),
                new TransactionSender(server, appId),
                new AccountInfoRetriever(server),
                new GeneralBlockchainInfoRetrieverImpl(server),
                new BlockchainEventsCreator(server),
                new BackupRestoreImpl(),
                appId
        );
    }

    @Test
    public void create_with_appId_is_valid() {
        createNewKinClient(APP_ID);
    }

    @Test
    public void create_with_null_appId_is_valid() {
        boolean failed = false;
        try {
            createNewKinClient(null);
        } catch (IllegalArgumentException e) {
            failed = true;
        }
        Assert.assertFalse(failed);
    }

    @Test
    public void create_with_empty_string_appId_is_valid() {
        boolean failed = false;
        try {
            createNewKinClient("");
        } catch (IllegalArgumentException e) {
            failed = true;
        }
        Assert.assertFalse(failed);
    }

    @Test
    public void create_with_invalid_appId_throws() {

        String[] invalidIds = new String[]{"A", "AB", "1", "a2", "ab", "$#", "$#@", "A?BC"};

        for (String invalidId : invalidIds) {
            boolean failed = false;
            try {
                createNewKinClient(invalidId);
            } catch (IllegalArgumentException e) {
                failed = true;
            }
            Assert.assertTrue(failed);
        }
    }
}
