package dp.api.dataset;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.protocol.HttpContext;

/**
 * Custom implementation of ServiceUnavailableRetryStrategy to retry any HTTP 5xx responses.
 */
public class RetryStrategy implements ServiceUnavailableRetryStrategy {

    private final int maxRetries;
    private final long retryIntervalMs;

    public RetryStrategy(int maxRetries, long retryIntervalMs) {
        this.maxRetries = maxRetries;
        this.retryIntervalMs = retryIntervalMs;
    }

    public RetryStrategy() {
        this(3, 20);
    }

    @Override
    public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
        return executionCount <= maxRetries &&
                response.getStatusLine().getStatusCode() >= HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }

    @Override
    public long getRetryInterval() {
        return retryIntervalMs;
    }
}
