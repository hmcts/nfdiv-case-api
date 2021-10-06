package uk.gov.hmcts.divorce.legaladvisor.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class LegalAdvisorGrantConditionalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String LEGAL_ADVISOR_GRANT_CONDITIONAL_ORDER = "legal-advisor-grant-conditional-order";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(LEGAL_ADVISOR_GRANT_CONDITIONAL_ORDER)
            .forStateTransition(AwaitingLegalAdvisorReferral, AwaitingPronouncement)
            .name("Make Decision")
            .description("Grant Conditional Order")
            .endButtonLabel("Submit")
            .showSummary()
            .showEventNotes()
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, LEGAL_ADVISOR)
            .grant(READ,
                CASE_WORKER,
                SUPER_USER,
                APPLICANT_1_SOLICITOR))
            .page("GrantConditionalOrder")
            .pageLabel("Grant Conditional Order")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getGranted)
                .mandatory(ConditionalOrder::getClaimsGranted, "coGranted=\"Yes\"")
                .done()
            .page("ConditionalOrderMakeCostsOrder")
            .pageLabel("Make a costs order")
            .showCondition("coClaimsGranted=\"Yes\"")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getClaimsCostsOrderInformation)
                .done();
    }
}
