package uk.gov.hmcts.divorce.bulkaction.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.bulkaction.service.BulkCaseProcessingService.getFailedBulkCases;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;

@Component
@Slf4j
public class ProcessFailedScheduledCasesTask implements BulkCaseTask {

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

    @Override
    public CaseDetails<BulkActionCaseData, BulkActionState> apply(final CaseDetails<BulkActionCaseData, BulkActionState> details) {

        final Long bulkCaseId = details.getId();
        final BulkActionCaseData bulkActionCaseData = details.getData();

        final User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        final String serviceAuth = authTokenGenerator.generate();

        final List<ListValue<BulkListCaseDetails>> unprocessedCases = bulkTriggerService.bulkTrigger(
            getFailedBulkCases(details),
            SYSTEM_UPDATE_CASE_COURT_HEARING,
            bulkCaseCaseTaskFactory.getCaseTask(details, SYSTEM_UPDATE_CASE_COURT_HEARING),
            user,
            serviceAuth
        );

        log.info("Error bulk case details list size {} for case id {} ", unprocessedCases.size(), bulkCaseId);

        final List<ListValue<BulkListCaseDetails>> processedCases = bulkActionCaseData.calculateProcessedCases(unprocessedCases);

        log.info("Successfully processed bulk case details list size {} for case id {}", processedCases.size(), bulkCaseId);

        bulkActionCaseData.setErroredCaseDetails(unprocessedCases);
        bulkActionCaseData.setProcessedCaseDetails(processedCases);

        return details;
    }
}
