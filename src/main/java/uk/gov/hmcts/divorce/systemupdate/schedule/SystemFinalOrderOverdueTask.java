package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyFinalOrderOverdue.SYSTEM_FINAL_ORDER_OVERDUE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
/**
 * Any cases which are in 'Awaiting Final Order' state and where the current date is greater than 12 months after the
 * 'conditional order has been pronounced date' should be moved to the state 'Final Order Overdue'.
 * Conditional Order Granted Date is set to the Pronouncement Date in PronounceCaseProvider
 */
public class SystemFinalOrderOverdueTask implements Runnable {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    private static final String PRONOUNCED_DATE = "coGrantedDate";

    @Override
    public void run() {
        log.info("Final Order overdue scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, AwaitingFinalOrder));

            final List<CaseDetails> casesInAwaitingFinalOrderState =
                ccdSearchService.searchForAllCasesWithQuery(AwaitingFinalOrder, query, user, serviceAuth);

            for (final CaseDetails caseDetails : casesInAwaitingFinalOrderState) {
                try {
                    Map<String, Object> caseDataMap = caseDetails.getData();

                    String pronouncedDate = (String) caseDataMap.getOrDefault(PRONOUNCED_DATE, null);
                    log.info("pronouncedDate is {} from caseDataMap for case id {}", pronouncedDate, caseDetails.getId());

                    if (pronouncedDate == null) {
                        log.error("Ignoring case id {} with created on {} and modified on {}, as pronounced date is null",
                            caseDetails.getId(),
                            caseDetails.getCreatedDate(),
                            caseDetails.getLastModified()
                        );
                    } else {
                        LocalDate finalOrderOverdueDate = LocalDate.parse(pronouncedDate).plusMonths(12);

                        log.info("Final Order Overdue Date {} Case {}", finalOrderOverdueDate, caseDetails.getId());

                        if (finalOrderOverdueDate.isBefore(LocalDate.now())) {
                            log.info("Submitting Final Order Overdue Event for Case {}", caseDetails.getId());
                            ccdUpdateService.submitEvent(caseDetails, SYSTEM_FINAL_ORDER_OVERDUE, user, serviceAuth);
                        }
                    }
                } catch (final CcdManagementException e) {
                    log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                } catch (final IllegalArgumentException e) {
                    log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

            log.info("Final Order overdue scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Final Order overdue schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Final Order overdue schedule task stopping "
                + "due to conflict with another running task"
            );
        }
    }
}
