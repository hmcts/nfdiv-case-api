package uk.gov.hmcts.divorce.citizen.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.WithdrawCaseService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Collections;
import java.util.EnumSet;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingResponseToHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PendingRefund;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Withdrawn;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CitizenWithdrawn implements CCDConfig<CaseData, State, UserRole> {
    public static final String CITIZEN_WITHDRAWN = "citizen-withdrawn";

    private final WithdrawCaseService withdrawCaseService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CITIZEN_WITHDRAWN)
            .forAllStates()
            .name("Citizen Withdraw")
            .description("Citizen Withdraw")
            .showEventNotes()
            .showCondition(NEVER_SHOW)
            .grant(CREATE_READ_UPDATE, CREATOR, APPLICANT_2)
            .grantHistoryOnly(
                CASE_WORKER,
                SOLICITOR,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE)
            .aboutToSubmitCallback(this::aboutToSubmit));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", CITIZEN_WITHDRAWN, details.getId());

        boolean canWithdrawApplication = canApplicationBeWithdrawn(details.getState(), details.getData());

        if (!canWithdrawApplication) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList("Application cannot be withdrawn at this stage."))
                .build();
        }

        State stateToSet = (details.getData().getApplication().getDateSubmitted() == null) ? Withdrawn : PendingRefund;

        withdrawCaseService.withdraw(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(stateToSet)
            .build();
    }

    private boolean canApplicationBeWithdrawn(State state, CaseData data) {
        boolean isAllowedPreSubmissionState = EnumSet.of(Draft, AwaitingApplicant1Response, AwaitingApplicant2Response, Applicant2Approved,
            AwaitingPayment, AwaitingResponseToHWFDecision).contains(state);
        boolean isApplicationSubmittedButNotIssued = data.getApplication().getDateSubmitted() != null
            && data.getApplication().getIssueDate() == null;;

        return isAllowedPreSubmissionState || isApplicationSubmittedButNotIssued;
    }
}
