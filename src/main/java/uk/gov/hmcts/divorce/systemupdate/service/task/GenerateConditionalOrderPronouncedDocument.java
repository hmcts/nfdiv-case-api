package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderPronouncedTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.Optional;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;

@Component
@Slf4j
public class GenerateConditionalOrderPronouncedDocument implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private ConditionalOrderPronouncedTemplateContent conditionalOrderPronouncedTemplateContent;

    @Autowired
    private DocumentRemovalService documentRemovalService;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {
        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        log.info("Generating Conditional Order granted pdf for CaseID: {}", caseDetails.getId());

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED,
            conditionalOrderPronouncedTemplateContent.apply(caseData, caseId),
            caseId,
            CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID,
            caseData.getApplicant1().getLanguagePreference(),
            CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME
        );

        addConditionalOrderGrantedDocument(caseData);

        return caseDetails;
    }

    public Optional<ListValue<DivorceDocument>> getConditionalOrderGrantedDoc(final CaseData caseData) {
        return !isEmpty(caseData.getDocuments().getDocumentsGenerated())
            ? caseData.getDocuments().getDocumentsGenerated().stream()
            .filter(document -> CONDITIONAL_ORDER_GRANTED.equals(document.getValue().getDocumentType())).findFirst()
            : Optional.empty();
    }

    public void removeExistingAndGenerateNewConditionalOrderGrantedDoc(CaseDetails<CaseData, State> caseDetails) {
        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        //remove existing doc from doc store
        documentRemovalService.deleteDocumentFromDocumentStore(
            caseData.getDocuments().getDocumentsGenerated(), CONDITIONAL_ORDER_GRANTED, caseId);

        //remove existing doc from case data
        if (!isEmpty(caseData.getDocuments().getDocumentsGenerated())) {
            caseData.getDocuments().getDocumentsGenerated()
                .removeIf(document -> CONDITIONAL_ORDER_GRANTED.equals(document.getValue().getDocumentType()));
        }

        //generate new doc
        apply(caseDetails);
    }

    private void addConditionalOrderGrantedDocument(CaseData caseData) {
        getConditionalOrderGrantedDoc(caseData)
            .ifPresent(divorceDocumentListValue -> caseData.getConditionalOrder()
            .setConditionalOrderGrantedDocument(divorceDocumentListValue.getValue()));
    }
}
