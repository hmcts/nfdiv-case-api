package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class AnswerReceivedPaymentConfirmation implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("answerReceivedPayment")
            .pageLabel("Payment - answer application payment")
            .complex(CaseData::getAcknowledgementOfService)
            .mandatory(AcknowledgementOfService::getDisputingFeePaymentMethod)
            .mandatory(AcknowledgementOfService::getDisputingFeeAccountNumber,
                "disputingFeePaymentMethod = \"feePayByAccount\"")
            .optional(AcknowledgementOfService::getDisputingFeeAccountReferenceNumber,
                "disputingFeePaymentMethod = \"feePayByAccount\"")
            .mandatory(AcknowledgementOfService::getDisputingFeeHelpWithFeesReferenceNumber,
                "disputingFeePaymentMethod = \"feePayByHelp\"")
            .done();
    }
}
