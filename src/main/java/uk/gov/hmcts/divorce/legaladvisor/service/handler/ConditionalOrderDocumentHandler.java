package uk.gov.hmcts.divorce.legaladvisor.service.handler;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.RefusalOption;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REFUSAL_ORDER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;

@Slf4j
public abstract class ConditionalOrderDocumentHandler {

    protected final CaseDataDocumentService caseDataDocumentService;
    protected final NotificationDispatcher notificationDispatcher;


    public ConditionalOrderDocumentHandler(CaseDataDocumentService caseDataDocumentService,
                                           NotificationDispatcher notificationDispatcher) {
        this.caseDataDocumentService = caseDataDocumentService;
        this.notificationDispatcher = notificationDispatcher;
    }

    public boolean canHandle(ConditionalOrder conditionalOrder) {
        return getRefusalOption().equals(conditionalOrder.getRefusalDecision());
    }

    public abstract State handle(CaseData caseData, Long caseId);

    public abstract RefusalOption getRefusalOption();

    public abstract State getEndState();

    public abstract ApplicantNotification getApplicantNotification();

    public abstract ConditionalOrderRefusedTemplateContent getConditionalOrderRefusedTemplateContent();

    public abstract String getRefusalDocumentTemplateId();

    protected void generateAndSetConditionalOrderRefusedDocument(final CaseData caseData, final Long caseId) {

        Document refusalOrderDocument = caseData.getConditionalOrder().getRefusalOrderDocument();

        if (refusalOrderDocument == null) {
            refusalOrderDocument = generateRefusalDocument(caseData, caseId);
            caseData.getConditionalOrder().setRefusalOrderDocument(refusalOrderDocument);
        }

        //no address on CO_REFUSAL so no need for confidential check
        caseData.getDocuments().setDocumentsGenerated(addDocumentToTop(
            caseData.getDocuments().getDocumentsGenerated(),
            DivorceDocument
                .builder()
                .documentLink(refusalOrderDocument)
                .documentFileName(refusalOrderDocument.getFilename())
                .documentType(CONDITIONAL_ORDER_REFUSAL)
                .build()
        ));
    }

    private Document generateRefusalDocument(final CaseData caseData, final Long caseId) {

        String templateId = getRefusalDocumentTemplateId();
        Map<String, Object> templateContents = getConditionalOrderRefusedTemplateContent().apply(caseData, caseId);

        log.info("Generating conditional order refusal document for templateId : {} caseId: {}", templateId, caseId);

        return caseDataDocumentService.renderDocument(
            templateContents,
            caseId,
            templateId,
            caseData.getApplicant1().getLanguagePreference(),
            REFUSAL_ORDER_DOCUMENT_NAME
        );
    }
}
