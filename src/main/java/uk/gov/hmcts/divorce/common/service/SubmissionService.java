package uk.gov.hmcts.divorce.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.task.SendCitizenSubmissionNotifications;
import uk.gov.hmcts.divorce.common.service.task.SetApplicant2Email;
import uk.gov.hmcts.divorce.common.service.task.SetDateSubmitted;
import uk.gov.hmcts.divorce.common.service.task.SetStateAfterSubmission;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner;

@Service
public class SubmissionService {

    @Autowired
    private SetStateAfterSubmission setStateAfterSubmission;

    @Autowired
    private SetDateSubmitted setDateSubmitted;

    @Autowired
    private SetApplicant2Email setApplicant2Email;

    @Autowired
    private SendCitizenSubmissionNotifications sendCitizenSubmissionNotifications;

    public CaseDetails<CaseData, State> submitApplication(final CaseDetails<CaseData, State> caseDetails) {

        return CaseTaskRunner.caseTasks(
            setStateAfterSubmission,
            setDateSubmitted,
            setApplicant2Email,
            sendCitizenSubmissionNotifications
        ).run(caseDetails);
    }
}
