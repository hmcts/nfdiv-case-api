package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
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

import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_PRE_AWAITING_CO_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class SolicitorConfirmService implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_CONFIRM_SERVICE = "solicitor-confirm-service";

    public static final String SOLICITOR_SERVICE_AS_THE_SERVICE_METHOD_ERROR =
        "This event can only be used for a case with Solicitor Service as the service method";

    @Autowired
    private SubmitConfirmService submitConfirmService;

    @Autowired
    private ConfirmService confirmService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        pageBuilder
            .page("SolConfirmService", this::midEvent)
            .pageLabel("Certificate of Service - Confirm Service")
            .complex(CaseData::getDocuments)
            .optional(CaseDocuments::getDocumentsUploadedOnConfirmService)
            .done()
            .label("petitionerLabel", "Name of Applicant - ${applicant1FirstName} ${applicant1LastName}")
            .label("respondentLabel", "Name of Respondent - ${applicant2FirstName} ${applicant2LastName}")
            .complex(CaseData::getApplication)
            .complex(Application::getSolicitorService)
            .mandatory(SolicitorService::getDateOfService)
            .mandatory(SolicitorService::getDocumentsServed)
            .mandatory(SolicitorService::getOnWhomServed)
            .mandatory(SolicitorService::getHowServed)
            .optional(SolicitorService::getServiceProcessedByProcessServer)
            .mandatory(SolicitorService::getServiceDetails, "solServiceHowServed=\"deliveredTo\" OR solServiceHowServed=\"postedTo\"")
            .mandatory(SolicitorService::getAddressServed)
            .mandatory(SolicitorService::getBeingThe)
            .mandatory(SolicitorService::getLocationServed)
            .mandatory(SolicitorService::getSpecifyLocationServed, "solServiceLocationServed=\"otherSpecify\"")
            .mandatory(SolicitorService::getServiceSotName)
            .readonly(SolicitorService::getTruthStatement)
            .mandatory(SolicitorService::getServiceSotFirm)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();

        final List<String> validationErrors = validateConfirmSolicitorService(caseData);

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

        log.info("Solicitor confirm service about to submit callback invoked with service method {}, Case Id: {}",
            caseData.getApplication().getServiceMethod().toString(),
            details.getId());

        final CaseDetails<CaseData, State> updateDetails = submitConfirmService.submitConfirmService(details);

        confirmService.addToDocumentsUploaded(updateDetails);

        log.info("Due date after submit Task is {}", updateDetails.getData().getDueDate());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updateDetails.getData())
            .state(updateDetails.getState())
            .build();
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(SOLICITOR_CONFIRM_SERVICE)
            .forStates(ArrayUtils.addAll(POST_SUBMISSION_PRE_AWAITING_CO_STATES, Holding))
            .name("Solicitor confirm service")
            .description("Solicitor confirm service")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }

    private List<String> validateConfirmSolicitorService(CaseData caseData) {
        List<String> errors = confirmService.validateConfirmService(caseData);

        if (!caseData.getApplication().isSolicitorServiceMethod()) {
            errors.add(SOLICITOR_SERVICE_AS_THE_SERVICE_METHOD_ERROR);
        }

        return errors;
    }
}
