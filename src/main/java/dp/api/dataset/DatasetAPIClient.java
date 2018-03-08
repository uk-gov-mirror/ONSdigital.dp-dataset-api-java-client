package dp.api.dataset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dp.api.dataset.exception.BadRequestException;
import dp.api.dataset.exception.DatasetAPIException;
import dp.api.dataset.exception.DatasetAlreadyExistsException;
import dp.api.dataset.exception.DatasetNotFoundException;
import dp.api.dataset.exception.InstanceNotFoundException;
import dp.api.dataset.exception.UnauthorisedException;
import dp.api.dataset.exception.UnexpectedResponseException;
import dp.api.dataset.model.Dataset;
import dp.api.dataset.model.DatasetResponse;
import dp.api.dataset.model.DatasetVersion;
import dp.api.dataset.model.Instance;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * HTTP client for the dataset API.
 */
public class DatasetAPIClient implements DatasetClient {

    private final URI datasetAPIURL;
    private final String datasetAPIAuthToken;

    private final CloseableHttpClient client;

    private static final String authTokenHeaderName = "Internal-token";
    private static final ObjectMapper json = new ObjectMapper();

    /**
     * Create a new instance of DatasetAPIClient
     *
     * @param datasetAPIURL       - The URL of the dataset API
     * @param datasetAPIAuthToken - The authentication token for the dataset API
     * @param client              - The HTTP client to use internally
     */
    public DatasetAPIClient(String datasetAPIURL, String datasetAPIAuthToken, CloseableHttpClient client) throws URISyntaxException {

        this.datasetAPIURL = new URI(datasetAPIURL);
        this.datasetAPIAuthToken = datasetAPIAuthToken;
        this.client = client;
    }

    /**
     * Create a new instance of DatasetAPIClient
     *
     * @param datasetAPIURL       - The URL of the dataset API
     * @param datasetAPIAuthToken - The authentication token for the dataset API
     */
    public DatasetAPIClient(String datasetAPIURL, String datasetAPIAuthToken) throws URISyntaxException {

        this(datasetAPIURL, datasetAPIAuthToken, createDefaultHttpClient());
    }

    private static CloseableHttpClient createDefaultHttpClient() {

        return HttpClients.custom()
                .setServiceUnavailableRetryStrategy(new RetryStrategy())
                .build();
    }

    /**
     * Get the instance for the given instance ID.
     *
     * @param instanceID
     * @return
     * @throws IOException
     * @throws DatasetAPIException
     */
    @Override
    public Instance getInstance(String instanceID) throws IOException, DatasetAPIException {

        validateInstanceID(instanceID);

        String path = "/instances/" + instanceID;
        URI uri = datasetAPIURL.resolve(path);

        HttpGet httpRequest = new HttpGet(uri);
        httpRequest.addHeader(authTokenHeaderName, datasetAPIAuthToken);

        logRequest(httpRequest);

        try (CloseableHttpResponse response = client.execute(httpRequest)) {

            logResponse(httpRequest, response);

            switch (response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                    return parseResponseBody(response, Instance.class);
                case HttpStatus.SC_NOT_FOUND:
                    throw new InstanceNotFoundException(formatErrResponse(httpRequest, response));
                default:
                    throw new UnexpectedResponseException(
                            formatErrResponse(httpRequest, response),
                            response.getStatusLine().getStatusCode());
            }
        }
    }

    /**
     * Create a new dataset
     *
     * @param datasetID
     * @param dataset
     * @return
     * @throws IOException
     * @throws DatasetAPIException
     */
    @Override
    public Dataset createDataset(String datasetID, Dataset dataset) throws IOException, DatasetAPIException {

        validateDatasetID(datasetID);

        String path = "/datasets/" + datasetID;
        URI uri = datasetAPIURL.resolve(path);

        HttpPost httpRequest = new HttpPost(uri);
        httpRequest.addHeader(authTokenHeaderName, datasetAPIAuthToken);
        httpRequest.setHeader("Content-Type", "application/json");

        addBody(dataset, httpRequest);

        logRequest(httpRequest);

        try (CloseableHttpResponse response = client.execute(httpRequest)) {

            logResponse(httpRequest, response);

            switch (response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_CREATED:
                    DatasetResponse datasetResponse = parseResponseBody(response, DatasetResponse.class);
                    return datasetResponse.getNext();
                case HttpStatus.SC_UNAUTHORIZED:
                    throw new UnauthorisedException();
                case HttpStatus.SC_FORBIDDEN:
                    throw new DatasetAlreadyExistsException();
                default:
                    throw new UnexpectedResponseException(
                            formatErrResponse(httpRequest, response),
                            response.getStatusLine().getStatusCode());
            }
        }
    }

