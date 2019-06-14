package dp.api.dataset.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetMetadata {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String nationalStatistic;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String pageType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String releaseDate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String title;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String uri;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DatasetContacts contact;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNationalStatistic() {
        return nationalStatistic;
    }

    public void setNationalStatistic(String nationalStatistic) {
        this.nationalStatistic = nationalStatistic;
    }

    public String getPageType() {
        return pageType;
    }

    public void setPageType(String pageType) { this.pageType = pageType; }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public DatasetContacts getContact() {
        return contact;
    }

    public void setContact(DatasetContacts contact) {
        this.contact = contact;
    }

}