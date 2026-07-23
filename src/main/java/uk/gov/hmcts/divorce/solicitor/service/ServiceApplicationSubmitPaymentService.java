package uk.gov.hmcts.divorce.solicitor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.payment.model.PbaResponse;
import uk.gov.hmcts.divorce.payment.service.PaymentService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import static org.springframework.http.HttpStatus.CREATED;

@Service
@RequiredArgsConstructor
public class ServiceApplicationSubmitPaymentService {

    private final PaymentService paymentService;
    private final Clock clock;

    public Optional<String> processSubmitPayment(long caseId, CaseData caseData) {
        AlternativeService serviceApp = caseData.getAlternativeService();
        FeeDetails feeDetails = serviceApp.getServicePaymentFee();

        if (!ServicePaymentMethod.FEE_PAY_BY_ACCOUNT.equals(feeDetails.getPaymentMethod())) {
            return Optional.empty();
        }

        Optional<String> pbaNumber = Optional.ofNullable(feeDetails.getPbaNumbers())
            .map(DynamicList::getValue)
            .map(DynamicListElement::getLabel);

        if (pbaNumber.isEmpty()) {
            return Optional.of("PBA number not present when payment method is 'Solicitor fee account (PBA)'");
        }

        if (feeDetails.getServiceRequestReference() == null) {
            return Optional.of("Service request reference is missing for PBA payment");
        }

        PbaResponse response = paymentService.processPbaPayment(
            caseId,
            feeDetails.getServiceRequestReference(),
            caseData.getApplicant1().getSolicitor(),
            pbaNumber.get(),
            feeDetails.getOrderSummary(),
            feeDetails.getAccountReferenceNumber()
        );

        if (response.getHttpStatus() != CREATED) {
            return Optional.ofNullable(response.getErrorMessage())
                .or(() -> Optional.of("Failed to process PBA payment"));
        }

        feeDetails.setPaymentReference(response.getPaymentReference());
        feeDetails.setHasCompletedOnlinePayment(YesOrNo.YES);
        feeDetails.setDateOfPayment(LocalDate.now(clock));

        return Optional.empty();
    }
}
