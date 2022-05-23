package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum CivilPartnershipBroken implements HasLabel {

    @JsonProperty("civilPartnershipBroken")
    CIVIL_PARTNERSHIP_BROKEN("The applicant’s civil partnership has broken down irretrievably");

    private final String label;
}
