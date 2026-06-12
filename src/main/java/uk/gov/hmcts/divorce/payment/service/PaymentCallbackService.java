package uk.gov.hmcts.divorce.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.payment.model.OnlinePaymentMethod;
import uk.gov.hmcts.divorce.payment.model.PaymentCallbackDto;
import uk.gov.hmcts.divorce.payment.model.ServiceRequestStatus;
import uk.gov.hmcts.divorce.payment.rule.PaymentMadeRule;
import uk.gov.hmcts.divorce.payment.rule.PaymentMadeRuleEngine;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackService {

    private final CcdUpdateService ccdUpdateService;

    private final CaseDetailsConverter caseDetailsConverter;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final CoreCaseDataApi coreCaseDataApi;

    private final PaymentMadeRuleEngine PaymentMadeRuleEngine;

    private static final String LOG_CALLBACK_RECEIVED = """
        Payment callback received for payment: {}, case id: {}, service request status: {}, payment method: {}
        """;

    private static final String LOG_NOT_PROCESSING_INVALID_PAYMENT = """
        Not processing payment callback ({}) for case id: {}, payment was done by PBA or is incomplete.
        """;

    private static final String LOG_NOT_PROCESSING_NO_MATCHING_RULE = """
        Not processing payment callback ({}) for case id: {}, no matching rule found for application awaiting payment
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

        final User systemUser = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> details = caseDetailsConverter.convertToCaseDetailsFromReformModel(
            coreCaseDataApi.getCase(systemUser.getAuthToken(), serviceAuthorization, caseRef)
        );

        final State state = details.getState();
        final CaseData caseData = details.getData();

        final var matchingPaymentMadeRuleOpt = PaymentMadeRuleEngine.find(state, serviceRequestReference, caseData);

        if (matchingPaymentMadeRuleOpt.isEmpty()) {
            log.info(LOG_NOT_PROCESSING_NO_MATCHING_RULE, paymentRef, caseRef);

            return;
        }

        PaymentMadeRule rule = matchingPaymentMadeRuleOpt.get();

        ccdUpdateService.submitEventWithRetry(
            caseRef,
            rule.paymentMadeEvent(),
            rule.updatePaymentStatusTask(),
            systemUser,
            serviceAuthorization
        );
    }

    private boolean serviceRequestNotPaid(ServiceRequestStatus serviceRequestStatus) {
        return !ServiceRequestStatus.PAID.equals(serviceRequestStatus);
    }

    private boolean isSolicitorPbaPayment(OnlinePaymentMethod paymentMethod) {
        return OnlinePaymentMethod.PAYMENT_BY_ACCOUNT.equals(paymentMethod);
    }
}
