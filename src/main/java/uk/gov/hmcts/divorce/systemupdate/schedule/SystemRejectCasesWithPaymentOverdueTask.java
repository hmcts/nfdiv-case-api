package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.service.task.UpdateSuccessfulPaymentStatus;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.payment.service.PaymentStatusService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.systemupdate.event.NonPaymentRejectCase.SYSTEM_REJECTED;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemRejectCasesWithPaymentOverdueTask implements Runnable {

    private static final String LAST_MODIFIED = "last_modified";

    private final CcdSearchService ccdSearchService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final PaymentStatusService paymentStatusService;

    private final CcdUpdateService ccdUpdateService;

    private final UpdateSuccessfulPaymentStatus updateSuccessfulPaymentStatus;

    @Override
    public void run() {
        log.info("SystemRejectCasesWithPaymentOverdueTask scheduled task started");

        final var user = idamService.retrieveSystemUpdateUserDetails();
        final var serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query = boolQuery()
                    .must(matchQuery(STATE, AwaitingPayment))
                    .filter(rangeQuery(LAST_MODIFIED)
                            .gte(LocalDate.now().minusDays(14)));

            final List<CaseDetails> casesInAwaitingPaymentStateForPaymentOverdue =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth, AwaitingPayment);

            casesInAwaitingPaymentStateForPaymentOverdue.forEach(caseDetails ->
                ccdUpdateService.submitEventWithRetry(caseDetails.getId().toString(), SYSTEM_REJECTED,
                    updateSuccessfulPaymentStatus, user, serviceAuth));

            log.info("SystemRejectCasesWithPaymentOverdueTask scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("SystemRejectCasesWithPaymentOverdueTask schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemRejectCasesWithPaymentOverdueTask schedule task stopping due to conflict with another running task");
        }
    }
}
