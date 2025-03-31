package uk.gov.hmcts.divorce.citizen.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_ISSUE_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CitizenNoResponseJourney implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_NO_RESPONSE_JOURNEY = "citizen-no-response-journey";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_NO_RESPONSE_JOURNEY)
            .forStates(POST_ISSUE_STATES)
            .showCondition(NEVER_SHOW)
            .name("Set noResponseJourney options")
            .description("Set no response journey options")
            .grant(CREATE_READ_UPDATE, CREATOR);
    }
}
