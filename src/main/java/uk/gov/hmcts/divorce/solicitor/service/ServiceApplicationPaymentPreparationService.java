package uk.gov.hmcts.divorce.solicitor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;

import static uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod.FEE_PAY_BY_HWF;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEES_HELP_WITH;

@Service
@RequiredArgsConstructor
public class ServiceApplicationPaymentPreparationService {

    private final PaymentSetupService paymentSetupService;

    public void prepareDraftServiceApplicationFee(
        long caseId,
        Applicant applicant,
        InterimApplicationOptions options,
        AlternativeService serviceApplication
    ) {
        FeeDetails feeDetails = serviceApplication.getServicePaymentFee();
        ServicePaymentMethod paymentMethod =
            FEES_HELP_WITH.equals(options.getInterimAppsPaymentMethod()) ? FEE_PAY_BY_HWF : FEE_PAY_BY_ACCOUNT;

        feeDetails.setPaymentMethod(paymentMethod);
        feeDetails.setOrderSummary(paymentSetupService.createServiceApplicationOrderSummary(serviceApplication, caseId));

        if (FEE_PAY_BY_ACCOUNT.equals(paymentMethod)) {
            feeDetails.setServiceRequestReference(paymentSetupService.createServiceApplicationPaymentServiceRequest(
                serviceApplication, caseId, applicant.getFullName()
            ));
            feeDetails.setHelpWithFeesReferenceNumber(null);
        } else {
            feeDetails.setServiceRequestReference(null);
        }
    }
}
