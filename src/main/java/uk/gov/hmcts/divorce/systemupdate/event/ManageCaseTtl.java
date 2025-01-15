package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.type.TTL;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.TTL_PROFILE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class ManageCaseTtl implements CCDConfig<CaseData, State, UserRole> {

    public static final String MANAGE_CASE_TTL = "manage-case-ttl";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(MANAGE_CASE_TTL)
            .forStates(Draft, AwaitingPayment)
            .name("Resolve time to live")
            .showCondition(NEVER_SHOW)
            .description("Resolve time to live")
                .fields()
                .complex(CaseData::getRetainAndDisposeTimeToLive)
                .readonly(TTL::getSystemTTL)
                .optional(TTL::getOverrideTTL)
                .optional(TTL::getSuspended)
                .done()
            .done()
            .grant(CREATE_READ_UPDATE, TTL_PROFILE));
    }
}
