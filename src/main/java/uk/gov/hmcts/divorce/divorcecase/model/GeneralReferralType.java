package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum GeneralReferralType implements HasLabel {
    @JsonProperty("alternativeServiceApplication")
    CASEWORKER_REFERRAL("Alternative service application"),

    @JsonProperty("orderApplicationWithoutMc")
    ORDER_APPLICATION_WITHOUT_MC("Order application without m/c"),

    @JsonProperty("orderOnFilingOfAnswers")
    ORDER_ON_FILLING_OF_ANSWERS("Order on filing of Answers"),

    @JsonProperty("permissionOnDaOot")
    PERMISSION_ON_DA_OOT("Permission on DA OOT"),

    @JsonProperty("disclosureViaDwp")
    DISCLOSURE_VIA_DWP("Disclosure via DWP"),

    @JsonProperty("amendApplication")
    AMEND_APPLICATION("Amend Application"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
