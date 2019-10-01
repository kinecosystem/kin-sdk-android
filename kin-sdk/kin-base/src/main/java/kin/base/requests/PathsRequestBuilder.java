package kin.base.requests;

import com.google.gson.reflect.TypeToken;
import com.here.oksse.ServerSentEvent;
import java.io.IOException;
import java.net.URI;
import kin.base.Asset;
import kin.base.AssetTypeCreditAlphaNum;
import kin.base.KeyPair;
import kin.base.responses.Page;
import kin.base.responses.PathResponse;
import okhttp3.OkHttpClient;

/**
 * Builds requests connected to paths.
 */
public class PathsRequestBuilder extends RequestBuilder {

  public PathsRequestBuilder(OkHttpClient httpClient, URI serverURI) {
    super(httpClient, serverURI, "paths");
  }

  public PathsRequestBuilder destinationAccount(KeyPair account) {
    uriBuilder.addEncodedQueryParameter("destination_account", account.getAccountId());
    return this;
  }

  public PathsRequestBuilder sourceAccount(KeyPair account) {
    uriBuilder.addEncodedQueryParameter("source_account", account.getAccountId());
    return this;
  }

  public PathsRequestBuilder destinationAmount(String amount) {
    uriBuilder.addEncodedQueryParameter("destination_amount", amount);
    return this;
  }

  public PathsRequestBuilder destinationAsset(Asset asset) {
    uriBuilder.addEncodedQueryParameter("destination_asset_type", asset.getType());
    if (asset instanceof AssetTypeCreditAlphaNum) {
      AssetTypeCreditAlphaNum creditAlphaNumAsset = (AssetTypeCreditAlphaNum) asset;
      uriBuilder.addEncodedQueryParameter("destination_asset_code", creditAlphaNumAsset.getCode());
      uriBuilder.addEncodedQueryParameter("destination_asset_issuer", creditAlphaNumAsset.getIssuer().getAccountId());
    }
    return this;
  }

  /**
   * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
   * @throws IOException
   */
  public static Page<PathResponse> execute(OkHttpClient httpClient, URI uri)
      throws IOException, TooManyRequestsException {
    TypeToken type = new TypeToken<Page<PathResponse>>() {};
    ResponseHandler<Page<PathResponse>> responseHandler = new ResponseHandler<Page<PathResponse>>(httpClient, type);
    return responseHandler.handleGetRequest(uri);
  }

  /**
   * @throws TooManyRequestsException when too many requests were sent to the Horizon server.
   * @throws IOException
   */
  public Page<PathResponse> execute() throws IOException, TooManyRequestsException {
    return this.execute(httpClient, this.buildUri());
  }

  @Override
  public <ListenerType> ServerSentEvent stream(EventListener<ListenerType> listener) {
    return null;
  }
}
