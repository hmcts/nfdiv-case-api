package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class AlternativeServicePaymentSummary implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("AltPaymentSummary")
            .complex(CaseData::getAlternativeService)
                .mandatoryNoSummary(AlternativeService::getServicePaymentFeeOrderSummary)
            .done()
            .label(
                "LabelPayAccountHeading",
                "Payment Method: Fee Account",
                "paymentMethod=\"feePayByAccount\"")
            .label(
                "LabelPayAccountReference1",
                "Your pay account reference: **${feeAccountReferenceNumber}**",
                "paymentMethod=\"feePayByAccount\"")
            .label(
                "LabelPayAccountReference2",
                "Fee account number: **${feeAccountNumber}**",
                "paymentMethod=\"feePayByAccount\"")
            .label(
                "LabelPayHWFHeading",
                "Payment Method: Help with Fees",
                "paymentMethod=\"feePayByHelp\"")
            .label(
                "LabelPayHWFReference",
                "Your Help with Fees reference: **${helpWithFeesReferenceNumber}**",
                "paymentMethod=\"feePayByHelp\"")
            .label(
                "LabelPayTelephoneHeading",
                "Payment Method: Telephone",
                "paymentMethod=\"feePayByTelephone\"")
            .label(
                "LabelPayChequeHeading",
                "Payment Method: Cheque",
                "paymentMethod=\"feePayByCheque\"");
    }
}
