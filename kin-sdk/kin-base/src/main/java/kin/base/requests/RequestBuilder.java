package kin.base.requests;

import com.here.oksse.ServerSentEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * Abstract class for request builders.
 */
public abstract class RequestBuilder {

  protected final HttpUrl.Builder uriBuilder;
  protected final OkHttpClient httpClient;
  private final ArrayList<String> segments;
  private boolean segmentsAdded;

  RequestBuilder(OkHttpClient httpClient, URI serverURI, String ... defaultSegments) {
    uriBuilder = HttpUrl.parse(serverURI.toString()).newBuilder();
    segments = new ArrayList<String>();
    segments.addAll(Arrays.asList(defaultSegments));
    this.httpClient = httpClient;
  }

  protected RequestBuilder setSegments(String... segments) {
    if (segmentsAdded) {
      throw new RuntimeException("URL segments have been already added.");
    }
    segmentsAdded = true;
    // Remove default segments
    this.segments.clear();
    Collections.addAll(this.segments, segments);

    return this;
  }

  /**
   * Sets <code>cursor</code> parameter on the request.
   * A cursor is a value that points to a specific location in a collection of resources.
   * The cursor attribute itself is an opaque value meaning that users should not try to parse it.
   * @see <a href="https://www.stellar.org/developers/horizon/reference/resources/page.html">Page documentation</a>
   * @param cursor
   */
  public RequestBuilder cursor(String cursor) {
    uriBuilder.addQueryParameter("cursor", cursor);
    return this;
  }

  /**
   * Sets <code>limit</code> parameter on the request.
   * It defines maximum number of records to return.
   * For range and default values check documentation of the endpoint requested.
   * @param number maxium number of records to return
   */
  public RequestBuilder limit(int number) {
    uriBuilder.addQueryParameter("limit", String.valueOf(number));
    return this;
  }

  /**
   * Sets <code>order</code> parameter on the request.
   * @param direction {@link kin.base.requests.RequestBuilder.Order}
   */
  public RequestBuilder order(Order direction) {
    uriBuilder.addQueryParameter("order", direction.getValue());
    return this;
  }

  URI buildUri() {
    for (String segment : segments) {
      uriBuilder.addPathSegment(segment);
    }
    segments.clear();
    return URI.create(uriBuilder.build().toString());
  }

  /**
   * Represents possible <code>order</code> parameter values.
   */
  public enum Order {
    ASC("asc"),
    DESC("desc");
    private final String value;
    Order(String value) {
      this.value = value;
    }
    public String getValue() {
      return value;
    }
  }

  abstract public <ListenerType> ServerSentEvent stream(final EventListener<ListenerType> listener);
}
