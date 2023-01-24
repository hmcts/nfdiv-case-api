package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderPronouncedTemplateContent;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_JS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;

@Component
@Slf4j
public class GenerateConditionalOrderPronouncedDocument implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private ConditionalOrderPronouncedTemplateContent conditionalOrderPronouncedTemplateContent;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {
        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        log.info("Generating Conditional Order granted pdf for CaseID: {}", caseDetails.getId());

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED,
            conditionalOrderPronouncedTemplateContent.apply(caseData, caseId, caseData.getApplicant1().getLanguagePreference()),
            caseId,
            YES.equals(caseData.getIsJudicialSeparation())
                ? CONDITIONAL_ORDER_PRONOUNCED_JS_TEMPLATE_ID
                : CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID,
            caseData.getApplicant1().getLanguagePreference(),
            CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME
        );

        addConditionalOrderGrantedDocument(caseData);

        return caseDetails;
    }

    private void addConditionalOrderGrantedDocument(CaseData caseData) {
        caseData.getDocuments().getDocumentGeneratedWithType(CONDITIONAL_ORDER_GRANTED)
            .ifPresent(divorceDocumentListValue -> caseData.getConditionalOrder()
                .setConditionalOrderGrantedDocument(divorceDocumentListValue.getValue()));
    }
}
