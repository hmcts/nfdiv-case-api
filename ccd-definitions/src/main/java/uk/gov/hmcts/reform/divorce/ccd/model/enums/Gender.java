package uk.gov.hmcts.reform.divorce.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum Gender implements HasLabel {

    MALE("male"),
    FEMALE("female"),
    NOT_GIVEN("notGiven");

    @JsonValue
    private final String label;
}
