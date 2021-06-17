package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import static uk.gov.hmcts.divorce.common.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;

@Slf4j
@Component
public class CaseworkerAwaitingApplicantFromAwaitingPayment implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_AWAITING_DOC_FROM_AWAITING_PAYMENT = "caseworker-awaiting-doc-from-awaiting-payment";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_AWAITING_DOC_FROM_AWAITING_PAYMENT)
            .forStateTransition(AwaitingPayment, AwaitingDocuments)
            .name("Awaiting applicant")
            .description("Awaiting applicant")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASEWORKER_DIVORCE_COURTADMIN_BETA, CASEWORKER_DIVORCE_COURTADMIN)
            .grant(READ, CASEWORKER_DIVORCE_SOLICITOR, CASEWORKER_DIVORCE_SUPERUSER, CASEWORKER_DIVORCE_COURTADMIN_LA))
            .page("caseworkerAwaitingDocuments")
            .pageLabel("Update Due Date")
            .optional(CaseData::getDueDate);
    }
}
