package uk.gov.hmcts.divorce.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.task.SendSubmissionNotifications;
import uk.gov.hmcts.divorce.common.service.task.SetApplicant2Email;
import uk.gov.hmcts.divorce.common.service.task.SetApplicantOfflineStatus;
import uk.gov.hmcts.divorce.common.service.task.SetDateSubmitted;
import uk.gov.hmcts.divorce.common.service.task.SetDefaultOrganisationPolicies;
import uk.gov.hmcts.divorce.common.service.task.SetStateAfterSubmission;
import uk.gov.hmcts.divorce.common.service.task.SetupCaseFlags;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SetStateAfterSubmission setStateAfterSubmission;

    private final SetDateSubmitted setDateSubmitted;

    private final SetApplicantOfflineStatus setApplicantOfflineStatus;

    private final SetApplicant2Email setApplicant2Email;

    private final SendSubmissionNotifications sendSubmissionNotifications;

    private final SetDefaultOrganisationPolicies setDefaultOrganisationPolicies;

    private final SetupCaseFlags setupCaseFlags;

    public CaseDetails<CaseData, State> submitApplication(final CaseDetails<CaseData, State> caseDetails) {

        return CaseTaskRunner.caseTasks(
            setStateAfterSubmission,
            setDateSubmitted,
            setApplicant2Email,
            setApplicantOfflineStatus,
            setDefaultOrganisationPolicies,
            sendSubmissionNotifications,
            setupCaseFlags
        ).run(caseDetails);
    }
}
