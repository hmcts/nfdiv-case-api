package uk.gov.hmcts.divorce.solicitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;

import static uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod.FEE_PAY_BY_HWF;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEES_HELP_WITH;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceApplicationPaymentPreparationService {

    private final PbaService pbaService;

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

        log.info("Preparing draft service application fee for case id: {}, payment method: {}", caseId, paymentMethod);

        feeDetails.setPaymentMethod(paymentMethod);

        if (FEE_PAY_BY_ACCOUNT.equals(paymentMethod)) {
            feeDetails.setOrderSummary(paymentSetupService.createServiceApplicationOrderSummary(serviceApplication, caseId));

            preparePbaDraftFee(caseId, applicant, serviceApplication, feeDetails);
        } else {
            feeDetails.setServiceRequestReference(null);
        }
    }

    private void preparePbaDraftFee(
        long caseId,
        Applicant applicant,
        AlternativeService serviceApplication,
        FeeDetails feeDetails
    ) {
        final DynamicList pbaNumbersDynamicList = pbaService.populatePbaDynamicList();

        feeDetails.setPbaNumbers(pbaNumbersDynamicList);

        log.info("Creating payment service request for case id: {}", caseId);
        feeDetails.setServiceRequestReference(paymentSetupService.createServiceApplicationPaymentServiceRequest(
            serviceApplication, caseId, applicant.getFullName()
        ));
        log.info("Payment service request set for case id: {}", caseId);
    }
}
