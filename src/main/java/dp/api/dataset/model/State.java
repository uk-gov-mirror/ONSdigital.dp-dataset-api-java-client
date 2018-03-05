package dp.api.dataset.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum State {

    @JsonProperty("created")
    CREATED,

    @JsonProperty("edition-confirmed")
    EDITION_CONFIRMED,

    @JsonProperty("associated")
    ASSOCIATED,

    @JsonProperty("published")
    PUBLISHED,
}
