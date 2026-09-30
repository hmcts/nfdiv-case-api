package uk.gov.hmcts.divorce.solicitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;

import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationFee.FEE0227;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationFee.FEE0228;
import static uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod.FEE_PAY_BY_HWF;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEES_HELP_WITH;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralApplicationPaymentPreparationService {

    private final PbaService pbaService;
    private final PaymentSetupService paymentSetupService;

    public void prepareDraftGeneralApplicationFee(
        long caseId,
        Applicant applicant,
        InterimApplicationOptions options,
        GeneralApplication generalApplication
    ) {
        generalApplication.setGeneralApplicationFeeType(options.isHearingRequired() ? FEE0227 : FEE0228);

        FeeDetails fee = generalApplication.getGeneralApplicationFee();
        ServicePaymentMethod paymentMethod =
            FEES_HELP_WITH.equals(options.getInterimAppsPaymentMethod()) ? FEE_PAY_BY_HWF : FEE_PAY_BY_ACCOUNT;

        fee.setPaymentMethod(paymentMethod);

        log.info("Preparing draft general application fee for case id: {}, payment method: {}", caseId, paymentMethod);

        if (FEE_PAY_BY_ACCOUNT.equals(paymentMethod)) {
            OrderSummary orderSummary = paymentSetupService.createGeneralApplicationOrderSummary(caseId, generalApplication
                .getGeneralApplicationFeeType());
            fee.setOrderSummary(orderSummary);

            DynamicList pbaNumbers = pbaService.populatePbaDynamicList();
            fee.setPbaNumbers(pbaNumbers);

            fee.setServiceRequestReference(
                paymentSetupService.createGeneralApplicationPaymentServiceRequest(
                    orderSummary, caseId, applicant.getFullName()
                )
            );
        } else {
            fee.setServiceRequestReference(null);
        }
    }
}
