package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
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

    private static final String CONFIRM_SERVICE_SOL_GUIDE = "Refer to the <a href=\"https://www.gov.uk/government/publications/myhmcts-how-"
            + "to-make-follow-up-applications-for-a-divorce-or-dissolution/change-how-the-application-was-served-or-confirm-it-has-been-"
            + "served target=\"_blank\" rel=\"noopener noreferrer\">Solicitor Guidance</a>:";

    public static final String SOLICITOR_CONFIRM_SERVICE = "solicitor-confirm-service";

    public static final String SOLICITOR_SERVICE_AS_THE_SERVICE_METHOD_ERROR =
        "This event can only be used for a case with Solicitor Service as the service method";

    public static final String NOT_ISSUED_ERROR =
        "The application must have been issued to use this event";

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
            .mandatory(SolicitorService::getFirstAttemptToServe)
            .mandatory(SolicitorService::getDocumentsPreviouslyReturned, "solServiceFirstAttemptToServe=\"No\"")
            .mandatory(SolicitorService::getDetailsOfPreviousService, "solServiceDocumentsPreviouslyReturned=\"Yes\""
                + " AND solServiceFirstAttemptToServe=\"No\"")
            .mandatory(SolicitorService::getDatePreviousServiceReturned, "solServiceDocumentsPreviouslyReturned=\"Yes\""
                + " AND solServiceFirstAttemptToServe=\"No\"")
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
            .done()
            .label("confirmServiceSolGuide", CONFIRM_SERVICE_SOL_GUIDE);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("Solicitor confirm service about to start callback invoked with Case Id: {}", details.getId());

        final Application application = details.getData().getApplication();
        final boolean notIssued = application.getIssueDate() == null;

        if (notIssued) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of(NOT_ISSUED_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder().build();
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

        final SolicitorService solicitorService = caseData.getApplication().getSolicitorService();

        if (solicitorService.getFirstAttemptToServe() == YesOrNo.YES) {
            solicitorService.setDocumentsPreviouslyReturned(null);
            solicitorService.setDetailsOfPreviousService(null);
            solicitorService.setDatePreviousServiceReturned(null);
        }

        if (solicitorService.getFirstAttemptToServe() == YesOrNo.NO
            && solicitorService.getDocumentsPreviouslyReturned() == YesOrNo.NO) {
            solicitorService.setDetailsOfPreviousService(null);
            solicitorService.setDatePreviousServiceReturned(null);
        }

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
            .showCondition("issueDate=\"*\"")
            .aboutToStartCallback(this::aboutToStart)
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
