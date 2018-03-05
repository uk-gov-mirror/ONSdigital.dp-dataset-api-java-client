package dp.api.dataset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dp.api.dataset.exception.DatasetNotFoundException;
import dp.api.dataset.exception.InstanceNotFoundException;
import dp.api.dataset.exception.UnexpectedResponseException;
import dp.api.dataset.model.Dataset;
import dp.api.dataset.model.DatasetResponse;
import dp.api.dataset.model.DatasetVersion;
import dp.api.dataset.model.Instance;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DatasetAPIClientTest {

    private static final ObjectMapper json = new ObjectMapper();

    private static final String authTokenHeaderName = "internal-token";

    private static final String datasetAPIURL = "";
    private static final String datasetAPIAuthToken = "12345";
    private static final String instanceID = "123";
    private static final String datasetID = "321";
    private static final String datasetTitle = "the dataset title";
    private static final String edition = "current";
    private static final String version = "1";

    @Test
    public void testDatasetAPI_invalidURI() {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);

        // Given an invalid URI
        String invalidURI = "{{}}";

        // When a new DatasetAPIClient is created
        // Then the expected exception is thrown
        assertThrows(URISyntaxException.class,
                () -> new DatasetAPIClient(invalidURI, datasetAPIAuthToken, mockHttpClient));
    }

    @Test
    public void testDatasetAPI_createDataset() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a new dataset
        Dataset dataset = createDataset();

        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_CREATED);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        DatasetResponse mockDatasetResponse = mockDatasetResponse(mockHttpResponse);
        Dataset expectedDataset = mockDatasetResponse.getNext();

        // When createDataset is called
        Dataset actualDataset = datasetAPIClient.createDataset(datasetID, dataset);

        HttpEntityEnclosingRequestBase httpRequest = captureHttpRequestWithBody(mockHttpClient);

        // Then the request should contain the authentication header
        String actualAuthToken = httpRequest.getFirstHeader(authTokenHeaderName).getValue();
        assertThat(actualAuthToken).isEqualTo(datasetAPIAuthToken);

        // Then the request should contain the provided dataset in the body
        String body = IOUtils.toString(httpRequest.getEntity().getContent());
        Dataset requestDataset = json.readValue(body, Dataset.class);

        assertThat(requestDataset.getId()).isEqualTo(datasetID);
        assertThat(requestDataset.getTitle()).isEqualTo(datasetTitle);

        // Then the response should be whats returned from the dataset API
        assertNotNull(actualDataset);
        assertThat(actualDataset.getId()).isEqualTo(expectedDataset.getId());
        assertThat(actualDataset.getTitle()).isEqualTo(expectedDataset.getTitle());
    }

    @Test
    public void testDatasetAPI_createDataset_internalError() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a request to the dataset API that returns a 500
        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        Dataset dataset = new Dataset();

        // When createDataset is called
        // Then the expected exception is thrown
        assertThrows(UnexpectedResponseException.class,
                () -> datasetAPIClient.createDataset(datasetID, dataset));
    }

    @Test
    public void testDatasetAPI_createDataset_emptyDatasetID() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given an empty dataset ID
        String datasetID = "";
        Dataset dataset = new Dataset();

        // When createDataset is called
        // Then the expected exception is thrown
        assertThrows(IllegalArgumentException.class,
                () -> datasetAPIClient.createDataset(datasetID, dataset));
    }

    @Test
    public void testDatasetAPI_getDataset() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a mock dataset response from the dataset API
        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_OK);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        DatasetResponse mockDatasetResponse = mockDatasetResponse(mockHttpResponse);
        Dataset expectedDataset = mockDatasetResponse.getNext();

        // When getDataset is called
        Dataset actualDataset = datasetAPIClient.getDataset(datasetID);

        assertNotNull(actualDataset);

        HttpRequestBase httpRequest = captureHttpRequest(mockHttpClient);

        // Then the request should contain the authentication header
        String actualAuthToken = httpRequest.getFirstHeader(authTokenHeaderName).getValue();
        assertThat(actualAuthToken).isEqualTo(datasetAPIAuthToken);

        // Then the response should be whats returned from the dataset API
        assertThat(actualDataset.getId()).isEqualTo(expectedDataset.getId());
        assertThat(actualDataset.getTitle()).isEqualTo(expectedDataset.getTitle());
    }

    @Test
    public void testDatasetAPI_getDataset_datasetNotFound() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a request to the dataset API that returns a 404
        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_NOT_FOUND);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        // When getDataset is called
        // Then the expected exception is thrown
        assertThrows(DatasetNotFoundException.class,
                () -> datasetAPIClient.getDataset(datasetID));
    }

    @Test
    public void testDatasetAPI_getDataset_internalError() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a request to the dataset API that returns a 500
        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        // When getDataset is called
        // Then the expected exception is thrown
        assertThrows(UnexpectedResponseException.class,
                () -> datasetAPIClient.getDataset(datasetID));
    }

    @Test
    public void testDatasetAPI_getDataset_emptyDatasetID() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given an empty dataset ID
        String datasetID = "";

        // When updateDataset is called
        // Then the expected exception is thrown
        assertThrows(IllegalArgumentException.class,
                () -> datasetAPIClient.getDataset(datasetID));
    }

    @Test
    public void testDatasetAPI_updateDataset() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given an updated dataset
        Dataset dataset = createDataset();

        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_OK);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        // When updateDataset is called
        datasetAPIClient.updateDataset(datasetID, dataset);

        HttpEntityEnclosingRequestBase httpRequest = captureHttpRequestWithBody(mockHttpClient);

        // Then the request should contain the authentication header
        String actualAuthToken = httpRequest.getFirstHeader(authTokenHeaderName).getValue();
        assertThat(actualAuthToken).isEqualTo(datasetAPIAuthToken);

        // Then the request should contain the provided dataset in the body
        String body = IOUtils.toString(httpRequest.getEntity().getContent());
        Dataset requestDataset = json.readValue(body, Dataset.class);

        assertThat(requestDataset.getId()).isEqualTo(dataset.getId());
        assertThat(requestDataset.getTitle()).isEqualTo(dataset.getTitle());
    }

    @Test
    public void testDatasetAPI_updateDataset_datasetNotFound() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a request to the dataset API that returns a 404
        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_NOT_FOUND);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        Dataset dataset = new Dataset();

        // When updateDataset is called
        // Then the expected exception is thrown
        assertThrows(DatasetNotFoundException.class,
                () -> datasetAPIClient.updateDataset(datasetID, dataset));
    }

    @Test
    public void testDatasetAPI_updateDataset_internalError() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a request to the dataset API that returns a 500
        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        Dataset dataset = new Dataset();

        // When updateDataset is called
        // Then the expected exception is thrown
        assertThrows(UnexpectedResponseException.class,
                () -> datasetAPIClient.updateDataset(datasetID, dataset));
    }

    @Test
    public void testDatasetAPI_updateDataset_emptyDatasetID() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given an empty dataset ID
        String datasetID = "";
        Dataset dataset = new Dataset();

        // When updateDataset is called
        // Then the expected exception is thrown
        assertThrows(IllegalArgumentException.class,
                () -> datasetAPIClient.updateDataset(datasetID, dataset));
    }

    @Test
    public void testDatasetAPI_getInstance() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a mock dataset response from the dataset API
        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_OK);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        Instance expectedInstance = mockInstanceResponse(mockHttpResponse);

        // When getInstance is called
        Instance actualInstance = datasetAPIClient.getInstance(datasetID);

        assertNotNull(actualInstance);

        HttpRequestBase httpRequest = captureHttpRequest(mockHttpClient);

        // Then the request should contain the authentication header
        String actualAuthToken = httpRequest.getFirstHeader(authTokenHeaderName).getValue();
        assertThat(actualAuthToken).isEqualTo(datasetAPIAuthToken);

        // Then the response should be whats returned from the dataset API
        assertThat(actualInstance.getId()).isEqualTo(expectedInstance.getId());
    }

    @Test
    public void testDatasetAPI_getInstance_instanceNotFound() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a request to the dataset API that returns a 404
        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_NOT_FOUND);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        // When getInstance is called
        // Then the expected exception is thrown
        assertThrows(InstanceNotFoundException.class,
                () -> datasetAPIClient.getInstance(instanceID));
    }

    @Test
    public void testDatasetAPI_getInstance_internalError() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a request to the dataset API that returns a 500
        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        // When getInstance is called
        // Then the expected exception is thrown
        assertThrows(UnexpectedResponseException.class,
                () -> datasetAPIClient.getInstance(instanceID));
    }

    @Test
    public void testDatasetAPI_getInstance_emptyInstanceID() throws URISyntaxException {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given an empty instance ID
        String instanceID = "";

        // When getInstance is called
        // Then the expected exception is thrown
        assertThrows(IllegalArgumentException.class,
                () -> datasetAPIClient.getInstance(instanceID));
    }

    @Test
    public void testDatasetAPI_getDatasetVersion() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a mock dataset response from the dataset API
        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_OK);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        DatasetVersion expectedVersion = mockVersionResponse(mockHttpResponse);

        // When getDatasetVersion is called
        DatasetVersion actualDatasetVersion = datasetAPIClient.getDatasetVersion(datasetID, edition, version);

        assertNotNull(actualDatasetVersion);

        HttpRequestBase httpRequest = captureHttpRequest(mockHttpClient);

        // Then the request should contain the authentication header
        String actualAuthToken = httpRequest.getFirstHeader(authTokenHeaderName).getValue();
        assertThat(actualAuthToken).isEqualTo(datasetAPIAuthToken);

        // Then the response should be whats returned from the dataset API
        assertThat(actualDatasetVersion.getId()).isEqualTo(expectedVersion.getId());
    }

    @Test
    public void testDatasetAPI_getDatasetVersion_datasetNotFound() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a request to the dataset API that returns a 404
        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_NOT_FOUND);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        // When getDatasetVersion is called
        // Then the expected exception is thrown
        assertThrows(DatasetNotFoundException.class,
                () -> datasetAPIClient.getDatasetVersion(datasetID, edition, version));
    }

    @Test
    public void testDatasetAPI_getDatasetVersion_internalError() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a request to the dataset API that returns a 500
        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        // When getDatasetVersion is called
        // Then the expected exception is thrown
        assertThrows(UnexpectedResponseException.class,
                () -> datasetAPIClient.getDatasetVersion(datasetID, edition, version));
    }

    @Test
    public void testDatasetAPI_getDatasetVersion_emptyDatasetID() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given an empty dataset ID
        String datasetID = "";

        // When getDatasetVersion is called
        // Then the expected exception is thrown
        assertThrows(IllegalArgumentException.class,
                () -> datasetAPIClient.getDatasetVersion(datasetID, edition, version));
    }

    @Test
    public void testDatasetAPI_getDatasetVersion_emptyEdition() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given an empty edition
        String edition = "";

        // When getDatasetVersion is called
        // Then the expected exception is thrown
        assertThrows(IllegalArgumentException.class,
                () -> datasetAPIClient.getDatasetVersion(datasetID, edition, version));
    }

    @Test
    public void testDatasetAPI_updateDatasetVersion() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given an updated dataset
        DatasetVersion datasetVersion = createDatasetVersion();

        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_OK);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        // When updateDatasetVersion is called
        datasetAPIClient.updateDatasetVersion(datasetID, edition, version, datasetVersion);

        HttpEntityEnclosingRequestBase httpRequest = captureHttpRequestWithBody(mockHttpClient);

        // Then the request should contain the authentication header
        String actualAuthToken = httpRequest.getFirstHeader(authTokenHeaderName).getValue();
        assertThat(actualAuthToken).isEqualTo(datasetAPIAuthToken);

        // Then the request should contain the provided dataset version in the body
        String body = IOUtils.toString(httpRequest.getEntity().getContent());
        DatasetVersion requestDatasetVersion = json.readValue(body, DatasetVersion.class);

        assertThat(requestDatasetVersion.getId()).isEqualTo(datasetVersion.getId());
    }

    @Test
    public void testDatasetAPI_updateDatasetVersion_datasetNotFound() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a request to the dataset API that returns a 404
        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_NOT_FOUND);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        DatasetVersion datasetVersion = createDatasetVersion();

        // When updateDatasetVersion is called
        // Then the expected exception is thrown
        assertThrows(DatasetNotFoundException.class,
                () -> datasetAPIClient.updateDatasetVersion(datasetID, edition, version, datasetVersion));
    }

    @Test
    public void testDatasetAPI_updateDatasetVersion_internalError() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a request to the dataset API that returns a 500
        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        DatasetVersion datasetVersion = createDatasetVersion();

        // When updateDatasetVersion is called
        // Then the expected exception is thrown
        assertThrows(UnexpectedResponseException.class,
                () -> datasetAPIClient.updateDatasetVersion(datasetID, edition, version, datasetVersion));
    }

    @Test
    public void testDatasetAPI_getDatasetVersion_emptyVersion() throws URISyntaxException {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given an empty version
        String version = "";

        // When getDatasetVersion is called
        // Then the expected exception is thrown
        assertThrows(IllegalArgumentException.class,
                () -> datasetAPIClient.getDatasetVersion(datasetID, edition, version));
    }

    @Test
    public void testDatasetAPI_updateDatasetVersion_emptyDatasetID() throws URISyntaxException {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given an empty dataset ID
        String datasetID = "";
        DatasetVersion datasetVersion = new DatasetVersion();

        // When updateDatasetVersion is called
        // Then the expected exception is thrown
        assertThrows(IllegalArgumentException.class,
                () -> datasetAPIClient.updateDatasetVersion(datasetID, edition, version, datasetVersion));
    }

    @Test
    public void testDatasetAPI_updateDatasetVersion_emptyEdition() throws URISyntaxException {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given an empty edition
        String edition = "";
        DatasetVersion datasetVersion = new DatasetVersion();

        // When updateDatasetVersion is called
        // Then the expected exception is thrown
        assertThrows(IllegalArgumentException.class,
                () -> datasetAPIClient.updateDatasetVersion(datasetID, edition, version, datasetVersion));
    }

    @Test
    public void testDatasetAPI_updateDatasetVersion_emptyVersion() throws URISyntaxException {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given an empty version
        String version = "";
        DatasetVersion datasetVersion = new DatasetVersion();

        // When updateDatasetVersion is called
        // Then the expected exception is thrown
        assertThrows(IllegalArgumentException.class,
                () -> datasetAPIClient.updateDatasetVersion(datasetID, edition, version, datasetVersion));
    }

    @Test
    public void testDatasetAPI_deleteDataset() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a mock dataset response from the dataset API
        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_NO_CONTENT);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        // When deleteDataset is called
        datasetAPIClient.deleteDataset(datasetID);

        HttpRequestBase httpRequest = captureHttpRequest(mockHttpClient);

        // Then the request should contain the authentication header
        String actualAuthToken = httpRequest.getFirstHeader(authTokenHeaderName).getValue();
        assertThat(actualAuthToken).isEqualTo(datasetAPIAuthToken);
    }

    @Test
    public void testDatasetAPI_deleteDataset_internalError() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given a request to the dataset API that returns a 500
        CloseableHttpResponse mockHttpResponse = mockHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockHttpResponse);

        // When deleteDataset is called
        // Then the expected exception is thrown
        assertThrows(UnexpectedResponseException.class,
                () -> datasetAPIClient.deleteDataset(datasetID));
    }

    @Test
    public void testDatasetAPI_deleteDataset_emptyDatasetID() throws Exception {

        CloseableHttpClient mockHttpClient = mock(CloseableHttpClient.class);
        DatasetAPIClient datasetAPIClient = new DatasetAPIClient(datasetAPIURL, datasetAPIAuthToken, mockHttpClient);

        // Given an empty dataset ID
        String datasetID = "";

        // When updateDataset is called
        // Then the expected exception is thrown
        assertThrows(IllegalArgumentException.class,
                () -> datasetAPIClient.deleteDataset(datasetID));
    }

    private CloseableHttpResponse mockHttpResponse(int httpStatus) {

        CloseableHttpResponse mockHttpResponse = mock(CloseableHttpResponse.class);

        StatusLine mockResponseStatus = mock(StatusLine.class);
        when(mockResponseStatus.getStatusCode()).thenReturn(httpStatus);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockResponseStatus);

        return mockHttpResponse;
    }

    private void mockResponseBody(CloseableHttpResponse mockHttpResponse, Object responseBody) throws JsonProcessingException, UnsupportedEncodingException {
        String responseJSON = json.writeValueAsString(responseBody);
        when(mockHttpResponse.getEntity()).thenReturn(new StringEntity(responseJSON));
    }

    private DatasetResponse mockDatasetResponse(CloseableHttpResponse mockHttpResponse) throws JsonProcessingException, UnsupportedEncodingException {
        DatasetResponse responseBody = new DatasetResponse();
        responseBody.setId(datasetID);
        responseBody.setNext(new Dataset());

        mockResponseBody(mockHttpResponse, responseBody);

        return responseBody;
    }

    private Instance mockInstanceResponse(CloseableHttpResponse mockHttpResponse) throws JsonProcessingException, UnsupportedEncodingException {
        Instance instance = new Instance();
        instance.setId(instanceID);

        mockResponseBody(mockHttpResponse, instance);

        return instance;
    }

    private HttpEntityEnclosingRequestBase captureHttpRequestWithBody(CloseableHttpClient mockHttpClient) throws IOException {
        ArgumentCaptor<HttpEntityEnclosingRequestBase> requestCaptor = ArgumentCaptor.forClass(HttpEntityEnclosingRequestBase.class);
        verify(mockHttpClient).execute(requestCaptor.capture());
        return requestCaptor.getValue();
    }

    private HttpRequestBase captureHttpRequest(CloseableHttpClient mockHttpClient) throws IOException {
        ArgumentCaptor<HttpRequestBase> requestCaptor = ArgumentCaptor.forClass(HttpRequestBase.class);
        verify(mockHttpClient).execute(requestCaptor.capture());
        return requestCaptor.getValue();
    }

    private Dataset createDataset() {
        Dataset dataset = new Dataset();
        dataset.setId(datasetID);
        dataset.setTitle(datasetTitle);
        return dataset;
    }

    private DatasetVersion createDatasetVersion() {
        DatasetVersion datasetVersion = new DatasetVersion();
        datasetVersion.setVersion(version);
        return datasetVersion;
    }

    private DatasetVersion mockVersionResponse(CloseableHttpResponse mockHttpResponse) throws UnsupportedEncodingException, JsonProcessingException {
        DatasetVersion datasetVersion = createDatasetVersion();
        mockResponseBody(mockHttpResponse, datasetVersion);

        return datasetVersion;
    }
}
