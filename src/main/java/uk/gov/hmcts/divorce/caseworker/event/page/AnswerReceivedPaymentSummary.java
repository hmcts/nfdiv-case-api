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
                "LabelAnswerReceivedPayAccountHeading",
                "Payment Method: Fee Account",
                "disputingFeePaymentMethod=\"feePayByAccount\"")
            .label(
                "LabelAnswerReceivedPayAccountReference1",
                "Your pay account reference: **${disputingFeeAccountReferenceNumber}**",
                "disputingFeePaymentMethod=\"feePayByAccount\"")
            .label(
                "LabelAnswerReceivedPayAccountReference2",
                "Fee account number: **${disputingFeeAccountNumber}**",
                "disputingFeePaymentMethod=\"feePayByAccount\"")
            .label(
                "LabelAnswerReceivedPayHWFHeading",
                "Payment Method: Help with Fees",
                "disputingFeePaymentMethod=\"feePayByHelp\"")
            .label(
                "LabelAnswerReceivedPayHWFReference",
                "Your Help with Fees reference: **${disputingFeeHelpWithFeesReferenceNumber}**",
                "disputingFeePaymentMethod=\"feePayByHelp\"")
            .label(
                "LabelAnswerReceivedPayTelephoneHeading",
                "Payment Method: Telephone",
                "disputingFeePaymentMethod=\"feePayByTelephone\"");
    }
}
