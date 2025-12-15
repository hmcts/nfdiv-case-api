package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.payment.client.PaymentClient;
import uk.gov.hmcts.divorce.payment.client.PaymentsResponse;
import uk.gov.hmcts.divorce.payment.model.ServiceRequestDto;
import uk.gov.hmcts.divorce.payment.service.PaymentStatusService;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static org.apache.commons.lang3.BooleanUtils.forEach;
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

    private static final int MAX_CASE_AGE_DAYS = 16;
    private static final int GRACE_PERIOD_HOURS = 24;
    private static final String SUCCESS_STATUS = "success";

    private static final String LAST_STATE_MODIFIED_DATE = "last_state_modified_date";
    private static final String NEW_PAPER_CASE = "newPaperCase";
    private final CcdSearchService ccdSearchService;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CcdUpdateService ccdUpdateService;
    private final PaymentStatusService paymentStatusService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final PaymentClient paymentClient;

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
                .minimumShouldMatch(1);

            final MatchQueryBuilder awaitingPaymentQuery = matchQuery(STATE, AwaitingPayment);

            final BoolQueryBuilder query = boolQuery()
                .must(awaitingPaymentQuery)
                .mustNot(paperOrJudicialSeparationCases)
                .mustNot(existsQuery(ISSUE_DATE))
                .filter(rangeQuery(LAST_STATE_MODIFIED_DATE).lte(LocalDate.now().minusDays(14)));

            final List<CaseDetails> casesInAwaitingPaymentStateForPaymentOverdue =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth, AwaitingPayment);

            casesInAwaitingPaymentStateForPaymentOverdue.stream()
                .map(caseDetailsConverter::convertToCaseDetailsFromReformModel)
                .forEach(caseDetails -> {
                    processPaymentRejection(caseDetails, user, serviceAuth);
                });

            log.info("SystemRejectCasesWithPaymentOverdueTask scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("SystemRejectCasesWithPaymentOverdueTask schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemRejectCasesWithPaymentOverdueTask schedule task stopping due to conflict with another running task");
        }
    }

    private void processPaymentRejection(uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails, User user, String serviceAuth) {
        ServiceRequestDto latestServiceRequest = getLatestServiceRequest(caseDetails, user, serviceAuth);

        // Hard rejection after 15 days - no exceptions
        if (isCaseOverAgeLimit(caseDetails)) {
            rejectCase(caseDetails, "case has exceeded the 15-day limit", user, serviceAuth);
            return;
        }

        // Within 15-day window: check grace period and payment status
        if (isServiceRequestWithinGracePeriod(latestServiceRequest)) {
            log.info("Skipping case {} - service request created within last {} hours",
                caseDetails.getId(), GRACE_PERIOD_HOURS);
        } else if (!hasSuccessfulPayment(latestServiceRequest)) {
            rejectCase(caseDetails, "payment not made after creating service request", user, serviceAuth);
        }
    }

    private ServiceRequestDto getLatestServiceRequest(uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails, User user, String serviceAuth) {
        var paymentClientResponse = paymentClient.getServiceRequests(
            user.getAuthToken(), serviceAuth, caseDetails.getId().toString());

        return paymentClientResponse.getServiceRequests()
            .stream()
            .max(Comparator.comparing(ServiceRequestDto::getDateCreated))
            .orElse(ServiceRequestDto.builder().build());

//         Doing it via all payments endpoint

//        User u = idamService.retrieveUser(idamService.getCachedIdamOauth2Token("payments.probate@mailinator.com", "LevelAt12"));
//
//        PaymentsResponse response = paymentClient.getAllPayments(u.getAuthToken(), serviceAuth, caseDetails.getId().toString());

//        response.getPayments().stream().max(Comparator.comparing(ServiceRequestDto.PaymentDto::getDateUpdated))
//            .ifPresent(payment -> {
//
//                log.info("Payment Details:");
//                log.info("  Reference: {}", payment.getPaymentReference());
//                log.info("  Status: {}", payment.getStatus());
//                log.info("  Amount: {}", payment.getAmount());
//                log.info("  Payer: {}", payment.getPayerName());
//                log.info("  Organisation: {}", payment.getOrganisationName());
//                log.info("  Case Reference: {}", payment.getCaseReference());
//            });
//
//        return null;
    }

    private boolean isCaseOverAgeLimit(uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails) {
        return caseDetails.getLastModified()
            .toLocalDate()
            .isBefore(LocalDate.now().minusDays(MAX_CASE_AGE_DAYS));
    }

    private boolean isServiceRequestWithinGracePeriod(ServiceRequestDto serviceRequest) {
        if (serviceRequest.getDateCreated() == null) {
            return false;
        }

        LocalDateTime gracePeriodStart = LocalDateTime.now().minusHours(GRACE_PERIOD_HOURS);
        LocalDateTime serviceCreatedTime = LocalDateTime.ofInstant(
            serviceRequest.getDateCreated().toInstant(),
            ZoneId.systemDefault()
        );

        return gracePeriodStart.isBefore(serviceCreatedTime);
    }

    private boolean hasSuccessfulPayment(ServiceRequestDto serviceRequest) {
        return serviceRequest.getPayments()
            .stream()
            .map(ServiceRequestDto.PaymentDto::getStatus)
            .anyMatch(SUCCESS_STATUS::equalsIgnoreCase);
    }

    private void rejectCase(uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails, String reason, User user, String serviceAuth) {
        log.info("Rejecting case {} as {}", caseDetails.getId(), reason);
        ccdUpdateService.submitEvent(caseDetails.getId(), APPLICATION_REJECTED_FEE_NOT_PAID, user, serviceAuth);
    }
}
