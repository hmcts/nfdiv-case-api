package uk.gov.hmcts.divorce.bulkaction.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.util.BulkCaseTaskUtil;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithPronouncementJudge.SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdatePronouncementJudgeDetailsTask implements BulkCaseTask {

    private final BulkCaseTaskUtil bulkCaseTaskUtil;

    private final AuthTokenGenerator authTokenGenerator;

    private final IdamService idamService;

    @Override
    public CaseDetails<BulkActionCaseData, BulkActionState> apply(final CaseDetails<BulkActionCaseData, BulkActionState> details) {
        return bulkCaseTaskUtil.processCases(
                details,
                details.getData().getBulkListCaseDetails(),
                SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE,
                idamService.retrieveSystemUpdateUserDetails(),
                authTokenGenerator.generate());
    }
}
