package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;

public class AnswerReceivedPaymentConfirmation implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("answerReceivedPayment")
            .pageLabel("Payment - answer application payment")
            .complex(CaseData::getAcknowledgementOfService)
                .complex(AcknowledgementOfService::getDisputingFee)
                .mandatory(FeeDetails::getPaymentMethod)
                .mandatory(FeeDetails::getAccountNumber,
                    "disputingFeePaymentMethod = \"feePayByAccount\"")
                .optional(FeeDetails::getAccountReferenceNumber,
                    "disputingFeePaymentMethod = \"feePayByAccount\"")
                .mandatory(FeeDetails::getHelpWithFeesReferenceNumber,
                    "disputingFeePaymentMethod = \"feePayByHelp\"")
                .done()
            .done();
    }
}
