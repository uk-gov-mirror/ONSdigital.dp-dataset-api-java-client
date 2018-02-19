package dp.api.dataset.model;

import com.google.gson.annotations.SerializedName;

public enum State {

    @SerializedName("created")
    CREATED,

    @SerializedName("edition-confirmed")
    EDITION_CONFIRMED,

    @SerializedName("associated")
    ASSOCIATED,

    @SerializedName("published")
    PUBLISHED,
}
