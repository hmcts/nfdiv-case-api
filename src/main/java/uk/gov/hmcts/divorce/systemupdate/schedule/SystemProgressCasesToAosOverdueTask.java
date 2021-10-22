package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.QueryConstants;
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
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.common.config.QueryConstants.STATE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressCaseToAosOverdue.SYSTEM_PROGRESS_TO_AOS_OVERDUE;

@Component
@Slf4j
/**
 * Any cases which are in AwaitingAos or AosDrafted state and whose due date >= current date will be moved to AosOverdue by this task.
 */
public class SystemProgressCasesToAosOverdueTask implements Runnable {

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
        log.info("Aos overdue scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, AwaitingAos))
                    .filter(rangeQuery(QueryConstants.DUE_DATE).lte(LocalDate.now()));

            final List<CaseDetails> casesInAwaitingAosState =
                ccdSearchService.searchForAllCasesWithQuery(AwaitingAos, query, user, serviceAuth);

            for (final CaseDetails caseDetails : casesInAwaitingAosState) {
                try {
                    Map<String, Object> caseDataMap = caseDetails.getData();
                    String dueDate = (String) caseDataMap.getOrDefault(DUE_DATE, null);
                    log.info("dueDate is {} from caseDataMap for case id {}", dueDate, caseDetails.getId());

                    if (dueDate == null) {
                        log.error("Ignoring case id {} with created on {} and modified on {}, as due date is null",
                            caseDetails.getId(),
                            caseDetails.getCreatedDate(),
                            caseDetails.getLastModified()
                        );
                    } else {
                        LocalDate aosDueDate = LocalDate.parse(dueDate);

                        if (aosDueDate.isEqual(LocalDate.now()) || aosDueDate.isBefore(LocalDate.now())) {
                            log.info("Due date {} for Case id {} is on/before current date hence moving state to AosOverdue",
                                aosDueDate,
                                caseDetails.getId()
                            );
                            ccdUpdateService.submitEvent(caseDetails, SYSTEM_PROGRESS_TO_AOS_OVERDUE, user, serviceAuth);
                        }
                    }
                } catch (final CcdManagementException e) {
                    log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                } catch (final IllegalArgumentException e) {
                    log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

            log.info("Aos overdue scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Aos overdue schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Aos overdue schedule task stopping "
                + "due to conflict with another running awaiting aos task"
            );
        }
    }
}
