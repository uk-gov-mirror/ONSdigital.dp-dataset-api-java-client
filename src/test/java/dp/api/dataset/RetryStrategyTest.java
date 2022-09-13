package dp.api.dataset;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RetryStrategyTest {

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_INTERVAL = 20;

    private final RetryStrategy retryStrategy = new RetryStrategy(MAX_RETRIES, RETRY_INTERVAL);

    @Test
    void testRetryStrategy_getRetryInterval() {

        assertEquals(RETRY_INTERVAL, retryStrategy.getRetryInterval());
    }

    @Test
    void testRetryStrategy_retryRequest() {

        HttpResponse httpResponse = MockHttp.response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        int executionCount = 1;
        HttpContext httpContext = new BasicHttpContext();

        boolean retryRequest = retryStrategy.retryRequest(httpResponse, executionCount, httpContext);
        assertTrue(retryRequest);
    }

    @Test
    void testRetryStrategy_retryRequest_countEqualsMaxRetries() {

        HttpResponse httpResponse = MockHttp.response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        int executionCount = MAX_RETRIES;
        HttpContext httpContext = new BasicHttpContext();

        boolean retryRequest = retryStrategy.retryRequest(httpResponse, executionCount, httpContext);
        assertTrue(retryRequest);
    }

    @Test
    void testRetryStrategy_retryRequest_not5xxError() {

        HttpResponse httpResponse = MockHttp.response(HttpStatus.SC_NOT_FOUND);
        int executionCount = 1;
        HttpContext httpContext = new BasicHttpContext();

        boolean retryRequest = retryStrategy.retryRequest(httpResponse, executionCount, httpContext);

        assertFalse(retryRequest);
    }

    @Test
    void testRetryStrategy_retryRequest_retriesExceeded() {

        HttpResponse httpResponse = MockHttp.response(HttpStatus.SC_NOT_FOUND);
        int executionCount = MAX_RETRIES + 1;
        HttpContext httpContext = new BasicHttpContext();

        boolean retryRequest = retryStrategy.retryRequest(httpResponse, executionCount, httpContext);

        assertFalse(retryRequest);
    }
}
