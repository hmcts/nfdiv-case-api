package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerRemoveRespondentAddress implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_REMOVE_APPLICANT2_ADDRESS = "remove-applicant2-address";
    private static final String WARNING_LABEL = """
        ### When you submit this event, respondent address will be removed from the case.
        """;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REMOVE_APPLICANT2_ADDRESS)
            .forStates(POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED)
            .showCondition("applicationType=\"soleApplication\"")
            .showEventNotes()
            .name("Remove respondent address")
            .description("Remove respondent address")
            .grant(CREATE_READ_UPDATE, SUPER_USER)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, JUDGE))
            .page("removeApp2Address")
            .pageLabel("Remove respondent address")
            .label("warningWithdrawCase", WARNING_LABEL);;
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("{} aboutToSubmit callback invoked for Case Id: {}", CASEWORKER_REMOVE_APPLICANT2_ADDRESS, details.getId());

        final CaseData caseData = details.getData();
        caseData.getApplicant2().setAddress(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
