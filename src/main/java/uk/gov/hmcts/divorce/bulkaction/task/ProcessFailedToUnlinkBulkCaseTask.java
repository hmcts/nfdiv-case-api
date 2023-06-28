package uk.gov.hmcts.divorce.bulkaction.task;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.bulkaction.util.BulkCaseTaskUtil;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.bulkaction.service.BulkCaseProcessingService.getFailedBulkCases;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemoveBulkCase.SYSTEM_REMOVE_BULK_CASE;

@Component
@Slf4j
public class ProcessFailedToUnlinkBulkCaseTask implements BulkCaseTask {

    @Autowired
    private BulkTriggerService bulkTriggerService;

    @Autowired
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamService idamService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private BulkCaseTaskUtil bulkCaseTaskUtil;

    @Override
    public CaseDetails<BulkActionCaseData, BulkActionState> apply(final CaseDetails<BulkActionCaseData, BulkActionState> details) {
        return bulkCaseTaskUtil.processCases(
                details,
                getFailedBulkCases(details),
                SYSTEM_REMOVE_BULK_CASE,
                idamService.retrieveUser(request.getHeader(AUTHORIZATION)),
                authTokenGenerator.generate());
    }
}
