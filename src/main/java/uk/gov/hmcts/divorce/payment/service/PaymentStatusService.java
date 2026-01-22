package uk.gov.hmcts.divorce.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.service.task.UpdateSuccessfulPaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.payment.client.PaymentClient;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.ServiceRequestDto;
import uk.gov.hmcts.divorce.payment.model.ServiceRequestStatus;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.divorce.citizen.event.CitizenPaymentMade.CITIZEN_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.citizen.event.RespondentFinalOrderPaymentMade.RESPONDENT_FINAL_ORDER_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRejectCasesWithPaymentOverdue.APPLICATION_REJECTED_FEE_NOT_PAID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentStatusService {

    private static final int GRACE_PERIOD_HOURS = 24;

    private final PaymentClient paymentClient;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final CaseDetailsConverter caseDetailsConverter;

    private final CcdUpdateService ccdUpdateService;

    private final UpdateSuccessfulPaymentStatus updateSuccessfulPaymentStatus;

    public void hasSuccessFulPayment(List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> casesInAwaitingPaymentState) {
        log.info("PaymentStatusService: {} cases in AwaitingPayment state",
            casesInAwaitingPaymentState.size());

        final List<CaseDetails<CaseData, State>> casesWithInProgressPayments = casesInAwaitingPaymentState
            .stream()
            .filter(this::getPayments)
            .map(caseDetailsConverter::convertToCaseDetailsFromReformModel)
            .filter(this::hasInProgressPayment)
            .toList();

        log.info("PaymentStatusService: {} cases with payments In Progress",
            casesWithInProgressPayments.size());

        log.info("PaymentStatusService caseIds: {}",
            casesWithInProgressPayments
                .stream()
                .map(CaseDetails::getId)
                .toList()
        );

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String userToken = user.getAuthToken();
        final String s2sToken = authTokenGenerator.generate();

        final List<CaseDetails<CaseData, State>> casesWithSuccessfulPayment = casesWithInProgressPayments
            .stream()
            .filter(caseDetails -> hasSuccessfulPayment(caseDetails, userToken, s2sToken))
            .toList();

        triggerPaymentMadeEvent(casesWithSuccessfulPayment, user, s2sToken);
    }

    private void triggerPaymentMadeEvent(List<CaseDetails<CaseData, State>> casesWithSuccessfulPayment, User user,  String s2sToken) {
        List<Long> successfulPaymentCaseIds = new ArrayList<>();
        casesWithSuccessfulPayment.forEach(successfulPaymentCase -> {

            String eventId = AwaitingPayment == successfulPaymentCase.getState()
                    ? CITIZEN_PAYMENT_MADE : RESPONDENT_FINAL_ORDER_PAYMENT_MADE;

            Long caseId = successfulPaymentCase.getId();

            log.info("{} event called for {} with successful payment: ", eventId, caseId);

            ccdUpdateService.submitEventWithRetry(caseId.toString(), eventId, updateSuccessfulPaymentStatus, user, s2sToken);
            successfulPaymentCaseIds.add(caseId);
        });

        log.info("PaymentStatusService found with successful payments: " + successfulPaymentCaseIds);
    }

    private boolean hasInProgressPayment(uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails) {
        return Optional.ofNullable(getPayments(caseDetails))
            .orElse(emptyList())
            .stream()
            .anyMatch(ap -> ap.getValue().getStatus().equals(PaymentStatus.IN_PROGRESS));
    }

    private boolean getPayments(uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData();

        return Objects.equals(AwaitingPayment.toString(), caseDetails.getState())
                ? data.containsKey("applicationPayments") : data.containsKey("finalOrderPayments");
    }

    private List<ListValue<uk.gov.hmcts.divorce.divorcecase.model.Payment>> getPayments(CaseDetails<CaseData, State> caseDetails) {
        return AwaitingPayment == caseDetails.getState() ? caseDetails.getData().getApplication().getApplicationPayments()
                : caseDetails.getData().getFinalOrder().getFinalOrderPayments();
    }

    public boolean hasSuccessfulPayment(uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails,
                                         String userToken, String s2sToken) {

        final List<ListValue<uk.gov.hmcts.divorce.divorcecase.model.Payment>> payments = getPayments(caseDetails);

        return Optional.ofNullable(payments)
            .orElse(emptyList())
            .stream()
            .filter(ap -> ap.getValue().getStatus().equals(PaymentStatus.IN_PROGRESS))
            .map(ap -> ap.getValue().getReference())
            .filter(Objects::nonNull)
            .map(paymentReference -> paymentSuccessful(paymentReference, userToken, s2sToken))
            .findFirst()
            .orElse(false);
    }

    private boolean paymentSuccessful(String paymentReference, String userToken, String s2sToken) {
        final Payment payment = paymentClient.getPaymentByReference(
            userToken,
            s2sToken,
            paymentReference
        );

        return SUCCESS.getLabel().equalsIgnoreCase(payment.getStatus());
    }

    public void processPaymentRejection(uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails, User user, String serviceAuth) {

        List<ServiceRequestDto> allServiceRequests = getAllServiceRequests(caseDetails, user, serviceAuth);

        // Find the first service request that is NOT "NOT_PAID" (i.e., PAID or PARTIALLY_PAID)
        ServiceRequestDto paidServiceRequest = allServiceRequests.stream()
            .filter(sr -> !ServiceRequestStatus.NOT_PAID.equals(sr.getServiceRequestStatus()))
            .findFirst()
            .orElse(null);


        if (paidServiceRequest != null && isServiceRequestWithinGracePeriod(paidServiceRequest)) {
            log.info("Skipping case {} - service request created within last {} hours",
                caseDetails.getId(), GRACE_PERIOD_HOURS);
        } else if (paidServiceRequest != null
            && (ServiceRequestStatus.NOT_PAID.equals(paidServiceRequest.getServiceRequestStatus())
                || !paidServiceRequest.hasSuccessfulPayment())) {
            rejectCase(caseDetails, "payment not made after creating service request", user, serviceAuth);
        }
    }

    private List<ServiceRequestDto> getAllServiceRequests(uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails,
                                                      User user, String serviceAuth) {
        var paymentClientResponse = paymentClient.getServiceRequests(
            user.getAuthToken(), serviceAuth, caseDetails.getId().toString());

        return paymentClientResponse.getServiceRequests()
            .stream()
            .filter(Objects::nonNull)
            .toList();
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

    private void rejectCase(uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails,
                            String reason, User user, String serviceAuth) {
        log.info("Rejecting case {} as {}", caseDetails.getId(), reason);
        ccdUpdateService.submitEvent(caseDetails.getId(), APPLICATION_REJECTED_FEE_NOT_PAID, user, serviceAuth);
    }
}
