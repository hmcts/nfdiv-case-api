package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.ConfirmService;
import uk.gov.hmcts.divorce.common.service.SubmitConfirmService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorService;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJsNullity;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CaseworkerConfirmService implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_CONFIRM_SERVICE = "caseworker-confirm-service";

    @Autowired
    private SubmitConfirmService submitConfirmService;

    @Autowired
    private ConfirmService confirmService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_CONFIRM_SERVICE)
            .forStates(POST_SUBMISSION_STATES)
            .name("Confirm service")
            .description("Confirm service")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SOLICITOR, SUPER_USER, LEGAL_ADVISOR, JUDGE))
            .page("CaseworkerConfirmService", this::midEvent)
            .pageLabel("Confirm Service")
            .complex(CaseData::getDocuments)
                .optional(CaseDocuments::getDocumentsUploadedOnConfirmService)
                .done()
            .label("applicantLabel", "Name of Applicant - ${applicant1FirstName} ${applicant1LastName}")
            .label("respondentLabel", "Name of Respondent - ${applicant2FirstName} ${applicant2LastName}")
            .complex(CaseData::getApplication)
                .complex(Application::getSolicitorService)
                    .mandatory(SolicitorService::getDateOfService)
                    .mandatory(SolicitorService::getDocumentsServed)
                    .mandatory(SolicitorService::getOnWhomServed)
                    .mandatory(SolicitorService::getHowServed)
                    .optional(SolicitorService::getServiceProcessedByProcessServer)
                    .mandatory(SolicitorService::getServiceDetails,
                        "solServiceHowServed=\"deliveredTo\" OR solServiceHowServed=\"postedTo\"")
                    .mandatory(SolicitorService::getAddressServed)
                    .mandatory(SolicitorService::getBeingThe)
                    .mandatory(SolicitorService::getLocationServed)
                    .mandatory(SolicitorService::getSpecifyLocationServed, "solServiceLocationServed=\"otherSpecify\"")
                    .mandatoryWithLabel(SolicitorService::getServiceSotName, "Solicitor's/Applicant's name")
                    .optional(SolicitorService::getServiceSotFirm)
                    .mandatory(SolicitorService::getStatementOfTruth)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();

        final List<String> validationErrors = confirmService.validateConfirmService(caseData);

        if (!validationErrors.isEmpty()) {
            return confirmService.getErrorResponse(details, validationErrors);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();

        var serviceMethod = !isNull(caseData.getApplication().getServiceMethod())
            ? caseData.getApplication().getServiceMethod().toString()
            : null;

        log.info("Caseworker confirm service about to submit callback invoked with service method {}, Case Id: {}",
            serviceMethod,
            details.getId());

        final CaseDetails<CaseData, State> updateDetails = submitConfirmService.submitConfirmService(details);

        log.info("Due date after submit task is {}", updateDetails.getData().getDueDate());

        confirmService.addToDocumentsUploaded(updateDetails);

        State state = updateDetails.getData().isJudicialSeparationCase()
            && !isEmpty(updateDetails.getData().getApplication().getSolicitorService().getServiceProcessedByProcessServer())
            ? AwaitingJsNullity
            : updateDetails.getState();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updateDetails.getData())
            .state(state)
            .build();
    }
}
