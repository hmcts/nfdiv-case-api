package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.systemupdate.service.task.RegenerateConditionalOrderPronouncedCoverLetter.removeAndRegenerated;


@Component
@Slf4j
public class RegenerateConditionalOrderPronouncedCoverLetterOffline implements CaseTask {


    @Autowired
    private ConditionalOrderPronouncedCoverLetterHelper coverLetterHelper;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        if (caseData.getApplicant1().isApplicantOffline()) {
            removeAndRegenerated(caseId, caseData, CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, coverLetterHelper);
        }
        if (caseData.getApplicant2().isApplicantOffline()) {
            removeAndRegenerated(caseId, caseData, CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, coverLetterHelper);
        }

        return caseDetails;
    }
}
