package dp.api.dataset.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The model of a links as provided by the dataset API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetLinks {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Link self;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Link taxonomy;

    public Link getSelf() {
        return self;
    }

    public void setSelf(Link self) {
        this.self = self;
    }

    public Link getTaxonomy() {
        return taxonomy;
    }

    public void setTaxonomy(Link taxonomy) {
        this.taxonomy = taxonomy;
    }
}
