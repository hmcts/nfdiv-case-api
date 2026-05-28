package uk.gov.hmcts.divorce.solicitor.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.CaseTerminationService;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.EnumSet;
import java.util.List;

import static org.springframework.cloud.openfeign.security.OAuth2AccessTokenInterceptor.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Archived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PendingRefund;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Withdrawn;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorWithdrawn implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_WITHDRAWN = "solicitor-withdrawn";

    private static final String WARNING_LABEL = """
        ### You will no longer have access to this case
        If the case had been shared with any colleagues, they will also lose access.
        """;
    public static final String CASE_WITHDRAWN_CONFIRMATION_HEADER = "# Case Withdrawn\n";

    public static final String CASE_WITHDRAWN_CONFIRMATION_LABEL = """
        This case has now been withdrawn.
        ### What happens next
        This case will no longer appear in your case list.

        [View case list](/cases)
        """;

    public static final String CANNOT_WITHDRAW_CASE = "You cannot withdraw this case at this stage";

    public static final String RESPONDENT_SOLICITOR_ERROR = "You cannot withdraw this case";

    private final CaseTerminationService caseTerminationService;

    private final CcdAccessService ccdAccessService;

    private final HttpServletRequest httpServletRequest;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(SOLICITOR_WITHDRAWN)
            .forAllStates()
            .name("Withdraw application")
            .description("Application withdrawn")
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted))
            .page("withdrawApplication")
            .pageLabel("# Withdraw application")
            .label("warningWithdrawCase", WARNING_LABEL);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", SOLICITOR_WITHDRAWN, details.getId());

        final boolean isRepresentingApplicant2 = isApplicant2Solicitor(details.getId());

        if (isRepresentingApplicant2 && ApplicationType.SOLE_APPLICATION.equals(details.getData().getApplicationType())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of(RESPONDENT_SOLICITOR_ERROR))
                .build();
        }

        boolean canSolicitorWithdrawApplication = canApplicationBeWithdrawn(details.getState(), details.getData());

        if (!canSolicitorWithdrawApplication) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of(CANNOT_WITHDRAW_CASE))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder().build();
    }

    private boolean isApplicant2Solicitor(Long id) {
        String authHeader = httpServletRequest.getHeader(AUTHORIZATION);

        return ccdAccessService.isApplicant2(authHeader, id);
    }

    private boolean canApplicationBeWithdrawn(State state, CaseData data) {
        boolean isAllowedPreSubmissionState =
            EnumSet.of(Draft, Archived, Applicant2Approved, AwaitingApplicant1Response, AwaitingApplicant2Response).contains(state);
        boolean isApplicationSubmittedButNotIssued = data.getApplication().getDateSubmitted() != null
            && data.getApplication().getIssueDate() == null;;

        return isAllowedPreSubmissionState || isApplicationSubmittedButNotIssued;
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_WITHDRAWN, details.getId());

        State stateToSet = (details.getData().getApplication().getDateSubmitted() == null) ? Withdrawn : PendingRefund;

        caseTerminationService.withdraw(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(stateToSet)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for Case Id: {}", SOLICITOR_WITHDRAWN, details.getId());

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(CASE_WITHDRAWN_CONFIRMATION_HEADER)
            .confirmationBody(CASE_WITHDRAWN_CONFIRMATION_LABEL)
            .build();
    }
}