    /**
     * Get the dataset for the given dataset ID.
     *
     * @param datasetID
     * @return
     * @throws IOException
     * @throws DatasetAPIException
     */
    @Override
    public Dataset getDataset(String datasetID) throws IOException, DatasetAPIException {

        validateDatasetID(datasetID);

        String path = "/datasets/" + datasetID;
        URI uri = datasetAPIURL.resolve(path);

        HttpGet httpRequest = new HttpGet(uri);
        httpRequest.addHeader(authTokenHeaderName, datasetAPIAuthToken);

        logRequest(httpRequest);

        try (CloseableHttpResponse response = client.execute(httpRequest)) {

            logResponse(httpRequest, response);
            validate200ResponseCode(httpRequest, response);

            DatasetResponse datasetResponse = parseResponseBody(response, DatasetResponse.class);
            return datasetResponse.getNext();
        }
    }

    /**
     * Delete the dataset for the given dataset ID.
     *
     * @param datasetID
     * @return
     * @throws IOException
     * @throws DatasetAPIException
     */
    @Override
    public void deleteDataset(String datasetID) throws IOException, DatasetAPIException {

        validateDatasetID(datasetID);

        String path = "/datasets/" + datasetID;
        URI uri = datasetAPIURL.resolve(path);

        HttpDelete httpRequest = new HttpDelete(uri);
        httpRequest.addHeader(authTokenHeaderName, datasetAPIAuthToken);

        logRequest(httpRequest);

        try (CloseableHttpResponse response = client.execute(httpRequest)) {

            logResponse(httpRequest, response);
            switch (response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_NO_CONTENT:
                    return;
                default:
                    throw new UnexpectedResponseException(
                            formatErrResponse(httpRequest, response),
                            response.getStatusLine().getStatusCode());
            }
        }
    }

    /**
     * Update the dataset for the given dataset ID with the given dataset instance data.
     *
     * @param datasetID
     * @param dataset
     * @return
     * @throws IOException
     * @throws DatasetAPIException
     */
    @Override
    public void updateDataset(String datasetID, Dataset dataset) throws IOException, DatasetAPIException {

        validateDatasetID(datasetID);

        String path = "/datasets/" + datasetID;
        URI uri = datasetAPIURL.resolve(path);

        HttpPut httpRequest = new HttpPut(uri);
        httpRequest.addHeader(authTokenHeaderName, datasetAPIAuthToken);
        httpRequest.setHeader("Content-Type", "application/json");

        addBody(dataset, httpRequest);

        logRequest(httpRequest);

        try (CloseableHttpResponse response = client.execute(httpRequest)) {

            logResponse(httpRequest, response);

            switch (response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                    return;
                case HttpStatus.SC_NOT_FOUND:
                    throw new DatasetNotFoundException(formatErrResponse(httpRequest, response));
                case HttpStatus.SC_UNAUTHORIZED:
                    throw new UnauthorisedException();
                case HttpStatus.SC_BAD_REQUEST:
                    throw new BadRequestException("invalid dataset request");
                default:
                    throw new UnexpectedResponseException(
                            formatErrResponse(httpRequest, response),
                            response.getStatusLine().getStatusCode());
            }
        }
    }

