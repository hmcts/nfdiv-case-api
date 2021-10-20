package uk.gov.hmcts.divorce.legaladvisor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ServiceOrderTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DEEMED_AS_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DISPENSED_AS_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SERVICE_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DISPENSE_WITH_SERVICE_GRANTED;

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

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(LEGAL_ADVISOR_SERVICE_DECISION)
            .forStates(Draft, AwaitingServiceConsideration)
            .name("Make service decision")
            .description("Make service decision")
            .showSummary()
            .showEventNotes()
            .explicitGrants()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, LEGAL_ADVISOR)
            .grant(READ, CASE_WORKER, SUPER_USER, SOLICITOR, CITIZEN, SYSTEMUPDATE))
            .page("makeServiceDecision")
            .pageLabel("Approve service application")
            .complex(CaseData::getAlternativeService)
            .mandatory(AlternativeService::getServiceApplicationGranted)
            .readonly(AlternativeService::getAlternativeServiceType, "serviceApplicationGranted=\"NEVER_SHOW\"")
            .mandatory(AlternativeService::getDeemedServiceDate,
                "alternativeServiceType=\"deemed\" AND serviceApplicationGranted=\"Yes\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Legal advisor make service decision about to submit callback invoked");

        var caseDataCopy = details.getData().toBuilder().build();
        var serviceApplication = caseDataCopy.getAlternativeService();

        State endState = details.getState();

        if (serviceApplication.getServiceApplicationGranted().toBoolean()) {
            log.info("Service application granted for case id {}", details.getId());
            serviceApplication.setServiceApplicationDecisionDate(LocalDate.now(clock));
            endState = Holding;

            if (DISPENSED.equals(serviceApplication.getAlternativeServiceType())) {
                generateAndSetOrderToDeemedOrDispenseDocument(
                    caseDataCopy,
                    details.getId(),
                    DISPENSED_AS_SERVICE_GRANTED,
                    DISPENSE_WITH_SERVICE_GRANTED
                );
            } else if (DEEMED.equals(serviceApplication.getAlternativeServiceType())) {
                generateAndSetOrderToDeemedOrDispenseDocument(
                    caseDataCopy,
                    details.getId(),
                    DEEMED_AS_SERVICE_GRANTED,
                    DocumentType.DEEMED_AS_SERVICE_GRANTED
                );
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDataCopy)
            .state(endState)
            .build();
    }

    private void generateAndSetOrderToDeemedOrDispenseDocument(final CaseData caseDataCopy,
                                                               final Long caseId,
                                                               final String fileName,
                                                               final DocumentType documentType) {
        log.info("Generating order to dispense document for templateId : {} caseId: {}", SERVICE_ORDER_TEMPLATE_ID, caseId);

        Document document = caseDataDocumentService.renderDocument(
            serviceOrderTemplateContent.apply(caseDataCopy, caseId),
            caseId,
            SERVICE_ORDER_TEMPLATE_ID,
            caseDataCopy.getApplicant1().getLanguagePreference(),
            fileName
        );

        var deemedOrDispensedDoc = DivorceDocument
            .builder()
            .documentLink(document)
            .documentFileName(document.getFilename())
            .documentType(documentType)
            .build();

        caseDataCopy.addToDocumentsGenerated(
            ListValue
                .<DivorceDocument>builder()
                .id(UUID.randomUUID().toString())
                .value(deemedOrDispensedDoc)
                .build()
        );
    }
}
