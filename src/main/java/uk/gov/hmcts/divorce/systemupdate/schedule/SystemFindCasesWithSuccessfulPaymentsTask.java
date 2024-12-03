package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.payment.PaymentStatusService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemFindCasesWithSuccessfulPaymentsTask implements Runnable {

    private static final String LAST_MODIFIED = "last_modified";

    private final CcdSearchService ccdSearchService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final PaymentStatusService paymentStatusService;

    @Override
    public void run() {
        log.info("SystemFindCasesWithSuccessfulPaymentsTask scheduled task started");

        final var user = idamService.retrieveSystemUpdateUserDetails();
        final var serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query = boolQuery()
                    .should(matchQuery(STATE, AwaitingPayment))
                    .should(matchQuery(STATE, AwaitingFinalOrderPayment))
                    .minimumShouldMatch(1)
                    .filter(rangeQuery(LAST_MODIFIED)
                            .gte(LocalDate.now().minusDays(1)));

            final List<CaseDetails> casesInAwaitingPaymentState =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth, AwaitingPayment, AwaitingFinalOrderPayment);

            paymentStatusService.hasSuccessFulPayment(casesInAwaitingPaymentState);

            log.info("SystemFindCasesWithSuccessfulPaymentsTask scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("SystemFindCasesWithSuccessfulPaymentsTask schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemFindCasesWithSuccessfulPaymentsTask schedule task stopping due to conflict with another running task");
        }
    }
}
