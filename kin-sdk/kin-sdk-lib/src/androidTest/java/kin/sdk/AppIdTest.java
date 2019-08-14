package kin.sdk;

import android.support.test.InstrumentationRegistry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static kin.sdk.IntegConsts.TEST_NETWORK_ID;
import static kin.sdk.IntegConsts.TEST_NETWORK_URL;

public class AppIdTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private static final String APP_ID = "1a2c";
    private static final String STORE_KEY_TEST = "test";

    private KinClient createNewKinClient(String appId) {
        return new KinClient(InstrumentationRegistry.getTargetContext(),
            new Environment(TEST_NETWORK_URL, TEST_NETWORK_ID),
            appId,
            STORE_KEY_TEST);
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
        assertFalse(failed);
    }

    @Test
    public void create_with_empty_string_appId_is_valid() {
        boolean failed = false;
        try {
            createNewKinClient("");
        } catch (IllegalArgumentException e) {
            failed = true;
        }
        assertFalse(failed);
    }

    @Test
    public void create_with_invalid_appId_throws() {

        String[] invalidIds = new String[] {"A", "AB", "1", "a2", "ab", "$#", "$#@", "A?BC"};

        for (String invalidId : invalidIds) {
            boolean failed = false;
            try {
                createNewKinClient(invalidId);
            } catch (IllegalArgumentException e) {
                failed = true;
            }
            assertTrue(failed);
        }
    }
}
