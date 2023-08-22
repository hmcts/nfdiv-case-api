package uk.gov.hmcts.divorce.bulkscan.endpoint.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FormType {
    @JsonProperty("d8")
    D8("D8"),

    @JsonProperty("d8s")
    D8S("D8S");

    private final String name;
}
