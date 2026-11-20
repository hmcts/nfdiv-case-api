package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.ServiceApplicationRejectedNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRejectGeneralApplication.validateStateTransition;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseworkerRejectServiceApplication implements CCDConfig<CaseData, State, UserRole> {
    private static final String ALWAYS_SHOW = "alternativeServiceType!=\"ALWAYS_SHOW\"";
    private static final String NEVER_SHOW = "alternativeServiceType=\"ALWAYS_SHOW\"";
    public static final String CASEWORKER_REJECT_SERVICE_APPLICATION = "caseworker-reject-service-application";
    private static final String REJECT_SERVICE_APPLICATION = "Reject Service Application";

    private final ServiceApplicationRejectedNotification serviceApplicationRejectedNotification;
    private final NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(CASEWORKER_REJECT_SERVICE_APPLICATION)
            .forStates(POST_SUBMISSION_STATES)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .aboutToStartCallback(this::aboutToStart)
            .name(REJECT_SERVICE_APPLICATION)
            .description(REJECT_SERVICE_APPLICATION)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, SUPER_USER)
            .grantHistoryOnly(
                SOLICITOR,
                CASE_WORKER,
                LEGAL_ADVISOR,
                JUDGE))
            .page("rejectServiceApplication", this::midEvent)
            .pageLabel(REJECT_SERVICE_APPLICATION)
            .complex(CaseData::getAlternativeService)
                .label("serviceApplicationTypeLabel", "## Note: The following service application will be rejected")
                .readonly(AlternativeService::getAlternativeServiceType)
                .readonly(AlternativeService::getReceivedServiceApplicationDate)
                .readonlyNoSummary(AlternativeService::getServiceApplicationSubmittedOnline, NEVER_SHOW)
            .done()
            .complex(CaseData::getApplication)
            .readonly(Application::getCurrentState)
            .mandatoryWithLabel(Application::getStateToTransitionApplicationTo, "State to transfer case to")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_REJECT_SERVICE_APPLICATION, details.getId());

        final CaseData caseData = details.getData();

        caseData.getApplication().setCurrentState(details.getState());

        State defaultState = caseData.getApplication().getIssueDate() == null ? AwaitingDocuments : AwaitingAos;
        caseData.getApplication().setStateToTransitionApplicationTo(defaultState);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(null)
            .warnings(null)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(final CaseDetails<CaseData, State> details,
                                                                  final CaseDetails<CaseData, State> detailsBefore) {

        final CaseData caseData = details.getData();
        State state = caseData.getApplication().getStateToTransitionApplicationTo();
        List<String> validationErrors = validateStateTransition(caseData, state);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(validationErrors)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked, Case Id: {}", CASEWORKER_REJECT_SERVICE_APPLICATION, details.getId());
        var caseData = details.getData();

        if (Objects.isNull(caseData.getAlternativeService())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of("No service application to reject."))
                .build();
        }

        if (YesOrNo.YES.equals(caseData.getAlternativeService().getServiceApplicationSubmittedOnline())) {
            notificationDispatcher.send(serviceApplicationRejectedNotification, caseData, details.getId());
        }

        State state =  caseData.getApplication().getStateToTransitionApplicationTo();

        caseData.setAlternativeService(new AlternativeService());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(state)
            .build();
    }
}
