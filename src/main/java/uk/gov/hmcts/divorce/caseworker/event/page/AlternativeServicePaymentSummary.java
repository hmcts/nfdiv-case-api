package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;

public class AlternativeServicePaymentSummary implements CcdPageConfiguration {
    private static final String FEE_PAY_BY_ACCOUNT = "servicePaymentFeePaymentMethod=\"feePayByAccount\"";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("AltPaymentSummary")
            .complex(CaseData::getAlternativeService)
                .complex(AlternativeService::getServicePaymentFee)
                .mandatoryNoSummary(FeeDetails::getOrderSummary)
                .done()
            .done()
            .label(
                "LabelPayAccountHeading",
                "Payment Method: Fee Account",
                FEE_PAY_BY_ACCOUNT)
            .label(
                "LabelPayAccountReference1",
                "Your pay account reference: **${servicePaymentFeeAccountReferenceNumber}**",
                FEE_PAY_BY_ACCOUNT)
            .label(
                "LabelPayAccountReference2",
                "Fee account number: **${servicePaymentFeeAccountNumber}**",
                FEE_PAY_BY_ACCOUNT)
            .label(
                "LabelPayHWFHeading",
                "Payment Method: Help with Fees",
                "servicePaymentFeePaymentMethod=\"feePayByHelp\"")
            .label(
                "LabelPayHWFReference",
                "Your Help with Fees reference: **${servicePaymentFeeHelpWithFeesReferenceNumber}**",
                "servicePaymentFeePaymentMethod=\"feePayByHelp\"")
            .label(
                "LabelPayTelephoneHeading",
                "Payment Method: Telephone",
                "servicePaymentFeePaymentMethod=\"feePayByTelephone\"");
    }
}
