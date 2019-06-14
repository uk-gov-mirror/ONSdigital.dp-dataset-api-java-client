package dp.api.dataset.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * The contact structure used by the dataset API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetContacts {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Contact self;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Contact email;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Contact name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Contact telephone;

    public Contact getSelf() {
        return self;
    }

    public void setSelf(Contact self) {
        this.self = self;
    }

    public Contact getEmail() {
        return email;
    }

    public void setEmail(Contact email) {
        this.email = email;
    }

    public Contact getName() {
        return name;
    }

    public void setName(Contact name) {
        this.name = name;
    }

    public Contact getTelephone() {
        return telephone;
    }

    public void setTelephone(Contact telephone) {
        this.telephone = telephone;
    }
}