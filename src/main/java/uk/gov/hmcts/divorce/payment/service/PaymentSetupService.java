package uk.gov.hmcts.divorce.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import static uk.gov.hmcts.divorce.controller.PaymentCallbackController.PAYMENT_UPDATE_PATH;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_ISSUE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_DIVORCE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_NOTICE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.SERVICE_DIVORCE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.SERVICE_OTHER;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentSetupService {

    private final PaymentService paymentService;

    public String createApplicationFeeServiceRequest(CaseData data, long caseId) {
        if (data.getApplication() != null && data.getApplication().getApplicationFeeServiceRequestReference() != null) {
            return data.getApplication().getApplicationFeeServiceRequestReference();
        }

        log.info("Application fee service request not found for case id: {}, creating service request", caseId);

        return paymentService.createServiceRequestReference(
            getPaymentCallbackUrl(),
            caseId,
            data.getApplicant1().getFullName(),
            data.getApplication().getApplicationFeeOrderSummary()
        );
    }

    public OrderSummary createApplicationFeeOrderSummary(CaseData data, long caseId) {
        if (data.getApplication() != null && data.getApplication().getApplicationFeeOrderSummary() != null) {
            return data.getApplication().getApplicationFeeOrderSummary();
        }

        log.info("Application fee order summary not found for case id: {}, creating order summary", caseId);

        return paymentService.getOrderSummaryByServiceEvent(SERVICE_DIVORCE, EVENT_ISSUE, KEYWORD_DIVORCE);
    }

    public String createFinalOrderFeeServiceRequest(CaseData data, long caseId, OrderSummary orderSummary) {
        if (data.getFinalOrder() != null && data.getFinalOrder().getApplicant2FinalOrderFeeServiceRequestReference() != null) {
            return data.getFinalOrder().getApplicant2FinalOrderFeeServiceRequestReference();
        }

        log.info("Final order fee service request not found for case id: {}, creating service request", caseId);

        return paymentService.createServiceRequestReference(
            getPaymentCallbackUrl(),
            caseId,
            data.getApplicant2().getFullName(),
            orderSummary
        );
    }

    public OrderSummary createFinalOrderFeeOrderSummary(CaseData data, long caseId) {
        if (data.getFinalOrder() != null && data.getFinalOrder().getApplicant2FinalOrderFeeOrderSummary() != null) {
            return data.getFinalOrder().getApplicant2FinalOrderFeeOrderSummary();
        }

        log.info("Final order fee order summary not found for case id: {}, creating order summary", caseId);

        return paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_NOTICE);
    }

    public static String getPaymentCallbackUrl() {
        return System.getenv().getOrDefault("CASE_API_URL", "http://localhost:4013") + PAYMENT_UPDATE_PATH;
    }
}
