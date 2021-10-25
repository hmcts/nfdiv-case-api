package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.bulkaction.service.ListValueUtil;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.solicitor.service.task.PronounceCase;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCaseErrors.SYSTEM_BULK_CASE_ERRORS;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;

@Component
@Slf4j
public class CaseworkerPronounceList implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String CASEWORKER_PRONOUNCE_LIST = "caseworker-pronounce-list";

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private BulkTriggerService bulkTriggerService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private PronounceCase pronounceCase;

    @Autowired
    private ListValueUtil listValueUtil;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_PRONOUNCE_LIST)
            .forStateTransition(Listed, Pronounced)
            .name("Pronounce list")
            .description("Pronounce list")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
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

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToSubmit(
        CaseDetails<BulkActionCaseData, BulkActionState> details,
        CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {

        final BulkActionCaseData caseData = details.getData();

        caseData.setPronouncedDate(caseData.getDateAndTimeOfHearing().toLocalDate());
        caseData.setDateFinalOrderEligibleFrom(caseData.getDateAndTimeOfHearing().toLocalDate().plusWeeks(6).plusDays(1));

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
            .data(caseData)
            .state(Pronounced)
            .build();
    }

    public SubmittedCallbackResponse submitted(
        CaseDetails<BulkActionCaseData, BulkActionState> details,
        CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {
        processBulkCases(details);
        return SubmittedCallbackResponse.builder().build();
    }

    @Async
    private void processBulkCases(final CaseDetails<BulkActionCaseData, BulkActionState> details) {
        final BulkActionCaseData caseData = details.getData();
        final List<BulkListCaseDetails> unprocessedBulkCases =
            bulkTriggerService.bulkTrigger(
                listValueUtil.fromListValueToList(caseData.getBulkListCaseDetails()),
                SYSTEM_PRONOUNCE_CASE,
                pronounceCase,
                idamService.retrieveUser(request.getHeader(AUTHORIZATION)),
                authTokenGenerator.generate());

        caseData.getErroredCaseDetails().addAll(listValueUtil.fromListToListValue(unprocessedBulkCases));

        try {
            ccdUpdateService.submitBulkActionEvent(
                details,
                SYSTEM_BULK_CASE_ERRORS,
                idamService.retrieveUser(request.getHeader(AUTHORIZATION)),
                authTokenGenerator.generate()
            );
        } catch (final CcdManagementException e) {
            log.error("Update failed for bulk case id {} ", details.getId());
        }
    }
}
