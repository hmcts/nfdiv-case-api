package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class CaseworkerPronounceList implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String CASEWORKER_PRONOUNCE_LIST = "caseworker-pronounce-list";

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new PageBuilder<>(configBuilder
            .event(CASEWORKER_PRONOUNCE_LIST)
            .forStateTransition(Listed, Pronounced)
            .name("Pronounce list")
            .description("Pronounce list")
            .showSummary()
            .submittedCallback(this::submitted)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grant(READ, SUPER_USER, LEGAL_ADVISOR, SOLICITOR, CITIZEN))
            .page("pronounceList", this::midEvent)
            .pageLabel("Pronounce List")
            .readonlyNoSummary(BulkActionCaseData::getPronouncementJudge)
            .mandatory(BulkActionCaseData::getHasJudgePronounced);
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> midEvent(
        final CaseDetails<BulkActionCaseData, BulkActionState> details,
        final CaseDetails<BulkActionCaseData, BulkActionState> detailsBefore) {

        log.info("Mid-event callback triggered for scheduleForPronouncement for Case ID: {}", details.getId());

        if (details.getData().getHasJudgePronounced() == NO) {
            return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
                .errors(singletonList("The judge must have pronounced to continue."))
                .build();
        }

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder().build();
    }

    public SubmittedCallbackResponse submitted(final CaseDetails<BulkActionCaseData, BulkActionState> details,
                                               final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails) {
        //TODO: Tom to add implementation
        return SubmittedCallbackResponse.builder().build();
    }
}
