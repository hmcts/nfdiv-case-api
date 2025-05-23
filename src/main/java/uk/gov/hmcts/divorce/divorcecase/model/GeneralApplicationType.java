package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum GeneralApplicationType implements HasLabel {

    @JsonProperty("dispensedWithService")
    DISPENSED_WITH_SERVICE("Dispensed with service", AlternativeServiceType.DISPENSED),

    @JsonProperty("deemedService")
    DEEMED_SERVICE("Deemed service", AlternativeServiceType.DEEMED),

    @JsonProperty("otherAlternativeServiceMethod")
    OTHER_ALTERNATIVE_SERVICE_METHODS("Alternative service", AlternativeServiceType.ALTERNATIVE_SERVICE),

    @JsonProperty("expedite")
    EXPEDITE("Expedite", null),

    @JsonProperty("issueDivorceWithoutMarriageCertificate")
    ISSUE_DIVORCE_WITHOUT_CERT("Issue divorce without marriage certificate", null),

    @JsonProperty("orderOnFilingOfAnswers")
    ORDER_ON_FILLING_OF_ANSWERS("Order on filing of Answers", null),

    @JsonProperty("permissionOnDaOot")
    PERMISSION_ON_DA_OOT("Permission on FO OOT", null),

    @JsonProperty("disclosureViaDwp")
    DISCLOSURE_VIA_DWP("Disclosure via DWP", null),

    @JsonProperty("amendApplication")
    AMEND_APPLICATION("Amend Application", null),

    @JsonProperty("other")
    OTHER("Other", null);

    private final String label;
    private final AlternativeServiceType serviceType;
}
