package uk.gov.hmcts.divorce.legaladvisor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class LegalAdvisorGrantConditionalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String LEGAL_ADVISOR_GRANT_CONDITIONAL_ORDER = "legal-advisor-grant-conditional-order";

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(LEGAL_ADVISOR_GRANT_CONDITIONAL_ORDER)
            .forState(AwaitingLegalAdvisorReferral)
            .name("Make a decision")
            .description("Grant Conditional Order")
            .endButtonLabel("Submit")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, LEGAL_ADVISOR)
            .grant(READ,
                CASE_WORKER,
                SUPER_USER,
                APPLICANT_1_SOLICITOR))
            .page("grantConditionalOrder")
            .pageLabel("Grant Conditional Order")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getGranted)
                .mandatory(ConditionalOrder::getClaimsGranted, "coGranted=\"Yes\"")
                .done()
            .page("conditionalOrderMakeCostsOrder")
            .pageLabel("Make a costs order")
            .showCondition("coClaimsGranted=\"Yes\"")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getClaimsCostsOrderInformation)
                .done()
            .page("makeRefusalOrder")
            .pageLabel("Make a refusal order")
            .showCondition("coGranted=\"No\"")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getRefusalDecision)
            .done()
            .page("refusalOrderClarification")
            .pageLabel("Refusal Order:Clarify - Make a Decision")
            .showCondition("coRefusalDecision=\"moreInfo\"")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getRefusalClarificationReason)
                .mandatory(ConditionalOrder::getRefusalClarificationAdditionalInfo)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Legal advisor grant conditional order about to submit callback invoked. CaseID: {}", details.getId());

        final ConditionalOrder conditionalOrder = details.getData().getConditionalOrder();

        State endState;

        if (conditionalOrder.hasConditionalOrderBeenGranted()) {
            log.info("Legal advisor conditional order granted for case id: {}", details.getId());
            conditionalOrder.setDecisionDate(LocalDate.now(clock));
            endState = AwaitingPronouncement;
        } else {
            endState = AwaitingClarification;
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(endState)
            .build();
    }
}
