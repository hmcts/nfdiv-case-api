package uk.gov.hmcts.divorce.solicitor.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

@Component
@Slf4j
public class SetApplicantAddresses implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        var applicant1 = caseDetails.getData().getApplicant1();
        var applicant2 = caseDetails.getData().getApplicant2();

        applicant1.setAddress(applicant1.getNonConfidentialAddress());
        applicant2.setAddress(applicant2.getNonConfidentialAddress());

        return caseDetails;
    }
}
