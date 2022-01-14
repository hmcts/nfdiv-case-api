package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class AnswerReceivedPaymentSummary implements CcdPageConfiguration {

    private static final String SHOW_CONDITION_FEE_METHOD_PBA = "disputingFeePaymentMethod=\"feePayByAccount\"";

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
                SHOW_CONDITION_FEE_METHOD_PBA)
            .label(
                "LabelAnswerReceivedPayAccountReference1",
                "Your pay account reference: **${disputingFeeAccountReferenceNumber}**",
                SHOW_CONDITION_FEE_METHOD_PBA)
            .label(
                "LabelAnswerReceivedPayAccountReference2",
                "Fee account number: **${disputingFeeAccountNumber}**",
                SHOW_CONDITION_FEE_METHOD_PBA)
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
