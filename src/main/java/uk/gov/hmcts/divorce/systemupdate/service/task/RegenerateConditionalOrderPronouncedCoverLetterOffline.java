package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.systemupdate.service.task.RegenerateConditionalOrderPronouncedCoverLetter.removeAndRegenerateApplicant1;
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

        if (caseData.getApplicant1().isApplicantOffline()
            && !YesOrNo.YES.equals(caseData.getApplicant1().getCoPronouncedCoverLetterRegenerated())) {
            removeAndRegenerateApplicant1(caseId, caseData, coverLetterHelper);
        }
        if (caseData.getApplicant2().isApplicantOffline()
            && !YesOrNo.YES.equals(caseData.getApplicant1().getCoPronouncedCoverLetterRegenerated())) {
            removeAndRegenerateApplicant2(caseId, caseData, coverLetterHelper);
        }

        return caseDetails;
    }
}
