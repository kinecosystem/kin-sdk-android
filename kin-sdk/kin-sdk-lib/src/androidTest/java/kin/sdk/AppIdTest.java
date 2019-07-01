package kin.sdk;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static kin.sdk.IntegConsts.TEST_NETWORK_ID;
import static kin.sdk.IntegConsts.TEST_NETWORK_URL;

import android.support.test.InstrumentationRegistry;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class AppIdTest {

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
        boolean failed = false;
        try {
            createNewKinClient(APP_ID);
        } catch (IllegalArgumentException e) {
            failed = true;
        }
        assertFalse(failed);
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

        String[] invalidIds = new String[] {"A", "AB", "1", "a2", "ab", "$#", "$#@"};

        for (int i = 0 ; i < invalidIds.length ; i++) {
            boolean failed = false;
            try {
                createNewKinClient(invalidIds[i]);
            } catch (IllegalArgumentException e) {
                failed = true;
            }
            assertTrue(failed);
        }
    }
}
