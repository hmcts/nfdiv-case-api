package uk.gov.hmcts.divorce.citizen.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CitizenResendInvite implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_RESEND_INVITE = "citizen-resend-invite";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_RESEND_INVITE)
            .forStates(AwaitingApplicant2Response)
            .name("Update applicant 2 email")
            .description("Citizen event for applicant 1 to update applicant 2 email pre-submission")
            .grant(CREATE_READ_UPDATE, CREATOR);
    }
}
