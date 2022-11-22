package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;

@Component
@Slf4j
public class RegenerateConditionalOrderPronouncedCoverLetter implements CaseTask {

    @Autowired
    private ConditionalOrderPronouncedCoverLetterHelper coverLetterHelper;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        if (caseData.getApplicant1().isApplicantOffline() && caseData.getApplicant1().isConfidentialContactDetails()) {

            boolean anyDocRemoved = removeExistingCoverLetterIfAny(caseData, CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1);

            if (anyDocRemoved) {
                log.info("Regenerating applicant 1 conditional order pronounced coversheet for case id {} ", caseId);
                coverLetterHelper.generateConditionalOrderPronouncedCoversheet(
                    caseData,
                    caseId,
                    caseData.getApplicant1(),
                    CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1
                );

                caseData.getApplicant1().setCoPronouncedCoverLetterRegenerated(YES);
            }
        }

        if (caseData.getApplicant2().isApplicantOffline() && caseData.getApplicant2().isConfidentialContactDetails()) {
            boolean anyDocRemoved = removeExistingCoverLetterIfAny(caseData, CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2);

            if (anyDocRemoved) {
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

                caseData.getApplicant2().setCoPronouncedCoverLetterRegenerated(YES);
            }
        }

        return caseDetails;
    }

    private boolean removeExistingCoverLetterIfAny(final CaseData caseData, final DocumentType documentType) {
        return caseData.getDocuments().getDocumentsGenerated()
            .removeIf(doc -> documentType.equals(doc.getValue().getDocumentType()));
    }
}
