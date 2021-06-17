package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import static java.util.EnumSet.allOf;
import static uk.gov.hmcts.divorce.common.model.State.Rejected;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;

@Component
public class CaseworkerRejected implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_REJECTED = "caseworker-rejected";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REJECTED)
            .forStateTransition(allOf(State.class), Rejected)
            .name("Rejected")
            .description("Rejected")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE,
                CASEWORKER_DIVORCE_COURTADMIN_BETA,
                CASEWORKER_DIVORCE_COURTADMIN)
            .grant(READ,
                CASEWORKER_DIVORCE_SOLICITOR,
                CASEWORKER_DIVORCE_SUPERUSER,
                CASEWORKER_DIVORCE_COURTADMIN_LA));
    }
}
