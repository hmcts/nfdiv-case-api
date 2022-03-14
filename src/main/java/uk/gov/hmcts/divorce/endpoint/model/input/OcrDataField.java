package uk.gov.hmcts.divorce.endpoint.model.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class OcrDataField {

    @JsonProperty
    private final String name;

    @JsonProperty
    private final String value;

    @JsonCreator
    public OcrDataField(
        @JsonProperty("name") String name,
        @JsonProperty("value") String value
    ) {
        this.name = name;
        this.value = value;
    }
}
