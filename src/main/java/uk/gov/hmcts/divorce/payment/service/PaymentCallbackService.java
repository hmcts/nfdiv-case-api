package uk.gov.hmcts.divorce.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.common.service.task.UpdateSuccessfulPaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.payment.model.OnlinePaymentMethod;
import uk.gov.hmcts.divorce.payment.model.PaymentCallbackDto;
import uk.gov.hmcts.divorce.payment.model.ServiceRequestStatus;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import java.util.Optional;

import static uk.gov.hmcts.divorce.citizen.event.CitizenGeneralApplicationPaymentMade.CITIZEN_GENERAL_APPLICATION_PAYMENT;
import static uk.gov.hmcts.divorce.citizen.event.CitizenPaymentMade.CITIZEN_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.citizen.event.CitizenServicePaymentMade.CITIZEN_SERVICE_PAYMENT;
import static uk.gov.hmcts.divorce.citizen.event.RespondentFinalOrderPaymentMade.RESPONDENT_FINAL_ORDER_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralApplicationPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServicePayment;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackService {

    private final CcdUpdateService ccdUpdateService;

    private final CaseDetailsConverter caseDetailsConverter;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final CoreCaseDataApi coreCaseDataApi;

    private final UpdateSuccessfulPaymentStatus updateSuccessfulPaymentStatus;

    private static final String LOG_CALLBACK_RECEIVED = """
        Payment callback received for payment: {}, case id: {}, service request status: {}, payment method: {}
        """;

    private static final String LOG_NOT_PROCESSING_INVALID_PAYMENT = """
        Not processing payment callback ({}) for case id: {}, payment was done by PBA or is incomplete.
        """;

    private static final String LOG_NOT_PROCESSING_COMPLETED_CALLBACK = """
        Not processing payment callback ({}) for case id: {}, case is no longer awaiting payment.
        """;

    public void handleCallback(PaymentCallbackDto paymentCallback) {
        final String caseRef = paymentCallback.getCcdCaseNumber();
        final String paymentRef = paymentCallback.getPayment().getPaymentReference();
        final ServiceRequestStatus serviceRequestStatus = paymentCallback.getServiceRequestStatus();
        final String serviceRequestReference = paymentCallback.getServiceRequestReference();
        final OnlinePaymentMethod paymentMethod = paymentCallback.getPayment().getPaymentMethod();

        log.info(LOG_CALLBACK_RECEIVED, paymentRef, caseRef, serviceRequestStatus, paymentMethod);

        if (serviceRequestNotPaid(serviceRequestStatus) || isSolicitorPbaPayment(paymentMethod)) {
            log.info(LOG_NOT_PROCESSING_INVALID_PAYMENT, paymentRef, caseRef);
            return;
        }

        final User systemUpdateUser = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> details = caseDetailsConverter.convertToCaseDetailsFromReformModel(
            coreCaseDataApi.getCase(systemUpdateUser.getAuthToken(), serviceAuthorization, caseRef)
        );
        final State state = details.getState();
        final String paymentMadeEvent = paymentMadeEvent(state, serviceRequestReference, details.getData());

        if (paymentMadeEvent == null) {
            log.info(LOG_NOT_PROCESSING_COMPLETED_CALLBACK, paymentRef, caseRef);
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

    private String paymentMadeEvent(State state, String paymentServiceRequest, CaseData caseData) {
        if (AwaitingPayment.equals(state) && isApplicationPayment(paymentServiceRequest, caseData)) {
            return CITIZEN_PAYMENT_MADE;
        }

        if (AwaitingFinalOrderPayment.equals(state) && isFinalOrderPayment(paymentServiceRequest, caseData)) {
            return RESPONDENT_FINAL_ORDER_PAYMENT_MADE;
        }

        if (AwaitingServicePayment.equals(state) && isServicePayment(paymentServiceRequest, caseData)) {
            return CITIZEN_SERVICE_PAYMENT;
        }

        if (AwaitingGeneralApplicationPayment.equals(state) && isGeneralApplicationPayment(paymentServiceRequest, caseData)) {
            return CITIZEN_GENERAL_APPLICATION_PAYMENT;
        }

        return null;
    }

    private boolean isApplicationPayment(String paymentServiceRequest, CaseData caseData) {
        return Optional.of(caseData.getApplication())
            .map(Application::getApplicationFeeServiceRequestReference)
            .filter(paymentServiceRequest::equals)
            .isPresent();
    }

    private boolean isFinalOrderPayment(String paymentServiceRequest, CaseData caseData) {
        return Optional.ofNullable(caseData.getFinalOrder())
            .map(FinalOrder::getApplicant2FinalOrderFeeServiceRequestReference)
            .filter(paymentServiceRequest::equals)
            .isPresent();
    }

    private boolean isServicePayment(String paymentServiceRequest, CaseData caseData) {
        return Optional.ofNullable(caseData.getAlternativeService())
            .map(AlternativeService::getServicePaymentFee)
            .map(FeeDetails::getServiceRequestReference)
            .filter(paymentServiceRequest::equals)
            .isPresent();
    }

    private boolean isGeneralApplicationPayment(String paymentServiceRequest, CaseData caseData) {
        return Optional.ofNullable(caseData.getApplicant1().getGeneralAppServiceRequest())
            .filter(paymentServiceRequest::equals)
            .isPresent();
    }
}
