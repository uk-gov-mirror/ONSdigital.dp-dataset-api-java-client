package dp.api.dataset.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetVersion {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String pageType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String uri;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPageType() {
        return pageType;
    }

    public void setDescription(String pageType) {
        this.pageType = pageType;
    }

    public String getUri() {
        return uri;
    }

    public void setDescription(String uri) {
        this.uri = uri;
    }

}