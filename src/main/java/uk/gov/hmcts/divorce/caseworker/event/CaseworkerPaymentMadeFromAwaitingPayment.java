package uk.gov.hmcts.divorce.caseworker.event;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import static uk.gov.hmcts.divorce.common.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.common.model.State.Submitted;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;

public class CaseworkerPaymentMadeFromAwaitingPayment implements CCDConfig<CaseData, State, UserRole> {

    public static final String PAYMENT_MADE_FROM_AWAITING_PAYMENT = "payment-made-from-awaiting-payment";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(PAYMENT_MADE_FROM_AWAITING_PAYMENT)
            .forStateTransition(AwaitingPayment, Submitted)
            .name("Payment made")
            .description("Payment made")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASEWORKER_DIVORCE_COURTADMIN_BETA, CASEWORKER_DIVORCE_COURTADMIN, CITIZEN)
            .grant(READ, CASEWORKER_DIVORCE_SUPERUSER, CASEWORKER_DIVORCE_COURTADMIN_LA));
    }
}
