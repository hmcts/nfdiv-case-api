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

@Component
@Slf4j
public class GenerateConditionalOrderPronouncedCoversheet implements CaseTask {

    @Autowired
    private ConditionalOrderPronouncedCoverLetterHelper coverLetterHelper;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        if (caseData.getApplicant1().isOffline()) {
            log.info("Generating applicant 1 conditional order pronounced coversheet for case id {} ", caseId);
            coverLetterHelper.generateConditionalOrderPronouncedCoversheet(
                caseData,
                caseId,
                caseData.getApplicant1(),
                CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1
            );
        }

        if (caseData.getApplicant2().isOffline()) {

            if (caseData.getApplicationType().isSole()) {
                log.info("Generating respondent conditional order pronounced coversheet for case id {} ", caseId);
                coverLetterHelper.generateConditionalOrderPronouncedCoversheetOfflineRespondent(
                    caseData,
                    caseId,
                    caseData.getApplicant2(),
                    caseData.getApplicant1()
                );
            } else {
                log.info("Generating applicant 2 conditional order pronounced coversheet for case id {} ", caseId);
                coverLetterHelper.generateConditionalOrderPronouncedCoversheet(
                    caseData,
                    caseId,
                    caseData.getApplicant2(),
                    CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2
                );
            }
        }

        return caseDetails;
    }
}
