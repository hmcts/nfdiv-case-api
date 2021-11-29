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
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressCaseToAwaitingFinalOrder.SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
/**
 * Any cases which are in Conditional Order Pronounced state and whose
 * final order eligible from date <= current date will be moved to AwaitingFinalOrder by this task.
 */
public class SystemProgressCasesToAwaitingFinalOrderTask implements Runnable {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public static final String DATA_DATE_FINAL_ORDER_ELIGIBLE_FROM = "data.dateFinalOrderEligibleFrom";
    public static final String DATE_FINAL_ORDER_ELIGIBLE_FROM = "dateFinalOrderEligibleFrom";

    @Override
    public void run() {
        log.info("System progress cases to Awaiting Final Order scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, ConditionalOrderPronounced))
                    .filter(rangeQuery(DATA_DATE_FINAL_ORDER_ELIGIBLE_FROM).lte(LocalDate.now()));

            final List<CaseDetails> casesInConditionalOrderPronouncedState =
                ccdSearchService.searchForAllCasesWithQuery(ConditionalOrderPronounced, query, user, serviceAuth);

            for (final CaseDetails caseDetails : casesInConditionalOrderPronouncedState) {
                try {
                    Map<String, Object> caseDataMap = caseDetails.getData();
                    String dateFinalOrderEligibleFrom = (String) caseDataMap.getOrDefault(DATE_FINAL_ORDER_ELIGIBLE_FROM, null);

                    if (dateFinalOrderEligibleFrom != null) {
                        LocalDate parsedDateFinalOrderEligibleFrom = LocalDate.parse(dateFinalOrderEligibleFrom);

                        if (!parsedDateFinalOrderEligibleFrom.isAfter(LocalDate.now())) {
                            ccdUpdateService.submitEvent(caseDetails, SYSTEM_PROGRESS_CASE_TO_AWAITING_FINAL_ORDER, user, serviceAuth);
                        }
                    }
                } catch (final CcdManagementException e) {
                    log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                } catch (final IllegalArgumentException e) {
                    log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

            log.info("System progress cases to Awaiting Final Order scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("System progress cases to Awaiting Final Order scheduled task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("System progress cases to Awaiting Final Order scheduled task stopping "
                + "due to conflict with another running Awaiting Final Order task"
            );
        }
    }
}
