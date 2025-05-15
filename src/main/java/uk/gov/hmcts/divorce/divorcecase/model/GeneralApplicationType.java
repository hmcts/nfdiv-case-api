package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum GeneralApplicationType implements HasLabel {

    @JsonProperty("dispensedWithService")
    DISPENSED_WITH_SERVICE("Dispensed with service"),

    @JsonProperty("deemedService")
    DEEMED_SERVICE("Deemed service"),

    @JsonProperty("otherAlternativeServiceMethod")
    OTHER_ALTERNATIVE_SERVICE_METHODS("Alternative service"),

    @JsonProperty("expedite")
    EXPEDITE("Expedite"),

    @JsonProperty("issueDivorceWithoutMarriageCertificate")
    ISSUE_DIVORCE_WITHOUT_CERT("Issue divorce without marriage certificate"),

    @JsonProperty("orderOnFilingOfAnswers")
    ORDER_ON_FILLING_OF_ANSWERS("Order on filing of Answers"),

    @JsonProperty("permissionOnDaOot")
    PERMISSION_ON_DA_OOT("Permission on FO OOT"),

    @JsonProperty("disclosureViaDwp")
    DISCLOSURE_VIA_DWP("Disclosure via DWP"),

    @JsonProperty("amendApplication")
    AMEND_APPLICATION("Amend Application"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
