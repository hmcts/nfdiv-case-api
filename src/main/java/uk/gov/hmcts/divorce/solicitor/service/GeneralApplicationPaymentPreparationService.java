package uk.gov.hmcts.divorce.solicitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;

import java.math.BigDecimal;
import java.text.NumberFormat;

import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationFee.FEE0227;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationFee.FEE0228;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationFee.FEE0233;
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
        GeneralApplication generalApplication,
        String existingServiceRequestReference,
        CaseData caseData
    ) {
        generalApplication.setGeneralApplicationFeeType(
            InterimApplicationType.SOLICITOR_D8B_ANSWERS.equals(options.getInterimApplicationType())
                ? FEE0233 : options.isHearingRequired() ? FEE0227 : FEE0228);

        FeeDetails fee = generalApplication.getGeneralApplicationFee();
        prepareDraftFee(caseId, applicant, options, generalApplication, fee,
            existingServiceRequestReference, options.getInterimApplicationType(), caseData);
    }

    private void prepareDraftFee(
        long caseId,
        Applicant applicant,
        InterimApplicationOptions options,
        GeneralApplication generalApplication,
        FeeDetails fee,
        String existingServiceRequestReference,
        InterimApplicationType applicationType,
        CaseData caseData
        ) {
        ServicePaymentMethod paymentMethod =
            FEES_HELP_WITH.equals(options.getInterimAppsPaymentMethod()) ? FEE_PAY_BY_HWF : FEE_PAY_BY_ACCOUNT;

        fee.setPaymentMethod(paymentMethod);

        log.info("Preparing draft fee for case id: {}, payment method: {}", caseId, paymentMethod);

        if (FEE_PAY_BY_ACCOUNT.equals(paymentMethod)) {
            OrderSummary orderSummary;
            if (InterimApplicationType.DIGITISED_GENERAL_APPLICATION_D11.equals(applicationType)) {
                orderSummary = paymentSetupService.createGeneralApplicationOrderSummary(caseId, generalApplication.getGeneralApplicationFeeType());
            } else {
                orderSummary = paymentSetupService.createSolicitorD8bFeeOrderSummary(caseData, caseId);
            }

            fee.setOrderSummary(orderSummary);

            generalApplication.setSolGeneralApplicationFeeInPounds(
                NumberFormat.getNumberInstance().format(
                    new BigDecimal(orderSummary.getPaymentTotal()).movePointLeft(2))
            );

            DynamicList pbaNumbers = pbaService.populatePbaDynamicList();
            fee.setPbaNumbers(pbaNumbers);

            String serviceRequestReference = StringUtils.isNotBlank(existingServiceRequestReference)
                ? existingServiceRequestReference
                : paymentSetupService.createGeneralApplicationPaymentServiceRequest(
                orderSummary, caseId, applicant.getFullName()
            );

            fee.setServiceRequestReference(serviceRequestReference);
            applicant.setActiveGeneralApplication(serviceRequestReference);
        } else {
            fee.setServiceRequestReference(null);
            applicant.setActiveGeneralApplication(null);
        }
    }
}
