package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.payment.PaymentStatusService;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemFindCasesWithSuccessfulPaymentsTask implements Runnable {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private PaymentStatusService paymentStatusService;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    @Override
    public void run() {
        log.info("SystemFindCasesWithSuccessfulPaymentsTask scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query = boolQuery()
                .filter(matchQuery(STATE, AwaitingPayment));

            final List<CaseDetails> casesWithPaymentsInAwaitingFinalOrderState =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth, AwaitingPayment);

            log.info("{} cases in AwaitingPayment state", casesWithPaymentsInAwaitingFinalOrderState.size());

            final List<Long> caseIds = casesWithPaymentsInAwaitingFinalOrderState
                .stream()
                .filter(cd -> cd.getData().containsKey("applicationPayments"))
                .filter(cd -> hasSuccessFulPayment(user.getAuthToken(), serviceAuth, cd))
                .map(CaseDetails::getId)
                .toList();

            log.info("Found : " + caseIds);
            log.info("SystemFindCasesWithSuccessfulPaymentsTask scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("SystemFindCasesWithSuccessfulPaymentsTask schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemFindCasesWithSuccessfulPaymentsTask schedule task stopping due to conflict with another running task");
        }
    }

    private boolean hasSuccessFulPayment(String authToken, String serviceAuthorisation, CaseDetails cd) {
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails =
            caseDetailsConverter.convertToCaseDetailsFromReformModel(cd);

        final List<ListValue<Payment>> applicationPayments = caseDetails.getData().getApplication().getApplicationPayments();

        return applicationPayments
            .stream()
            .filter(ap -> ap.getValue().getStatus().equals(PaymentStatus.IN_PROGRESS))
            .map(ap -> ap.getValue().getReference())
            .map(r -> paymentStatusService.paymentSuccessful(authToken, serviceAuthorisation, r))
            .findFirst()
            .orElse(false);
    }
}
