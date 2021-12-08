package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class AnswerReceivedPaymentSummary implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("AnswerReceivedPaymentSummary")
            .complex(CaseData::getAcknowledgementOfService)
            .mandatoryNoSummary(AcknowledgementOfService::getDisputingFee)
            .done()
            .label(
                "LabelPayAccountHeading",
                "Payment Method: Fee Account",
                "disputingFeePaymentMethod=\"feePayByAccount\"")
            .label(
                "LabelPayAccountReference1",
                "Your pay account reference: **${disputingFeeAccountReferenceNumber}**",
                "disputingFeePaymentMethod=\"feePayByAccount\"")
            .label(
                "LabelPayAccountReference2",
                "Fee account number: **${disputingFeeAccountNumber}**",
                "disputingFeePaymentMethod=\"feePayByAccount\"")
            .label(
                "LabelPayHWFHeading",
                "Payment Method: Help with Fees",
                "disputingFeePaymentMethod=\"feePayByHelp\"")
            .label(
                "LabelPayHWFReference",
                "Your Help with Fees reference: **${disputingFeeHelpWithFeesReferenceNumber}**",
                "disputingFeePaymentMethod=\"feePayByHelp\"")
            .label(
                "LabelPayTelephoneHeading",
                "Payment Method: Telephone",
                "disputingFeePaymentMethod=\"feePayByTelephone\"")
            .label(
                "LabelPayChequeHeading",
                "Payment Method: Cheque",
                "disputingFeePaymentMethod=\"feePayByCheque\"");
    }
}
