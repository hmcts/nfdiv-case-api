package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAdminClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderReview;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class ConditionalOrderReviewCaseworker implements CCDConfig<CaseData, State, UserRole> {
    public static final String CONDITIONAL_ORDER_REVIEW_CASEWORKER = "conditional-order-review-caseworker";
    public static final String CONDITIONAL_ORDER_REVIEW_CASEWORKER_EVENT = "CO review caseworker";


    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
                .event(CONDITIONAL_ORDER_REVIEW_CASEWORKER)
                .forStateTransition(AwaitingAdminClarification, ConditionalOrderReview)
                .name(CONDITIONAL_ORDER_REVIEW_CASEWORKER_EVENT)
                .description(CONDITIONAL_ORDER_REVIEW_CASEWORKER_EVENT)
                .showEventNotes()
                .grant(CREATE_READ_UPDATE, CASE_WORKER, SUPER_USER)
                .grantHistoryOnly(LEGAL_ADVISOR, JUDGE, CITIZEN));
    }
}
