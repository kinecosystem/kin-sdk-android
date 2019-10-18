package kin.base.requests;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import kin.base.Asset;
import kin.base.KeyPair;
import kin.base.Server;
import okhttp3.OkHttpClient;

import static org.junit.Assert.assertEquals;

public class OrderBookRequestBuilderTest {

    private Server server;

    @Before
    public void before() {
        server = new Server("https://horizon-testnet.stellar.org", new OkHttpClient());
    }

    @Test
    public void testOrderBook() {
        URI uri = server.orderBook()
                .buyingAsset(Asset.createNonNativeAsset("EUR", KeyPair.fromAccountId("GAUPA4HERNBDPVO4IUA3MJXBCRRK5W54EVXTDK6IIUTGDQRB6D5W242W")))
                .sellingAsset(Asset.createNonNativeAsset("USD", KeyPair.fromAccountId("GDRRHSJMHXDTQBT4JTCILNGF5AS54FEMTXL7KOLMF6TFTHRK6SSUSUZZ")))
                .buildUri();

        assertEquals(
                "https://horizon-testnet.stellar.org/order_book?" +
                        "buying_asset_type=credit_alphanum4&" +
                        "buying_asset_code=EUR&" +
                        "buying_asset_issuer=GAUPA4HERNBDPVO4IUA3MJXBCRRK5W54EVXTDK6IIUTGDQRB6D5W242W&" +
                        "selling_asset_type=credit_alphanum4&" +
                        "selling_asset_code=USD&" +
                        "selling_asset_issuer=GDRRHSJMHXDTQBT4JTCILNGF5AS54FEMTXL7KOLMF6TFTHRK6SSUSUZZ",
                uri.toString());
    }
}
