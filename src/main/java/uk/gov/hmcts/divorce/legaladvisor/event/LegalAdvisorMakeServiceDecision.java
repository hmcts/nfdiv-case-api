package uk.gov.hmcts.divorce.legaladvisor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.ServiceApplicationNotification;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ServiceOrderTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceApplicationRefusalReason.ADMIN_REFUSAL;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJsNullity;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ServiceAdminRefusal;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DEEMED_AS_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DEEMED_SERVICE_REFUSED_FILE_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DISPENSED_AS_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DISPENSED_WITH_SERVICE_REFUSED_FILE_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SERVICE_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SERVICE_REFUSAL_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DEEMED_SERVICE_REFUSED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DISPENSE_WITH_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DISPENSE_WITH_SERVICE_REFUSED;

@Component
@Slf4j
public class LegalAdvisorMakeServiceDecision implements CCDConfig<CaseData, State, UserRole> {
    public static final String LEGAL_ADVISOR_SERVICE_DECISION = "legal-advisor-service-decision";

    @Autowired
    private Clock clock;

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private ServiceOrderTemplateContent serviceOrderTemplateContent;

    @Autowired
    private ServiceApplicationNotification serviceApplicationNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private HoldingPeriodService holdingPeriodService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(LEGAL_ADVISOR_SERVICE_DECISION)
            .forStates(AwaitingServiceConsideration)
            .name("Make service decision")
            .description("Make service decision")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, LEGAL_ADVISOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, SOLICITOR, CITIZEN, SYSTEMUPDATE, JUDGE))
            .page("makeServiceDecision")
            .pageLabel("Approve service application")
            .complex(CaseData::getAlternativeService)
                .mandatory(AlternativeService::getServiceApplicationGranted)
                .readonly(AlternativeService::getAlternativeServiceType, "serviceApplicationGranted=\"NEVER_SHOW\"")
                .mandatory(AlternativeService::getDeemedServiceDate,
                "alternativeServiceType=\"deemed\" AND serviceApplicationGranted=\"Yes\"")
                .done()
            .page("makeServiceDecision-2")
            .showCondition("serviceApplicationGranted=\"No\"")
            .pageLabel("Reason for refusal")
                .complex(CaseData::getAlternativeService)
                .mandatory(AlternativeService::getRefusalReason)
                .mandatory(AlternativeService::getServiceApplicationRefusalReason)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Legal advisor make service decision about to submit callback invoked for Case Id: {}", details.getId());

        var caseDataCopy = details.getData().toBuilder().build();
        var serviceApplication = caseDataCopy.getAlternativeService();
        var application = caseDataCopy.getApplication();

        State endState = details.getState();

        log.info("Application end state is {}", endState);

        serviceApplication.setServiceApplicationDecisionDate(LocalDate.now(clock));

        if (serviceApplication.isApplicationGranted()) {
            log.info("Service application granted for case id {}", details.getId());

            if (application.getIssueDate() == null) {
                endState = Submitted;
            } else if (caseDataCopy.getApplicationType().isSole()
                && caseDataCopy.isJudicialSeparationCase()
                && (DEEMED.equals(serviceApplication.getAlternativeServiceType())
                    || DISPENSED.equals(serviceApplication.getAlternativeServiceType()))) {
                endState = AwaitingJsNullity;
            } else {
                endState = Holding;
                if (caseDataCopy.getApplicationType().isSole()) {
                    caseDataCopy.setDueDate(holdingPeriodService.getDueDateFor(application.getIssueDate()));
                }
            }

            if (DISPENSED.equals(serviceApplication.getAlternativeServiceType())) {
                generateAndSetOrderToDeemedOrDispenseDocument(
                    caseDataCopy,
                    details.getId(),
                    DISPENSED_AS_SERVICE_GRANTED,
                    DISPENSE_WITH_SERVICE_GRANTED,
                    SERVICE_ORDER_TEMPLATE_ID);
            } else if (DEEMED.equals(serviceApplication.getAlternativeServiceType())) {
                generateAndSetOrderToDeemedOrDispenseDocument(
                    caseDataCopy,
                    details.getId(),
                    DEEMED_AS_SERVICE_GRANTED,
                    DocumentType.DEEMED_AS_SERVICE_GRANTED,
                    SERVICE_ORDER_TEMPLATE_ID);
            }
        } else {
            if (ADMIN_REFUSAL.equals(caseDataCopy.getAlternativeService().getRefusalReason())) {
                endState = ServiceAdminRefusal;
            } else {
                if (DISPENSED.equals(serviceApplication.getAlternativeServiceType())) {
                    generateAndSetOrderToDeemedOrDispenseDocument(
                        caseDataCopy,
                        details.getId(),
                        DISPENSED_WITH_SERVICE_REFUSED_FILE_NAME,
                        DISPENSE_WITH_SERVICE_REFUSED,
                        SERVICE_REFUSAL_TEMPLATE_ID);
                    endState = caseDataCopy.getApplication().getIssueDate() != null ? AwaitingAos : ServiceAdminRefusal;
                } else if (DEEMED.equals(serviceApplication.getAlternativeServiceType())) {
                    generateAndSetOrderToDeemedOrDispenseDocument(
                        caseDataCopy,
                        details.getId(),
                        DEEMED_SERVICE_REFUSED_FILE_NAME,
                        DEEMED_SERVICE_REFUSED,
                        SERVICE_REFUSAL_TEMPLATE_ID);
                    endState = AwaitingAos;
                }
            }
        }

        log.info("ServiceApplication decision. End State is {} Due date is {}", endState, caseDataCopy.getDueDate());

        log.info("Sending ServiceApplicationNotification case ID: {}", details.getId());
        if (endState != ServiceAdminRefusal) {
            notificationDispatcher.send(serviceApplicationNotification, caseDataCopy, details.getId());
        }

        caseDataCopy.archiveAlternativeServiceApplicationOnCompletion();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDataCopy)
            .state(endState)
            .build();
    }

    private void generateAndSetOrderToDeemedOrDispenseDocument(final CaseData caseDataCopy,
                                                               final Long caseId,
                                                               final String fileName,
                                                               final DocumentType documentType, String templateId) {
        log.info("Generating order to dispense document for templateId : {} caseId: {}", templateId, caseId);

        Document document = caseDataDocumentService.renderDocument(
            serviceOrderTemplateContent.apply(caseDataCopy, caseId),
            caseId,
            templateId,
            caseDataCopy.getApplicant1().getLanguagePreference(),
            fileName
        );

        var deemedOrDispensedDoc = DivorceDocument
            .builder()
            .documentLink(document)
            .documentFileName(document.getFilename())
            .documentType(documentType)
            .build();

        caseDataCopy.getDocuments().setDocumentsGenerated(addDocumentToTop(
            caseDataCopy.getDocuments().getDocumentsGenerated(),
            deemedOrDispensedDoc
        ));
    }
}
