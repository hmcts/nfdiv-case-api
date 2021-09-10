package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static java.util.EnumSet.allOf;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PendingRejection;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class CaseworkerPendingRejection implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_PENDING_REJECTION = "caseworker-pending-rejection";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_PENDING_REJECTION)
            .forStateTransition(allOf(State.class), PendingRejection)
            .name("Pending rejection")
            .description("Pending rejection")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE,
                CASEWORKER_COURTADMIN)
            .grant(READ,
                SOLICITOR,
                CASEWORKER_SUPERUSER,
                CASEWORKER_LEGAL_ADVISOR));
    }
}
