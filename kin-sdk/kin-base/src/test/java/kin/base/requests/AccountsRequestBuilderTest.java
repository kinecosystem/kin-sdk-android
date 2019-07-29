package kin.base.requests;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import kin.base.Server;
import org.junit.Test;

public class AccountsRequestBuilderTest {
  @Test
  public void testAccounts() {
    Server server = new Server("https://horizon-testnet.stellar.org");
    URI uri = server.accounts()
            .cursor("13537736921089")
            .limit(200)
            .order(RequestBuilder.Order.ASC)
            .buildUri();
    assertEquals("https://horizon-testnet.stellar.org/accounts?cursor=13537736921089&limit=200&order=asc", uri.toString());
  }
}
