package uk.gov.hmcts.divorce.caseworker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeOfChangeRequest {
    private Applicant applicant;
    private Applicant applicantBefore;
    private CaseDetails<CaseData, State> details;
    private CaseDetails<CaseData, State> detailsBefore;
    private List<String> roles;
    private String solicitorRole;
}
