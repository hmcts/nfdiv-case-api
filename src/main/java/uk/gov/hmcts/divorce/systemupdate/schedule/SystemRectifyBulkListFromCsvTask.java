package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemRectifyBulkList.SYSTEM_RECTIFY_BULK_LIST;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemRectifyBulkListFromCsvTask implements Runnable {

    public static final String CSV_FILE = "rectify-bulk.csv";
    public static final String SEARCH_ERROR   = "SystemRectifyBulkListFromCsvTask stopped after search error";
    public static final String CONFLICT_ERROR = "SystemRectifyBulkListFromCsvTask stopping due to conflict";
    public static final String FILE_READ_ERROR = "SystemRectifyBulkListFromCsvTask stopped after file read error";
    public static final String SUBMIT_EVENT_ERROR = "Submit event failed for bulk id: {}, continuing to next bulk";

    private final CcdUpdateService ccdUpdateService;
    private final CcdSearchService ccdSearchService;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final TaskHelper taskHelper;

    @Override
    public void run() {
        log.info("SystemRectifyBulkListFromCsvTask started");
        try {
            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuth = authTokenGenerator.generate();

            // Read CSV: one line per bulk → (bulkRef, [caseRefs...])
            final List<TaskHelper.BulkRectifySpec> specs = taskHelper.loadRectifyBatches(CSV_FILE);
            final List<Long> bulkIds = specs.stream().map(TaskHelper.BulkRectifySpec::bulkRef).distinct().toList();

            if (bulkIds.isEmpty()) {
                log.info("No bulk IDs in CSV '{}'; nothing to do.", CSV_FILE);
                return;
            }

            final BoolQueryBuilder query = boolQuery()
                .filter(QueryBuilders.termsQuery("reference", bulkIds));

            final List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState>>
                bulkCases = ccdSearchService.searchForBulkCases(user, serviceAuth, query);

            for (uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> bulkCase : bulkCases) {
                submitRectifyBulkList(bulkCase.getId(), user, serviceAuth);
            }
            log.info("SystemRectifyBulkListFromCsvTask completed.");
        } catch (Exception e) {
            taskHelper.logError("SystemRectifyBulkListFromCsvTask stopped due to exception", null, e);
        }
    }

    private void submitRectifyBulkList(Long caseId, User user, String serviceAuth) {
        try {
            log.info("Submitting {} for Bulk {}", SYSTEM_RECTIFY_BULK_LIST, caseId);
            // No payload needed: the event itself reads CSV and removes the right cases for THIS bulk
            ccdUpdateService.submitEvent(
                caseId,
                SYSTEM_RECTIFY_BULK_LIST,
                user,
                serviceAuth
            );
        } catch (final CcdManagementException | IllegalArgumentException e) {
            taskHelper.logError(SUBMIT_EVENT_ERROR, caseId, e);
        }
    }
}
