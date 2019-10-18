package kin.base.requests;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import kin.base.Asset;
import kin.base.KeyPair;
import kin.base.Server;
import okhttp3.OkHttpClient;

import static org.junit.Assert.assertEquals;

public class PathsRequestBuilderTest {

    private Server server;

    @Before
    public void before() {
        server = new Server("https://horizon-testnet.stellar.org", new OkHttpClient());
    }

    @Test
    public void testAccounts() {
        URI uri = server.paths()
                .destinationAccount(KeyPair.fromAccountId("GB24QI3BJNKBY4YNJZ2I37HFIYK56BL2OURFML76X46RQQKDLVT7WKJF"))
                .sourceAccount(KeyPair.fromAccountId("GD4KO3IOYYWIYVI236Y35K2DU6VNYRH3BPNFJSH57J5BLLCQHBIOK3IN"))
                .destinationAmount("20.50")
                .destinationAsset(Asset.createNonNativeAsset("USD", KeyPair.fromAccountId("GAYSHLG75RPSMXWJ5KX7O7STE6RSZTD6NE4CTWAXFZYYVYIFRUVJIBJH")))
                .cursor("13537736921089")
                .limit(200)
                .order(RequestBuilder.Order.ASC)
                .buildUri();

        assertEquals("https://horizon-testnet.stellar.org/paths?" +
                "destination_account=GB24QI3BJNKBY4YNJZ2I37HFIYK56BL2OURFML76X46RQQKDLVT7WKJF&" +
                "source_account=GD4KO3IOYYWIYVI236Y35K2DU6VNYRH3BPNFJSH57J5BLLCQHBIOK3IN&" +
                "destination_amount=20.50&" +
                "destination_asset_type=credit_alphanum4&" +
                "destination_asset_code=USD&" +
                "destination_asset_issuer=GAYSHLG75RPSMXWJ5KX7O7STE6RSZTD6NE4CTWAXFZYYVYIFRUVJIBJH&" +
                "cursor=13537736921089&" +
                "limit=200&" +
                "order=asc", uri.toString());
    }
}
