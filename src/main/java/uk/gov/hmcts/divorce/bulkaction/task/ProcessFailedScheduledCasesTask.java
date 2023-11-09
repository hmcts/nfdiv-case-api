package uk.gov.hmcts.divorce.bulkaction.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.util.BulkCaseTaskUtil;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static uk.gov.hmcts.divorce.bulkaction.service.BulkCaseProcessingService.getFailedBulkCases;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;

@Component
@Slf4j
public class ProcessFailedScheduledCasesTask implements BulkCaseTask {

    @Autowired
    protected IdamService idamService;

    @Autowired
    protected AuthTokenGenerator authTokenGenerator;

    @Autowired
    private BulkCaseTaskUtil bulkCaseTaskUtil;

    @Override
    public CaseDetails<BulkActionCaseData, BulkActionState> apply(final CaseDetails<BulkActionCaseData, BulkActionState> details) {
        return bulkCaseTaskUtil.processCases(
                details,
                getFailedBulkCases(details),
                SYSTEM_UPDATE_CASE_COURT_HEARING,
                idamService.retrieveSystemUpdateUserDetails(),
                authTokenGenerator.generate());
    }
}
