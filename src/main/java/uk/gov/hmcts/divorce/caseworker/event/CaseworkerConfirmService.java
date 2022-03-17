package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorService;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorSubmitConfirmService;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CaseworkerConfirmService implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_CONFIRM_SERVICE = "caseworker-confirm-service";

    @Autowired
    private SolicitorSubmitConfirmService solicitorSubmitConfirmService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_CONFIRM_SERVICE)
            .forStateTransition(POST_SUBMISSION_STATES, AwaitingAos)
            .name("Confirm service")
            .description("Confirm service")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SOLICITOR, SUPER_USER, LEGAL_ADVISOR))
            .page("caseworkerConfirmService")
            .pageLabel("Confirm Service")
            .optional(CaseData::getDocumentsUploaded)
            .label("applicantLabel", "Name of Applicant - ${applicant1FirstName} ${applicant1LastName}")
            .label("respondentLabel", "Name of Respondent - ${applicant2FirstName} ${applicant2LastName}")
            .complex(CaseData::getApplication)
                .complex(Application::getSolicitorService)
                    .mandatory(SolicitorService::getDateOfService)
                    .mandatory(SolicitorService::getDocumentsServed)
                    .mandatory(SolicitorService::getOnWhomServed)
                    .mandatory(SolicitorService::getHowServed)
                    .mandatory(SolicitorService::getServiceDetails,
                        "solServiceHowServed=\"deliveredTo\" OR solServiceHowServed=\"postedTo\"")
                    .mandatory(SolicitorService::getAddressServed)
                    .mandatory(SolicitorService::getBeingThe)
                    .mandatory(SolicitorService::getLocationServed)
                    .mandatory(SolicitorService::getSpecifyLocationServed, "solServiceLocationServed=\"otherSpecify\"")
                    .mandatoryWithLabel(SolicitorService::getServiceSotName, "Solicitor's/Applicant's name")
                    .readonly(SolicitorService::getTruthStatement)
                    .mandatory(SolicitorService::getServiceSotFirm)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();

        log.info("Caseworker confirm service about to submit callback invoked with service method {}",
            caseData.getApplication().getSolServiceMethod().toString());

        if (!caseData.getApplication().isSolicitorServiceMethod()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(singletonList("This event can only be used for a case with solicitor service as the service method"))
                .build();
        }

        final CaseDetails<CaseData, State> updateDetails = solicitorSubmitConfirmService.submitConfirmService(details);

        log.info("Due date after submit task is {}", updateDetails.getData().getDueDate());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updateDetails.getData())
            .state(updateDetails.getState())
            .build();
    }
}
