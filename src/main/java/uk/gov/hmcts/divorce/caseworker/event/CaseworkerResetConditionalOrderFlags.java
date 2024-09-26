package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.task.ResetConditionalOrderFlags;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Collections;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseworkerResetConditionalOrderFlags implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_RESET_CONDITIONAL_ORDER = "caseworker-reset-conditional-order-flags";

    private static final String RESET_CONDITIONAL_ORDER = "Reset conditional order flags";

    private static final String DESCRIPTION_RESET_CONDITIONAL_ORDER_FLAGS = """
        This event will reset the conditional order drafted and submitted flags, making the draft conditional order event
        available again for the case to allow the conditional order to be drafted a second time.
        """;

    public static final String WARNING_RESET_CONDITIONAL_ORDER_FLAGS = """
        ### WARNING: Only continue if you are certain that the conditional order must be drafted again on this case.
        """;

    private final ResetConditionalOrderFlags resetConditionalOrderFlags;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(CASEWORKER_RESET_CONDITIONAL_ORDER)
            .forState(AwaitingConditionalOrder)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .name(RESET_CONDITIONAL_ORDER)
            .description(RESET_CONDITIONAL_ORDER)
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, SUPER_USER)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, JUDGE))
            .page(RESET_CONDITIONAL_ORDER)
            .pageLabel(RESET_CONDITIONAL_ORDER)
            .label("resetDescription", DESCRIPTION_RESET_CONDITIONAL_ORDER_FLAGS)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for case Id: {}", CASEWORKER_RESET_CONDITIONAL_ORDER, details.getId());

        final CaseDetails<CaseData, State> updatedCaseDetails = resetConditionalOrderFlags.apply(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updatedCaseDetails.getData())
            .warnings(Collections.singletonList(WARNING_RESET_CONDITIONAL_ORDER_FLAGS))
            .build();
    }
}
