package dp.api.dataset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.onsdigital.logging.builder.LogMessageBuilder;
import dp.api.dataset.exception.BadRequestException;
import dp.api.dataset.exception.DatasetAPIException;
import dp.api.dataset.exception.DatasetNotFoundException;
import dp.api.dataset.exception.UnexpectedResponseException;
import dp.api.dataset.model.Dataset;
import dp.api.dataset.model.DatasetResponse;
import dp.api.dataset.model.DatasetVersion;
import dp.api.dataset.model.Instance;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * HTTP DatasetAPIClient for the dataset API.
 */
public class DatasetAPIClient implements DatasetClient {

    private final String datasetAPIURL;
    private final String datasetAPIAuthToken;

    private final static String authTokenHeaderName = "internal-token";

    private static CloseableHttpClient client = HttpClients.createDefault();
    private static ObjectMapper json = new ObjectMapper();

    /**
     * Create a new instance of DatasetAPIClient
     *
     * @param datasetAPIURL       - The URL of the dataset API
     * @param datasetAPIAuthToken - The authentication token for the dataset API
     */
    public DatasetAPIClient(String datasetAPIURL, String datasetAPIAuthToken) {
        this.datasetAPIURL = datasetAPIURL;
        this.datasetAPIAuthToken = datasetAPIAuthToken;
    }

    /**
     * Get the dataset for the given dataset ID.
     *
     * @param datasetID
     * @return
     */
    @Override
    public Dataset getDataset(String datasetID) throws IOException, DatasetAPIException {

        if (isEmpty(datasetID)) {
            throw new BadRequestException("a dataset id must be provided.");
        }

        String path = "/datasets/" + datasetID;

        URI uri;
        try {
            uri = new URIBuilder(datasetAPIURL)
                    .setPath(path)
                    .build();
        } catch (URISyntaxException e) {
            throw new BadRequestException(e.getMessage());
        }

        new LogBuilder("Calling dataset API")
                .addParameter("method", "GET")
                .addParameter("uri", uri)
                .log();

        HttpGet httpGet = new HttpGet(uri);
        httpGet.addHeader(authTokenHeaderName, datasetAPIAuthToken);

        DatasetResponse datasetResponse = doHttpRequest(httpGet, "", DatasetResponse.class);
        return datasetResponse.getNext();
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

        if (isEmpty(datasetID) || isEmpty(edition)) {
            throw new BadRequestException("a dataset id, edition, and version must be provided.");
        }

        String path = String.format("/datasets/%s/editions/%s/versions/%s", datasetID, edition, version);

        URI uri;
        try {
            uri = new URIBuilder(datasetAPIURL)
                    .setPath(path)
                    .build();
        } catch (URISyntaxException e) {
            throw new BadRequestException(e.getMessage());
        }

        new LogBuilder("calling dataset API")
                .addParameter("method", "GET")
                .addParameter("uri", uri)
                .log();

        HttpGet httpGet = new HttpGet(uri);
        httpGet.addHeader(authTokenHeaderName, datasetAPIAuthToken);

        return doHttpRequest(httpGet, "", DatasetVersion.class);
    }

    /**
     * Get the instance for the given instance ID.
     *
     * @param instanceID
     * @return
     */
    @Override
    public Instance getInstance(String instanceID) throws IOException, DatasetAPIException {

        if (isEmpty(instanceID)) {
            throw new BadRequestException("An instance ID must be provided.");
        }

        String path = "/instances/" + instanceID;

        URI uri;
        try {
            uri = new URIBuilder(datasetAPIURL)
                    .setPath(path)
                    .build();
        } catch (URISyntaxException e) {
            throw new BadRequestException(e.getMessage());
        }

        new LogBuilder("Calling dataset API")
                .addParameter("method", "GET")
                .addParameter("uri", uri)
                .log();

        HttpGet httpGet = new HttpGet(uri);
        httpGet.addHeader(authTokenHeaderName, datasetAPIAuthToken);

        return doHttpRequest(httpGet, "", Instance.class);
    }

    /**
     * Update the dataset for the given dataset ID with the given dataset instance data.
     *
     * @param datasetID
     * @param dataset
     */
    @Override
    public Dataset updateDataset(String datasetID, Dataset dataset) throws IOException, DatasetAPIException {

        if (isEmpty(datasetID)) {
            throw new BadRequestException("A dataset ID must be provided.");
        }

        String path = "/datasets/" + datasetID;

        URI uri;
        try {
            uri = new URIBuilder(datasetAPIURL)
                    .setPath(path)
                    .build();
        } catch (URISyntaxException e) {
            throw new BadRequestException(e.getMessage());
        }

        String body = json.writeValueAsString(dataset);

        new LogBuilder("Calling dataset API")
                .addParameter("method", "PUT")
                .addParameter("uri", uri)
                .addParameter("body", body)
                .log();

        HttpPut httpPut = new HttpPut(uri);
        httpPut.addHeader(authTokenHeaderName, datasetAPIAuthToken);
        httpPut.setHeader("Content-Type", "application/json");

        StringEntity stringEntity = new StringEntity(body);
        httpPut.setEntity(stringEntity);

        return doHttpRequest(httpPut, body, Dataset.class);
    }

    /**
     * Update the dataset version
     */
    @Override
    public Dataset updateDatasetVersion(String datasetID, String edition, String version, DatasetVersion datasetVersion) throws IOException, DatasetAPIException {

        if (isEmpty(datasetID)) {
            throw new BadRequestException("A dataset ID must be provided.");
        }

        String path = String.format("/datasets/%s/editions/%s/versions/%s", datasetID, edition, version);

        URI uri;
        try {
            uri = new URIBuilder(datasetAPIURL)
                    .setPath(path)
                    .build();
        } catch (URISyntaxException e) {
            throw new BadRequestException(e.getMessage());
        }

        String body = json.writeValueAsString(datasetVersion);

        new LogBuilder("Calling dataset API")
                .addParameter("method", "PUT")
                .addParameter("uri", uri)
                .addParameter("body", body)
                .log();

        HttpPut httpPut = new HttpPut(uri);
        httpPut.addHeader(authTokenHeaderName, datasetAPIAuthToken);
        httpPut.setHeader("Content-Type", "application/json");

        StringEntity stringEntity = new StringEntity(body);
        httpPut.setEntity(stringEntity);

        return doHttpRequest(httpPut, body, Dataset.class);
    }

    private <T> T doHttpRequest(HttpRequestBase httpRequest, String body, Class<T> responseType) throws IOException, DatasetNotFoundException, UnexpectedResponseException {

        try (CloseableHttpResponse response = client.execute(httpRequest)) {

            LogMessageBuilder logBuilder = new LogBuilder("Dataset API response")
                    .addParameter("uri", httpRequest.getURI())
                    .addParameter("method", httpRequest.getMethod())
                    .addParameter("body", body)
                    .addParameter("status", response.getStatusLine());

            switch (response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                    String responseString = EntityUtils.toString(response.getEntity());
                    logBuilder.addParameter("response_body", responseString).log();
                    return json.readValue(responseString, responseType);

                case HttpStatus.SC_NOT_FOUND:
                    logBuilder.log();
                    throw new DatasetNotFoundException("The dataset API returned 404 for " + httpRequest.getURI());

                default:
                    logBuilder.log();
                    throw new UnexpectedResponseException(
                            String.format("The dataset API returned a %s response for %s",
                                    response.getStatusLine().getStatusCode(),
                                    httpRequest.getURI()));
            }
        }
    }

    private static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}
