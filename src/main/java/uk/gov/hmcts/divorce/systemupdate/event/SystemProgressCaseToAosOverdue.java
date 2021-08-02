package uk.gov.hmcts.divorce.systemupdate.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static java.util.EnumSet.of;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class SystemProgressCaseToAosOverdue implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_PROGRESS_TO_AOS_OVERDUE = "system-progress-to-aos-overdue";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SYSTEM_PROGRESS_TO_AOS_OVERDUE)
            .forStateTransition(of(AwaitingAos, AosDrafted), AosOverdue)
            .name("AoS not received within SLA")
            .description("AoS not received within SLA")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASEWORKER_SYSTEMUPDATE)
            .grant(READ, SOLICITOR, CASEWORKER_COURTADMIN_CTSC, CASEWORKER_COURTADMIN_RDU, CASEWORKER_SUPERUSER, CASEWORKER_LEGAL_ADVISOR));
    }
}
