package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.time.LocalDate;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressHeldCase.SYSTEM_PROGRESS_HELD_CASE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
/**
 * Any cases that were issued >= 20 weeks ago AND are in the Holding state will be moved to AwaitingConditionalOrder by this task.
 */
public class SystemProgressHeldCasesTask implements Runnable {

    @Autowired
    private HoldingPeriodService holdingPeriodService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    private static final String DUE_DATE = "dueDate";

    @Override
    public void run() {
        log.info("SystemProgressHeldCasesTask scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query = boolQuery()
                .must(matchQuery(STATE, Holding))
                .filter(rangeQuery(CcdSearchService.DUE_DATE).lte(LocalDate.now()));

            ccdSearchService
                .searchForAllCasesWithQuery(Holding, query, user, serviceAuth)
                .forEach(caseDetails -> submitEvent(caseDetails, user, serviceAuth));

            log.info("SystemProgressHeldCasesTask scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("SystemProgressHeldCasesTask schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemProgressHeldCasesTask schedule task stopping due to conflict with another running task"
            );
        }
    }

    private void submitEvent(CaseDetails caseDetails, User user, String serviceAuth) {
        try {
            log.info("Case id {} has been in holding state for > {} weeks hence moving state to AwaitingConditionalOrder",
                caseDetails.getId(), holdingPeriodService.getHoldingPeriodInDays());
            caseDetails.getData().put(DUE_DATE, null);
            ccdUpdateService.submitEvent(caseDetails, SYSTEM_PROGRESS_HELD_CASE, user, serviceAuth);
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
        }
    }
}
