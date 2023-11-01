package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.PaymentStatus;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemFindCasesWithSuccessfulPaymentsTask implements Runnable {

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

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    @Override
    public void run() {
        log.info("SystemFindCasesWithSuccessfulPaymentsTask scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            final BoolQueryBuilder query = boolQuery()
                .filter(matchQuery(STATE, AwaitingPayment));

            final List<CaseDetails> casesWithPaymentsInAwaitingFinalOrderState =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth, AwaitingPayment);

            final List<Map.Entry<Long, PaymentStatus>> applicationPayments = casesWithPaymentsInAwaitingFinalOrderState
                .stream()
                .filter(cd -> cd.getData().containsKey("applicationPayments"))
                .map(cd -> getPaymentStatus(user.getAuthToken(), serviceAuth, cd))
                .toList();

            log.info("SystemFindCasesWithSuccessfulPaymentsTask scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("SystemFindCasesWithSuccessfulPaymentsTask schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemFindCasesWithSuccessfulPaymentsTask schedule task stopping due to conflict with another running task"
            );
        }

    }

    private Map.Entry<Long, PaymentStatus> getPaymentStatus(String authToken, String serviceAuthorisation, CaseDetails cd) {
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails =
            caseDetailsConverter.convertToCaseDetailsFromReformModel(cd);
        final List<ListValue<Payment>> applicationPayments = caseDetails.getData().getApplication().getApplicationPayments();
        final Optional<ListValue<Payment>> inProgressPayment = applicationPayments
            .stream()
            .filter(ap -> ap.getValue().getStatus().equals(PaymentStatus.IN_PROGRESS))
            .findFirst();

        return Map.entry(cd.getId(), getPaymentStatusByReference(authToken, serviceAuthorisation, inProgressPayment));
    }

    private PaymentStatus getPaymentStatusByReference(String authToken, String serviceAuthorisation,
                                                      Optional<ListValue<Payment>> inProgressPayment) {
        log.info(inProgressPayment.toString());
        return paymentService.getPaymentStatusByReference(authToken, serviceAuthorisation,
            inProgressPayment.get().getValue().getReference());
    }
}
