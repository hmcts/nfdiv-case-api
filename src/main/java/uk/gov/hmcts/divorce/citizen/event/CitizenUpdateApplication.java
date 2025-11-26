package uk.gov.hmcts.divorce.citizen.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Archived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.NewPaperCase;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Rejected;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Withdrawn;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CitizenUpdateApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_UPDATE = "citizen-update-application";

    private static final EnumSet<State> CITIZEN_UPDATE_STATES = EnumSet.complementOf(EnumSet.of(
        AwaitingApplicant2Response,
        Applicant2Approved,
        AwaitingPayment,
        NewPaperCase,
        Withdrawn,
        Rejected,
        Archived
    ));

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_UPDATE)
            .forStates(CITIZEN_UPDATE_STATES)
            .showCondition(NEVER_SHOW)
            .name("Patch case")
            .description("Patch a divorce or dissolution")
            .grant(CREATE_READ_UPDATE, CREATOR);
    }
}
