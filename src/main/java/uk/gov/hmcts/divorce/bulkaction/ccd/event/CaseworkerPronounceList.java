package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.CasePronouncementService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerPronounceList implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String CASEWORKER_PRONOUNCE_LIST = "caseworker-pronounce-list";

    @Autowired
    private CasePronouncementService casePronouncementService;

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_PRONOUNCE_LIST)
            .forStateTransition(Listed, Pronounced)
            .name("Pronounce list")
            .description("Pronounce list")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, SOLICITOR, CITIZEN, JUDGE))
            .page("pronounceList", this::midEvent)
            .pageLabel("Pronounce List")
            .readonlyNoSummary(BulkActionCaseData::getPronouncementJudge)
            .mandatory(BulkActionCaseData::getHasJudgePronounced);
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> midEvent(
        final CaseDetails<BulkActionCaseData, BulkActionState> details,
        final CaseDetails<BulkActionCaseData, BulkActionState> detailsBefore) {

        log.info("{} mid event callback invoked for Case Id: {}", CASEWORKER_PRONOUNCE_LIST, details.getId());

        if (details.getData().getHasJudgePronounced() == NO) {
            return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
                .errors(singletonList("The judge must have pronounced to continue."))
                .build();
        }

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder().build();
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToSubmit(
        final CaseDetails<BulkActionCaseData, BulkActionState> details,
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {

        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_PRONOUNCE_LIST, details.getId());

        final BulkActionCaseData caseData = details.getData();
        final LocalDateTime dateAndTimeOfHearing = caseData.getDateAndTimeOfHearing();

        if (now(clock).isBefore(dateAndTimeOfHearing)) {
            return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
                .data(caseData)
                .errors(List.of("You cannot pronounce a case before the hearing date"))
                .build();
        }

        caseData.setPronouncedDate(dateAndTimeOfHearing.toLocalDate());
        caseData.setDateFinalOrderEligibleFrom(caseData.getDateFinalOrderEligibleFrom(dateAndTimeOfHearing));

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(
        CaseDetails<BulkActionCaseData, BulkActionState> details,
        CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {

        log.info("{} submitted callback invoked for Case Id: {}", CASEWORKER_PRONOUNCE_LIST, details.getId());
        casePronouncementService.pronounceCases(details);
        return SubmittedCallbackResponse.builder().build();
    }
}
