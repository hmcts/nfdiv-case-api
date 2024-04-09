package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.systemupdate.service.task.RegenerateConditionalOrderPronouncedCoverLetter.removeAndRegenerateApplicant2;

@Component
@Slf4j
public class RegenerateConditionalOrderPronouncedCoverLetterOffline implements CaseTask {


    @Autowired
    private ConditionalOrderPronouncedCoverLetterHelper coverLetterHelper;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        //need a run with only resends to applicant2
        if (caseData.getApplicant2().isApplicantOffline()) {
            removeAndRegenerateApplicant2(caseId, caseData, coverLetterHelper);
        }

        return caseDetails;
    }
}
