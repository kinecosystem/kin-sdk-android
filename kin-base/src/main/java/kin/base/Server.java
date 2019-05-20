package kin.base;

import android.net.Uri;
import android.net.Uri.Builder;
import android.text.TextUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import kin.base.requests.AccountsRequestBuilder;
import kin.base.requests.EffectsRequestBuilder;
import kin.base.requests.LedgersRequestBuilder;
import kin.base.requests.OffersRequestBuilder;
import kin.base.requests.OperationsRequestBuilder;
import kin.base.requests.OrderBookRequestBuilder;
import kin.base.requests.PathsRequestBuilder;
import kin.base.requests.PaymentsRequestBuilder;
import kin.base.requests.TradesRequestBuilder;
import kin.base.requests.TransactionsRequestBuilder;
import kin.base.responses.GsonSingleton;
import kin.base.responses.SubmitTransactionResponse;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Main class used to connect to Horizon server.
 */
public class Server {

    private static final int TEMPORARY_REDIRECT = 307;
    private static final String LOCATION_HEADER = "Location";
    private static final String KIN_SDK_ANDROID_VERSION_HEADER = "kin-sdk-android-version";

    private URI serverURI;

    private OkHttpClient httpClient;

    /**
     * Creates server with input uri
     *
     * @param uri Horizon server uri
     */
    public Server(String uri) {
        createUri(uri);
        httpClient = new OkHttpClient();
    }

    /**
     * Creates server with input uri and timeout for transactions, i.e. {@link Server#submitTransaction(Transaction)}
     * <p>Increase timeout to prevent timeout exception for transaction with ledger close
     * time above default of 10 sec</p>
     *
     * @param uri Horizon server uri
     * @param transactionsTimeout transactions timeout value
     * @param timeUnit transactions timeout unit
     */
    public Server(String uri, int transactionsTimeout, TimeUnit timeUnit) {
        createUri(uri);
        httpClient = new OkHttpClient.Builder()
            .connectTimeout(transactionsTimeout, timeUnit)
            .writeTimeout(transactionsTimeout, timeUnit)
            .readTimeout(transactionsTimeout, timeUnit)
            .addInterceptor(new HeaderInterceptor())
            .build();
    }

    private void createUri(String uri) {
        try {
            serverURI = new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns {@link AccountsRequestBuilder} instance.
     */
    public AccountsRequestBuilder accounts() {
        return new AccountsRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link EffectsRequestBuilder} instance.
     */
    public EffectsRequestBuilder effects() {
        return new EffectsRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link LedgersRequestBuilder} instance.
     */
    public LedgersRequestBuilder ledgers() {
        return new LedgersRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link OffersRequestBuilder} instance.
     */
    public OffersRequestBuilder offers() {
        return new OffersRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link OperationsRequestBuilder} instance.
     */
    public OperationsRequestBuilder operations() {
        return new OperationsRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link OrderBookRequestBuilder} instance.
     */
    public OrderBookRequestBuilder orderBook() {
        return new OrderBookRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link TradesRequestBuilder} instance.
     */
    public TradesRequestBuilder trades() {
        return new TradesRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link PathsRequestBuilder} instance.
     */
    public PathsRequestBuilder paths() {
        return new PathsRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link PaymentsRequestBuilder} instance.
     */
    public PaymentsRequestBuilder payments() {
        return new PaymentsRequestBuilder(httpClient, serverURI);
    }

    /**
     * Returns {@link TransactionsRequestBuilder} instance.
     */
    public TransactionsRequestBuilder transactions() {
        return new TransactionsRequestBuilder(httpClient, serverURI);
    }

    /**
     * Submits transaction to the network.
     *
     * @param transaction transaction to submit to the network.
     * @param url if supplied then use this url instead of the serverURI member and don't append anything to it.
     * @return {@link SubmitTransactionResponse}
     */
    private SubmitTransactionResponse submitTransaction(Transaction transaction, String url) throws IOException {
        Uri transactionsUri = getUri(url);

        RequestBody formBody = new FormBody.Builder()
            .add("tx", transaction.toEnvelopeXdrBase64())
            .build();
        Request request = new Request.Builder()
            .url(transactionsUri.toString())
            .post(formBody)
            .build();

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();

            if (response != null) {
				String location = response.header(LOCATION_HEADER);
				if (response.code() == TEMPORARY_REDIRECT && location != null) {
                    return submitTransaction(transaction, location);
                } else {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseString = body.string();
                        return GsonSingleton.getInstance().fromJson(responseString, SubmitTransactionResponse.class);
                    }
                }
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return null;
    }

    /**
     * Submits transaction to the network.
     *
     * @param transaction transaction to submit to the network.
     * @return {@link SubmitTransactionResponse}
     */
    public SubmitTransactionResponse submitTransaction(Transaction transaction) throws IOException {
        return submitTransaction(transaction, null);
    }

    private Uri getUri(String url) {
        boolean urlIsEmpty = TextUtils.isEmpty(url);
        String serverUrl = urlIsEmpty ? serverURI.toString() : url;
        Builder uriBuilder = Uri.parse(serverUrl).buildUpon();
        if (urlIsEmpty) {
            uriBuilder.appendPath("transactions");
        }
        return uriBuilder.build();
    }

    /**
     * To support mocking a client
     */
    void setHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private class HeaderInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            request = request.newBuilder()
                .addHeader(KIN_SDK_ANDROID_VERSION_HEADER, BuildConfig.VERSION_NAME)
                .build();
            return chain.proceed(request);
        }
    }
}
