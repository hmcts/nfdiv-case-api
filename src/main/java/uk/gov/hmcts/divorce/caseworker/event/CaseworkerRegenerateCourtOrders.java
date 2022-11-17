package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateFinalOrder;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateFinalOrderCoverLetter;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.RegenerateCourtOrdersNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlement;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedCoversheet;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedDocument;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;

@Component
@Slf4j
public class CaseworkerRegenerateCourtOrders implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_REGENERATE_COURT_ORDERS = "caseworker-regenerate-court-orders";

    @Autowired
    private GenerateCertificateOfEntitlement generateCertificateOfEntitlement;

    @Autowired
    private GenerateConditionalOrderPronouncedDocument generateConditionalOrderPronouncedDocument;

    @Autowired
    private GenerateConditionalOrderPronouncedCoversheet generateConditionalOrderPronouncedCoversheetDocument;

    @Autowired
    private GenerateFinalOrderCoverLetter generateFinalOrderCoverLetter;

    @Autowired
    private GenerateFinalOrder generateFinalOrder;

    @Autowired
    private RegenerateCourtOrdersNotification regenerateCourtOrdersNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REGENERATE_COURT_ORDERS)
            .forStates(POST_SUBMISSION_STATES)
            .name("Regenerate court orders")
            .description("Regenerate court orders")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, SUPER_USER)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, SOLICITOR))
            .page("regenerateCourtOrderDocs")
            .pageLabel("Regenerate court orders")
            .label("regenerateCourtOrdersWarningLabel", "Updating court orders recreates the Certificate of Entitlement, "
                + "Conditional Order and Final Order, "
                + "based on the latest case data. "
                + "Any other court orders e.g. conditional order refusals, will remain unchanged.\r\n\r\n"
                + "If there have been updates to the case data e.g. change of applicant name, then these will be reflected in the updated "
                + "court orders.\r\n\r\nPrevious versions of court orders will not be stored against the case.");
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker regenerate court orders callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();

        if (caseData.getDocuments().getDocumentGeneratedWithType(CONDITIONAL_ORDER_GRANTED).isPresent()) {
            log.info("Regenerating CO Pronounced document for Case Id: {}", details.getId());
            generateConditionalOrderPronouncedCoversheetDocument.removeExistingAndGenerateConditionalOrderPronouncedCoversheet(details);
            generateConditionalOrderPronouncedDocument.removeExistingAndGenerateNewConditionalOrderGrantedDoc(details);
        }

        if (caseData.getDocuments().getDocumentGeneratedWithType(FINAL_ORDER_GRANTED).isPresent()) {
            log.info("Regenerating Final Order Granted document for Case Id: {}", details.getId());
            generateFinalOrderCoverLetter.removeExistingAndGenerateNewFinalOrderGrantedCoverLetters(details);
            generateFinalOrder.removeExistingAndGenerateNewFinalOrderGrantedDoc(details);
        }

        CaseDetails<CaseData, State> updatedDetails = null;
        if (isNotEmpty(caseData.getConditionalOrder().getCertificateOfEntitlementDocument())) {
            log.info("Regenerating certificate of entitlement document for Case Id: {}", details.getId());
            generateCertificateOfEntitlement.removeExistingAndGenerateNewCertificateOfEntitlementCoverLetters(details);
            updatedDetails = caseTasks(generateCertificateOfEntitlement).run(details);
        }

        if (null != updatedDetails) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(updatedDetails.getData())
                .build();
        } else {
            log.info("Certificate of entitlement doesn't exist for Case Id: {}", details.getId());
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
        }
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker regenerate court orders submitted callback invoked for case id: {}", details.getId());

        final CaseData caseData = details.getData();
        notificationDispatcher.send(regenerateCourtOrdersNotification, caseData, details.getId());
        return SubmittedCallbackResponse.builder().build();
    }
}
