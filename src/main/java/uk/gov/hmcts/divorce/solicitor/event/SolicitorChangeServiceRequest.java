package uk.gov.hmcts.divorce.solicitor.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.ReIssueApplicationService;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant1NoticeOfProceeding;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant2NoticeOfProceedings;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateD10Form;
import uk.gov.hmcts.divorce.caseworker.service.task.SetNoticeOfProceedingDetailsForRespondent;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.ApplicationIssuedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.List;

import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.REISSUE_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_PRE_AWAITING_CO_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateCaseFieldsForCourtService;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemIssueSolicitorServicePack.SYSTEM_ISSUE_SOLICITOR_SERVICE_PACK;

@Component
@Slf4j
@RequiredArgsConstructor
public class SolicitorChangeServiceRequest implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_CHANGE_SERVICE_REQUEST = "solicitor-change-service-request";

    public static final String NOT_ISSUED_ERROR = "The application must have been issued before you can change the service request.";
    private static final String CHANGE_SERVICE_REQUEST = "Change service request";

    private final ApplicationIssuedNotification applicationIssuedNotification;

    private final CcdUpdateService ccdUpdateService;

    private final SetNoticeOfProceedingDetailsForRespondent setNoticeOfProceedingDetailsForRespondent;

    private final GenerateApplicant1NoticeOfProceeding generateApplicant1NoticeOfProceeding;

    private final GenerateApplicant2NoticeOfProceedings generateApplicant2NoticeOfProceedings;

    private final GenerateD10Form generateD10Form;

    private final GenerateD84Form generateD84Form;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final ReIssueApplicationService reIssueApplicationService;

    @Value("${aos_pack.due_date_offset_days}")
    private long dueDateOffsetDays;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SOLICITOR_CHANGE_SERVICE_REQUEST)
            .forStates(POST_SUBMISSION_PRE_AWAITING_CO_STATES)
            .showCondition("issueDate=\"*\" AND applicationType=\"soleApplication\"")
            .name(CHANGE_SERVICE_REQUEST)
            .description(CHANGE_SERVICE_REQUEST)
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, JUDGE))
            .page("changeServiceRequest")
            .pageLabel(CHANGE_SERVICE_REQUEST)
            .complex(CaseData::getApplication)
            .mandatory(Application::getServiceMethod);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("Solicitor change service request about to start callback invoked with Case Id: {}", details.getId());

        final Application application = details.getData().getApplication();
        final boolean notIssued = application.getIssueDate() == null;

        if (notIssued) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of(NOT_ISSUED_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder().build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Solicitor change service request about to submit callback invoked with Case Id: {}", details.getId());

        CaseData caseData = details.getData();
        final Application application = caseData.getApplication();
        final Applicant applicant2 = caseData.getApplicant2();

        if (application.isPersonalServiceMethod()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(singletonList("You may not select Personal Service. Please select Solicitor or Court Service."))
                .build();
        } else if (application.isSolicitorServiceMethod()
            && applicant2.isConfidentialContactDetails()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(singletonList("You may not select Solicitor Service if the respondent is confidential."))
                .build();
        } else if (validateCaseFieldsForCourtService(caseData).size() > 0) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(singletonList("Solicitor cannot select court service because the respondent has an international address."))
                .build();
        } else if (application.isCourtServiceMethod() && beforeDetails.getData().getApplication().isSolicitorServiceMethod()) {
            caseData.setDueDate(now().plusDays(dueDateOffsetDays));
        }

        log.info("Regenerate NOP for App and Respondent, and D10 for case id: {}", details.getId());
        caseTasks(generateApplicant1NoticeOfProceeding, generateApplicant2NoticeOfProceedings, generateD10Form).run(details);
        State state = application.isCourtServiceMethod() ? AwaitingAos : AwaitingService;

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(state)
            .build();
    }

    public SubmittedCallbackResponse submitted(final CaseDetails<CaseData, State> details,
                                               final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Solicitor change service request submitted callback invoked for case id: {}", details.getId());
        final Application application = details.getData().getApplication();

        if (application.isSolicitorServiceMethod()) {
            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuthorization = authTokenGenerator.generate();

            log.info("Send Notification to Applicant 1 Solicitor for case id: {}", details.getId());
            applicationIssuedNotification.sendToApplicant1Solicitor(details.getData(), details.getId());

            log.info("Submitting system-issue-solicitor-service-pack event for case id: {}", details.getId());
            ccdUpdateService.submitEvent(details.getId(), SYSTEM_ISSUE_SOLICITOR_SERVICE_PACK, user, serviceAuthorization);
        } else {
            log.info("Send Notifications for case id: {}", details.getId());
            reIssueApplicationService.sendNotifications(details, REISSUE_CASE);
        }

        return SubmittedCallbackResponse.builder().build();
    }
}
