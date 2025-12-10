package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.WithdrawCaseService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.divorcecase.model.WithdrawApplicationReasonType;

import java.util.Collections;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Withdrawn;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseworkerWithdrawn implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_WITHDRAWN = "caseworker-withdrawn";
    public static final String DETAILS_NOT_PROVIDED = "You need to provide details for withdrawing the application.";

    private final WithdrawCaseService withdrawCaseService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_WITHDRAWN)
            .forStateTransition(POST_SUBMISSION_STATES, Withdrawn)
            .name("Withdraw")
            .description("Withdrawn")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE,
                CASE_WORKER)
            .grantHistoryOnly(
                SOLICITOR,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE)
            .aboutToSubmitCallback(this::aboutToSubmit))
            .page("withdrawReason", this::midEvent)
            .pageLabel("Reason for withdrawal")
            .complex(CaseData::getApplication)
            .mandatory(Application::getCwWithdrawApplicationReason)
            .optional(Application::getCwWithdrawApplicationDetails)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        CaseData caseData = details.getData();
        if (WithdrawApplicationReasonType.OTHER.equals(caseData.getApplication().getCwWithdrawApplicationReason())
            && caseData.getApplication().getCwWithdrawApplicationDetails() == null) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(DETAILS_NOT_PROVIDED))
                .build();
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Caseworker withdrawn about to submit callback invoked for Case Id: {}", details.getId());

        withdrawCaseService.withdraw(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(details.getState())
            .build();
    }
}
