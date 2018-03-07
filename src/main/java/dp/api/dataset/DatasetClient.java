package dp.api.dataset;

import dp.api.dataset.exception.DatasetAPIException;
import dp.api.dataset.model.Dataset;
import dp.api.dataset.model.DatasetVersion;
import dp.api.dataset.model.Instance;

import java.io.Closeable;
import java.io.IOException;

public interface DatasetClient extends Closeable {

    /**
     * Get the instance for the given instance ID.
     */
    Instance getInstance(String instanceID)  throws IOException, DatasetAPIException;

    /**
     * Create a new dataset.
     */
    Dataset createDataset(String datasetID, Dataset dataset)  throws IOException, DatasetAPIException;

    /**
     * Get the dataset for the given dataset ID.
     */
    Dataset getDataset(String datasetID)  throws IOException, DatasetAPIException;

    /**
     * Delete the dataset for the given dataset ID.
     */
    void deleteDataset(String datasetID) throws IOException, DatasetAPIException;

    /**
     * Update the dataset for the given dataset ID with the given dataset instance data.
     */
    void updateDataset(String datasetID, Dataset dataset) throws IOException, DatasetAPIException;

    /**
     * Get a particular version of a dataset.
     */
    DatasetVersion getDatasetVersion(String datasetID, String edition, String version) throws IOException, DatasetAPIException;

    /**
     * Update the dataset version
     */
    void updateDatasetVersion(String datasetID, String edition, String version, DatasetVersion datasetVersion) throws IOException, DatasetAPIException;
}
