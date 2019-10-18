package kin.base.requests;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import kin.base.Server;
import okhttp3.OkHttpClient;

import static org.junit.Assert.assertEquals;

public class LedgersRequestBuilderTest {

    private Server server;

    @Before
    public void before() {
        server = new Server("https://horizon-testnet.stellar.org", new OkHttpClient());
    }

    @Test
    public void testAccounts() {
        URI uri = server.ledgers()
                .limit(200)
                .order(RequestBuilder.Order.ASC)
                .buildUri();
        assertEquals("https://horizon-testnet.stellar.org/ledgers?limit=200&order=asc", uri.toString());
    }
}
