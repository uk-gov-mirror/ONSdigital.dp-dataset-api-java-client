package dp.api.dataset.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The link structure used by the dataset API.
 */
public class Link {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String href;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
