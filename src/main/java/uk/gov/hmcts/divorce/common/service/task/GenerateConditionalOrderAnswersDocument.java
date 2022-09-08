package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderAnswersTemplateContent;

import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_ANSWERS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_ANSWERS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_ANSWERS;

@Component
@Slf4j
public class GenerateConditionalOrderAnswersDocument {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private ConditionalOrderAnswersTemplateContent conditionalOrderAnswersTemplateContent;

    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails,
                                              LanguagePreference languagePreference) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        log.info("Generating Conditional Order answers pdf for CaseID: {}", caseDetails.getId());

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_ANSWERS,
            conditionalOrderAnswersTemplateContent.apply(caseData, caseId),
            caseId,
            CONDITIONAL_ORDER_ANSWERS_TEMPLATE_ID,
            languagePreference,
            CONDITIONAL_ORDER_ANSWERS_DOCUMENT_NAME
        );

        return caseDetails;
    }
}
