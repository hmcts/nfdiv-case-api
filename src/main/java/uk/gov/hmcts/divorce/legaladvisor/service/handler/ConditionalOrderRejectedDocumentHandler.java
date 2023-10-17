package uk.gov.hmcts.divorce.legaladvisor.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.RefusalOption;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForAmendmentContent;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedTemplateContent;
import uk.gov.hmcts.divorce.legaladvisor.notification.LegalAdvisorRejectedDecisionNotification;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.REJECT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAmendedApplication;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REJECTED_REFUSAL_ORDER_TEMPLATE_ID;

@Slf4j
@Service
public class ConditionalOrderRejectedDocumentHandler extends ConditionalOrderDocumentHandler {

    private final LegalAdvisorRejectedDecisionNotification rejectedNotification;
    private final ConditionalOrderRefusedForAmendmentContent conditionalOrderRefusedForAmendmentContent;
    public ConditionalOrderRejectedDocumentHandler(CaseDataDocumentService caseDataDocumentService,
                                                   NotificationDispatcher notificationDispatcher,
                                                   LegalAdvisorRejectedDecisionNotification rejectedNotification,
                                                   ConditionalOrderRefusedForAmendmentContent conditionalOrderRefusedForAmendmentContent) {
        super(caseDataDocumentService, notificationDispatcher);
        this.rejectedNotification = rejectedNotification;
        this.conditionalOrderRefusedForAmendmentContent = conditionalOrderRefusedForAmendmentContent;
    }

    @Override
    public RefusalOption getRefusalOption() {
        return REJECT;
    }

    @Override
    public State getEndState() {
        return AwaitingAmendedApplication;
    }

    @Override
    public ApplicantNotification getApplicantNotification() {
        return rejectedNotification;
    }

    @Override
    public ConditionalOrderRefusedTemplateContent getConditionalOrderRefusedTemplateContent() {
        return conditionalOrderRefusedForAmendmentContent;
    }

    @Override
    public String getRefusalDocumentTemplateId() {
        return REJECTED_REFUSAL_ORDER_TEMPLATE_ID;
    }

    @Override
    public State handle(CaseData caseData, Long caseId) {
        generateAndSetConditionalOrderRefusedDocument(
            caseData,
            caseId);
        notificationDispatcher.send(getApplicantNotification(), caseData, caseId);
        return getEndState();
    }
}
