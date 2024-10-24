package uk.gov.hmcts.divorce.citizen.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class TestInvalidEvent implements CCDConfig<CaseData, State, UserRole> {

    public static final String EVENT_NAME_TOO_LONG = "event-name-too-long-zzzzzzzzzzzzzzzzzzzzzzzzzzz";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(EVENT_NAME_TOO_LONG)
            .forState(AwaitingPayment)
            .showCondition(NEVER_SHOW)
            .name("Name too long zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz")
            .description("Description too long zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .grantHistoryOnly(SUPER_USER, CASE_WORKER, LEGAL_ADVISOR);
    }
}

