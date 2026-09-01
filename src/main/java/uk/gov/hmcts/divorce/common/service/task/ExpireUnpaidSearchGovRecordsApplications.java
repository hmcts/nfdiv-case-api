package uk.gov.hmcts.divorce.common.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.CitizenGeneralApplicationSubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExpireUnpaidSearchGovRecordsApplications implements CaseTask {

    private final CitizenGeneralApplicationSubmissionService submissionService;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {

        CaseData caseData = caseDetails.getData();
        Applicant applicant = caseData.getApplicant1();

        Optional<GeneralApplication> generalApplicationOptional = submissionService.findActiveGeneralApplication(caseData, applicant);
        generalApplicationOptional.ifPresent(generalApplication -> {
            if (GeneralApplicationType.DISCLOSURE_VIA_DWP.equals(generalApplication.getGeneralApplicationType())) {
                applicant.setActiveGeneralApplication(null);
            }
        });

        return caseDetails;
    }
}
