package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Withdrawn;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CitizenSwitchToSoleApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String WITHDRAW_JOINT_APPLICATION = "withdraw-joint-application";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        EnumSet<State> stateSet = EnumSet.noneOf(State.class);
        stateSet.add(AwaitingApplicant1Response);
        stateSet.add(Applicant2Approved);

        configBuilder
            .event(WITHDRAW_JOINT_APPLICATION)
            .forStateTransition(stateSet, Withdrawn)
            .name("Switch to sole application")
            .description("Switch from joint to sole application and withdraw case")
            .grant(CREATE_READ_UPDATE, CREATOR)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Switching to sole application. Withdrawing case {}", details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(Withdrawn)
            .build();
    }
}
