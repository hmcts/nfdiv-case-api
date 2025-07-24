package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.payment.service.PaymentStatusService;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRejectCasesWithPaymentOverdue.APPLICATION_REJECTED_FEE_NOT_PAID;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.ISSUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.SUPPLEMENTARY_CASE_TYPE;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemRejectCasesWithPaymentOverdueTask implements Runnable {

    private static final String LAST_MODIFIED = "last_modified";
    private static final String NEW_PAPER_CASE = "newPaperCase";
    private final CcdSearchService ccdSearchService;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CcdUpdateService ccdUpdateService;
    private final PaymentStatusService paymentStatusService;
    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    public void run() {
        log.info("SystemRejectCasesWithPaymentOverdueTask scheduled task started");

        final var user = idamService.retrieveSystemUpdateUserDetails();
        final var serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder paperOrJudicialSeparationCases = boolQuery()
                .should(matchQuery(String.format(DATA, SUPPLEMENTARY_CASE_TYPE), "judicialSeparation"))
                .should(matchQuery(String.format(DATA, SUPPLEMENTARY_CASE_TYPE), "separation"))
                .should(matchQuery(String.format(DATA, NEW_PAPER_CASE), "Yes"))
                .filter(existsQuery(ISSUE_DATE))
                .minimumShouldMatch(1);

            final MatchQueryBuilder awaitingPaymentQuery = matchQuery(STATE, AwaitingPayment);

            final BoolQueryBuilder query = boolQuery()
                .should(
                    boolQuery()
                        .must(awaitingPaymentQuery)
                        .mustNot(paperOrJudicialSeparationCases)
                        .filter(rangeQuery(LAST_MODIFIED).lte(LocalDate.now().minusDays(14)))
                )
                .should(
                    boolQuery()
                        .must(awaitingPaymentQuery)
                        .must(paperOrJudicialSeparationCases)
                        .filter(rangeQuery(LAST_MODIFIED).lte(LocalDate.now().minusDays(17)))
                )
                .minimumShouldMatch(1);

            final List<CaseDetails> casesInAwaitingPaymentStateForPaymentOverdue =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth, AwaitingPayment);

            casesInAwaitingPaymentStateForPaymentOverdue.stream()
                .map(caseDetailsConverter::convertToCaseDetailsFromReformModel)
                .filter(caseDetails ->
                    !paymentStatusService.hasSuccessfulPayment(caseDetails, user.getAuthToken(), serviceAuth)).toList()
                .forEach(caseDetails -> {
                    log.info("Rejecting case {} due to fee not paid within allocated time", caseDetails.getId());
                    ccdUpdateService.submitEvent(caseDetails.getId(), APPLICATION_REJECTED_FEE_NOT_PAID, user, serviceAuth);
                    });

            log.info("SystemRejectCasesWithPaymentOverdueTask scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("SystemRejectCasesWithPaymentOverdueTask schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemRejectCasesWithPaymentOverdueTask schedule task stopping due to conflict with another running task");
        }
    }
}
