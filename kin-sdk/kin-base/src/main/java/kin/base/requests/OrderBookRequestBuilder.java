package kin.base.requests;

import com.google.gson.reflect.TypeToken;
import com.here.oksse.ServerSentEvent;

import java.io.IOException;
import java.net.URI;

import kin.base.Asset;
import kin.base.AssetTypeCreditAlphaNum;
import kin.base.responses.OrderBookResponse;
import okhttp3.OkHttpClient;

/**
 * Builds requests connected to order book.
 */
public class OrderBookRequestBuilder extends RequestBuilder {

    public OrderBookRequestBuilder(OkHttpClient httpClient, URI serverURI) {
        super(httpClient, serverURI, "order_book");
    }

    public OrderBookRequestBuilder buyingAsset(Asset asset) {
        uriBuilder.addEncodedQueryParameter("buying_asset_type", asset.getType());
        if (asset instanceof AssetTypeCreditAlphaNum) {
            AssetTypeCreditAlphaNum creditAlphaNumAsset = (AssetTypeCreditAlphaNum) asset;
            uriBuilder.addEncodedQueryParameter("buying_asset_code", creditAlphaNumAsset.getCode());
            uriBuilder.addEncodedQueryParameter("buying_asset_issuer", creditAlphaNumAsset.getIssuer().getAccountId());
        }
        return this;
    }

    public OrderBookRequestBuilder sellingAsset(Asset asset) {
        uriBuilder.addEncodedQueryParameter("selling_asset_type", asset.getType());
        if (asset instanceof AssetTypeCreditAlphaNum) {
            AssetTypeCreditAlphaNum creditAlphaNumAsset = (AssetTypeCreditAlphaNum) asset;
            uriBuilder.addEncodedQueryParameter("selling_asset_code", creditAlphaNumAsset.getCode());
            uriBuilder.addEncodedQueryParameter("selling_asset_issuer", creditAlphaNumAsset.getIssuer().getAccountId());
        }
        return this;
    }

    public static OrderBookResponse execute(OkHttpClient httpClient, URI uri)
            throws IOException, TooManyRequestsException {
        TypeToken type = new TypeToken<OrderBookResponse>() {
        };
        ResponseHandler<OrderBookResponse> responseHandler = new ResponseHandler<OrderBookResponse>(httpClient, type);
        return responseHandler.handleGetRequest(uri);
    }

    public OrderBookResponse execute() throws IOException, TooManyRequestsException {
        return this.execute(httpClient, this.buildUri());
    }

    @Override
    public RequestBuilder cursor(String cursor) {
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public RequestBuilder limit(int number) {
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public RequestBuilder order(Order direction) {
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public <ListenerType> ServerSentEvent stream(EventListener<ListenerType> listener) {
        return null;
    }
}
