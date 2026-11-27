package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum GenAppTypeApplicantSolePreIssue implements HasLabel {
    WITHDRAW_POST_ISSUE("Withdraw the divorce application"),
    EXTEND("More time to serve"),
    ISSUE_DIVORCE_WITHOUT_CERT("Issue divorce without marriage certificate"),
    EXPEDITE("Expedite"),
    AMEND_APPLICATION("Amend application"),
    OTHER("Other");

    private final String label;
}
