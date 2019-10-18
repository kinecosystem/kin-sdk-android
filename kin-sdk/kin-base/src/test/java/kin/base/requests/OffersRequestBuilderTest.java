package kin.base.requests;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import kin.base.KeyPair;
import kin.base.Server;
import okhttp3.OkHttpClient;

import static org.junit.Assert.assertEquals;

public class OffersRequestBuilderTest {

    private Server server;

    @Before
    public void before() {
        server = new Server("https://horizon-testnet.stellar.org", new OkHttpClient());
    }


    @Test
    public void testForAccount() {
        URI uri = server.offers()
                .forAccount(KeyPair.fromAccountId("GBRPYHIL2CI3FNQ4BXLFMNDLFJUNPU2HY3ZMFSHONUCEOASW7QC7OX2H"))
                .limit(200)
                .order(RequestBuilder.Order.DESC)
                .buildUri();
        assertEquals("https://horizon-testnet.stellar.org/accounts/GBRPYHIL2CI3FNQ4BXLFMNDLFJUNPU2HY3ZMFSHONUCEOASW7QC7OX2H/offers?limit=200&order=desc", uri.toString());
    }
}
