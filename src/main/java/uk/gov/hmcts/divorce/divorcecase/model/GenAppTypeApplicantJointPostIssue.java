package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum GenAppTypeApplicantJointPostIssue implements HasLabel {
    WITHDRAW_POST_ISSUE("Withdraw the divorce application"),
    DELAY("Delay or pause the divorce application"),
    EXPEDITE("Expedite"),
    AMEND_APPLICATION("Amend application"),
    OTHER("Other");

    private final String label;
}
