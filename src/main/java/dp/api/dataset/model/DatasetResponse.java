package dp.api.dataset.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Dataset current;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Dataset next;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Dataset getCurrent() {
        return current;
    }

    public void setCurrent(Dataset current) {
        this.current = current;
    }

    public Dataset getNext() {
        return next;
    }

    public void setNext(Dataset next) {
        this.next = next;
    }
}
