package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
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

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerUpdateFinalOrderFields implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_UPDATE_FINAL_ORDER_FIELDS = "caseworker-update-final-order-fields";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPDATE_FINAL_ORDER_FIELDS)
            .forStates(ConditionalOrderPronounced)
            .name("Update FO fields")
            .description("Update FO fields")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, SUPER_USER)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR))
            .page("updateFinalOrderFields")
            .pageLabel("Update FO fields")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getDateAndTimeOfHearing)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        final Long caseId = details.getId();
        CaseData caseData = details.getData();

        log.info("Caseworker update FO fields about to submit callback invoked for case id: {}", caseId);

        final var conditionalOrder = caseData.getConditionalOrder();
        final var finalOrder = caseData.getFinalOrder();

        final LocalDate dateFinalOrderEligibleFrom = finalOrder.getDateFinalOrderEligibleFrom(conditionalOrder.getDateAndTimeOfHearing());

        caseData.setDueDate(dateFinalOrderEligibleFrom);
        conditionalOrder.setOutcomeCase(YES);
        conditionalOrder.setGrantedDate(conditionalOrder.getDateAndTimeOfHearing().toLocalDate());
        finalOrder.setDateFinalOrderEligibleFrom(dateFinalOrderEligibleFrom);
        finalOrder.setDateFinalOrderNoLongerEligible(
            finalOrder.calculateDateFinalOrderNoLongerEligible(conditionalOrder.getGrantedDate()));
        finalOrder.setDateFinalOrderEligibleToRespondent(
            finalOrder.calculateDateFinalOrderEligibleToRespondent());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
