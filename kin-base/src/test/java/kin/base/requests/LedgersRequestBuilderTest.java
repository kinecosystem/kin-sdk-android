package kin.base.requests;

import org.junit.Test;

import java.net.URI;

import kin.base.Server;

import static org.junit.Assert.assertEquals;

public class LedgersRequestBuilderTest {
  @Test
  public void testAccounts() {
    Server server = new Server("https://horizon-testnet.stellar.org");
    URI uri = server.ledgers()
            .limit(200)
            .order(RequestBuilder.Order.ASC)
            .buildUri();
    assertEquals("https://horizon-testnet.stellar.org/ledgers?limit=200&order=asc", uri.toString());
  }
}