    /**
     * Get a particular version of a dataset.
     *
     * @param datasetID
     * @param edition
     * @param version
     * @return
     * @throws IOException
     * @throws DatasetAPIException
     */
    @Override
    public DatasetVersion getDatasetVersion(String datasetID, String edition, String version) throws IOException, DatasetAPIException {

        validateDatasetID(datasetID);
        validateEdition(edition);
        validateVersion(version);

        String path = String.format("/datasets/%s/editions/%s/versions/%s", datasetID, edition, version);
        URI uri = datasetAPIURL.resolve(path);

        HttpGet httpRequest = new HttpGet(uri);
        httpRequest.addHeader(authTokenHeaderName, datasetAPIAuthToken);

        logRequest(httpRequest);

        try (CloseableHttpResponse response = client.execute(httpRequest)) {

            logResponse(httpRequest, response);
            validate200ResponseCode(httpRequest, response);
            return parseResponseBody(response, DatasetVersion.class);
        }
    }

    /**
     * Update the dataset version
     *
     * @param datasetID
     * @param edition
     * @param version
     * @param datasetVersion
     * @return
     * @throws IOException
     * @throws DatasetAPIException
     */
    @Override
    public void updateDatasetVersion(String datasetID, String edition, String version, DatasetVersion datasetVersion) throws IOException, DatasetAPIException {

        validateDatasetID(datasetID);
        validateEdition(edition);
        validateVersion(version);

        String path = String.format("/datasets/%s/editions/%s/versions/%s", datasetID, edition, version);
        URI uri = datasetAPIURL.resolve(path);

        HttpPut httpRequest = new HttpPut(uri);
        httpRequest.addHeader(authTokenHeaderName, datasetAPIAuthToken);
        httpRequest.setHeader("Content-Type", "application/json");

        addBody(datasetVersion, httpRequest);

        logRequest(httpRequest);

        try (CloseableHttpResponse response = client.execute(httpRequest)) {

            logResponse(httpRequest, response);
            validate200ResponseCode(httpRequest, response);
        }
    }

    private void validate200ResponseCode(HttpRequestBase httpRequest, CloseableHttpResponse response) throws DatasetNotFoundException, UnexpectedResponseException, UnauthorisedException {

        switch (response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_OK:
                return;
            case HttpStatus.SC_NOT_FOUND:
                throw new DatasetNotFoundException(formatErrResponse(httpRequest, response));
            case HttpStatus.SC_UNAUTHORIZED:
                throw new UnauthorisedException();
            default:
                throw new UnexpectedResponseException(
                        formatErrResponse(httpRequest, response),
                        response.getStatusLine().getStatusCode());
        }
    }

    private void addBody(Object object, HttpEntityEnclosingRequestBase httpRequest) throws JsonProcessingException, UnsupportedEncodingException {

        String body = json.writeValueAsString(object);
        StringEntity stringEntity = new StringEntity(body);
        httpRequest.setEntity(stringEntity);
    }

    private void validateDatasetID(String datasetID) {
        Args.check(isNotEmpty(datasetID), "a dataset id must be provided.");
    }

    private void validateEdition(String edition) {
        Args.check(isNotEmpty(edition), "an edition must be provided.");
    }

    private void validateVersion(String version) {
        Args.check(isNotEmpty(version), "a version must be provided.");
    }

    private void validateInstanceID(String instanceID) {
        Args.check(isNotEmpty(instanceID), "a instance id must be provided.");
    }

    private static boolean isNotEmpty(String str) {
        return str != null && str.length() > 0;
    }

    private void logRequest(HttpRequestBase httpRequest) {

        new LogBuilder("Calling dataset API")
                .addParameter("method", httpRequest.getMethod())
                .addParameter("uri", httpRequest.getURI())
                .log();

    }

    private void logResponse(HttpRequestBase httpRequest, CloseableHttpResponse response) {

        new LogBuilder("dataset api response")
                .addParameter("uri", httpRequest.getURI())
                .addParameter("method", httpRequest.getMethod())
                .addParameter("status", response.getStatusLine())
                .log();
    }

    private <T> T parseResponseBody(CloseableHttpResponse response, Class<T> type) throws IOException {
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity);
        return json.readValue(responseString, type);
    }

    private String formatErrResponse(HttpRequestBase httpRequest, CloseableHttpResponse response) {

        return String.format("the dataset api returned a %s response for %s",
                response.getStatusLine().getStatusCode(),
                httpRequest.getURI());
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
