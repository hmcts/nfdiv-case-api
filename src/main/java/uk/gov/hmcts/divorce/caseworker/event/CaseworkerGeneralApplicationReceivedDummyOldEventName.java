package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;


/* The up to date version of this event that is being triggered in production is CaseworkerGeneralApplicationReceived
The event name for CaseworkerGeneralApplicationReceived was changed in NFDIV-4226 and this caused uses of the old event name to disappear
from case history. This dummy class generates CCD config to add back read permissions for the old event name.
 */
@Slf4j
@Component
public class CaseworkerGeneralApplicationReceivedDummyOldEventName implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_GENERAL_APPLICATION_RECEIVED = "caseworker-general-application-received";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        addEventConfig(configBuilder);
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_GENERAL_APPLICATION_RECEIVED)
            .forStates()
            .showCondition(NEVER_SHOW)
            .name("General application received")
            .description("General application received")
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }
}
