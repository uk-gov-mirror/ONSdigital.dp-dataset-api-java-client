package dp.api.dataset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockHttp {

    private static final ObjectMapper json = new ObjectMapper();


    public static CloseableHttpResponse response(int httpStatus) {

        CloseableHttpResponse mockHttpResponse = mock(CloseableHttpResponse.class);

        StatusLine mockResponseStatus = mock(StatusLine.class);
        when(mockResponseStatus.getStatusCode()).thenReturn(httpStatus);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockResponseStatus);

        return mockHttpResponse;
    }

    public static void responseBody(CloseableHttpResponse mockHttpResponse, Object responseBody) throws JsonProcessingException, UnsupportedEncodingException {
        String responseJSON = json.writeValueAsString(responseBody);
        when(mockHttpResponse.getEntity()).thenReturn(new StringEntity(responseJSON));
    }
}
