package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.GeneralLetterTemplateContent;

import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER;

@Component
@Slf4j
public class GenerateGeneralLetter implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private GeneralLetterTemplateContent templateContent;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        log.info("Generating general letter for case id: {} ", caseId);

        LanguagePreference languagePreference =
            GeneralParties.RESPONDENT.equals(caseData.getGeneralLetter().getGeneralLetterParties())
            ? caseData.getApplicant2().getLanguagePreference()
            : caseData.getApplicant1().getLanguagePreference();

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            GENERAL_LETTER,
            templateContent.apply(caseData, caseId),
            caseId,
            GENERAL_LETTER_TEMPLATE_ID,
            languagePreference,
            GENERAL_LETTER_DOCUMENT_NAME
        );

        return caseDetails;
    }
}
