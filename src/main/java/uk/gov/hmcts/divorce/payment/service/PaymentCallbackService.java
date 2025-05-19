package uk.gov.hmcts.divorce.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.common.service.task.UpdateSuccessfulPaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.payment.model.OnlinePaymentMethod;
import uk.gov.hmcts.divorce.payment.model.PaymentCallbackDto;
import uk.gov.hmcts.divorce.payment.model.ServiceRequestStatus;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static uk.gov.hmcts.divorce.citizen.event.CitizenPaymentMade.CITIZEN_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.citizen.event.RespondentFinalOrderPaymentMade.RESPONDENT_FINAL_ORDER_PAYMENT_MADE;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackService {

    private final CcdUpdateService ccdUpdateService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final CoreCaseDataApi coreCaseDataApi;

    private final UpdateSuccessfulPaymentStatus updateSuccessfulPaymentStatus;

    private static final String LOG_NOT_PROCESSING_CALLBACK = """
        Not processing callback for payment {}, case id: {}, status: {}, payment method: {}
        """;

    public void handleCallback(PaymentCallbackDto paymentCallback) {
        final String caseRef = paymentCallback.getCcdCaseNumber();
        final String paymentRef = paymentCallback.getPayment().getPaymentReference();
        final ServiceRequestStatus serviceRequestStatus = paymentCallback.getServiceRequestStatus();
        final OnlinePaymentMethod paymentMethod = paymentCallback.getPayment().getPaymentMethod();

        if (serviceRequestNotPaid(serviceRequestStatus) || isSolicitorPbaPayment(paymentMethod)) {
            log.info(LOG_NOT_PROCESSING_CALLBACK, paymentRef, caseRef, serviceRequestStatus, paymentMethod);
            return;
        }

        final User systemUpdateUser = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();
        final CaseDetails details = coreCaseDataApi.getCase(systemUpdateUser.getAuthToken(), serviceAuthorization, caseRef);
        final State state = State.valueOf(details.getState());
        final String paymentMadeEvent = paymentMadeEvent(state);

        if (paymentMadeEvent == null) {
            log.info(LOG_NOT_PROCESSING_CALLBACK, paymentRef, caseRef, serviceRequestStatus, paymentMethod);
            return;
        }

        ccdUpdateService.submitEventWithRetry(
            caseRef,
            paymentMadeEvent,
            updateSuccessfulPaymentStatus,
            systemUpdateUser,
            serviceAuthorization
        );
    }

    private boolean serviceRequestNotPaid(ServiceRequestStatus serviceRequestStatus) {
        return !ServiceRequestStatus.PAID.equals(serviceRequestStatus);
    }

    private boolean isSolicitorPbaPayment(OnlinePaymentMethod paymentMethod) {
        return OnlinePaymentMethod.PAYMENT_BY_ACCOUNT.equals(paymentMethod);
    }

    private String paymentMadeEvent(State state) {
        return switch (state) {
            case AwaitingPayment -> CITIZEN_PAYMENT_MADE;
            case AwaitingFinalOrderPayment -> RESPONDENT_FINAL_ORDER_PAYMENT_MADE;
            default -> null;
        };
    }
}
