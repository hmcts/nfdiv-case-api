package uk.gov.hmcts.divorce.citizen.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class CitizenFinalOrderDelayReason implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_FINAL_ORDER_DELAY_REASON = "citizen-final-order-delay-reason";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_FINAL_ORDER_DELAY_REASON)
            .forStateTransition(FinalOrderOverdue, FinalOrderRequested)
            .name("Citizen FO delay reason")
            .description("Citizen final order delay reason")
            .grant(CREATE_READ_UPDATE, CREATOR)
            .grant(READ, SUPER_USER, LEGAL_ADVISOR, CASE_WORKER);
    }
}

