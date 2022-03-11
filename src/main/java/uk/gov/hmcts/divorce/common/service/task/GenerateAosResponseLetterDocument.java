package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.AosResponseLetterTemplateContent;
import uk.gov.hmcts.divorce.document.content.AosUndefendedResponseLetterTemplateContent;

import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_RESPONSE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_UNDEFENDED_RESPONSE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_RESPONDED_UNDEFENDED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_RESPONSE_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_UNDEFENDED_RESPONSE_LETTER;

@Component
@Slf4j
public class GenerateAosResponseLetterDocument implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private AosResponseLetterTemplateContent aosResponseLetterTemplateContent;

    @Autowired
    private AosUndefendedResponseLetterTemplateContent aosUndefendedResponseLetterTemplateContent;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();
        final AcknowledgementOfService acknowledgementOfService = caseData.getAcknowledgementOfService();

        if (caseData.getApplicant1().isOffline()) {

            if (acknowledgementOfService.isDisputed()) {
                log.info("Generating aos response (disputed) letter pdf for case id: {}", caseDetails.getId());
                caseDataDocumentService.renderDocumentAndUpdateCaseData(
                    caseData,
                    AOS_RESPONSE_LETTER,
                    aosResponseLetterTemplateContent.apply(caseData, caseId),
                    caseId,
                    RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID,
                    caseData.getApplicant1().getLanguagePreference(),
                    AOS_RESPONSE_LETTER_DOCUMENT_NAME
                );
            } else {
                log.info("Generating aos response (undefended) letter pdf for case id: {}", caseId);
                caseDataDocumentService.renderDocumentAndUpdateCaseData(
                    caseData,
                    AOS_UNDEFENDED_RESPONSE_LETTER,
                    aosUndefendedResponseLetterTemplateContent.apply(caseData, caseId),
                    caseId,
                    RESPONDENT_RESPONDED_UNDEFENDED_TEMPLATE_ID,
                    caseData.getApplicant1().getLanguagePreference(),
                    AOS_UNDEFENDED_RESPONSE_LETTER_DOCUMENT_NAME
                );
            }
        }
        return caseDetails;
    }
}
