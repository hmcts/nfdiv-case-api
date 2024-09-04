package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.CasePronouncementService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerRetryPronounceList implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String CASEWORKER_RETRY_PRONOUNCE_LIST = "caseworker-retry-pronounce-list";
    private static final String RETRY_PRONOUNCE_LIST = "Retry Pronounce list";

    @Autowired
    private CasePronouncementService casePronouncementService;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_RETRY_PRONOUNCE_LIST)
            .forStates(Pronounced)
            .name(RETRY_PRONOUNCE_LIST)
            .description(RETRY_PRONOUNCE_LIST)
            .showSummary()
            .showEventNotes()
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, SUPER_USER)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, CITIZEN))
            .page("retryPronounceList")
            .pageLabel(RETRY_PRONOUNCE_LIST)
            .mandatory(BulkActionCaseData::getPronouncementJudge, null, "District Judge");
    }

    public SubmittedCallbackResponse submitted(
        CaseDetails<BulkActionCaseData, BulkActionState> details,
        CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {
        log.info("{} Retry pronounce list submitted callback invoked for Case Id: {}", CASEWORKER_RETRY_PRONOUNCE_LIST, details.getId());
        casePronouncementService.retryPronounceCases(details);
        return SubmittedCallbackResponse.builder().build();
    }
}
