package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import static uk.gov.hmcts.divorce.common.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.common.model.State.Submitted;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;

@Component
public class CaseworkerHwfApplicationAcceptedFromAwaitingPayment implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_HWF_APPLICATION_ACCEPTED_FROM_AWAITING_PAYMENT
        = "caseworker-hwf-application-accepted-from-awaiting-payment";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_HWF_APPLICATION_ACCEPTED_FROM_AWAITING_PAYMENT)
            .forStateTransition(AwaitingPayment, Submitted)
            .name("HWF application accepted")
            .description("HWF application accepted")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASEWORKER_COURTADMIN_CTSC, CASEWORKER_COURTADMIN_RDU)
            .grant(READ, SOLICITOR, CASEWORKER_SUPERUSER, CASEWORKER_LEGAL_ADVISOR));
    }
}
