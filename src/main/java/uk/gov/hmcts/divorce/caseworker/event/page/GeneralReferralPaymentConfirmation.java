package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;

public class GeneralReferralPaymentConfirmation implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("GeneralReferralPayment")
            .pageLabel("Payment - general referral payment")
            .complex(CaseData::getGeneralReferral)
            .complex(GeneralReferral::getGeneralReferralFee)
            .mandatory(FeeDetails::getPaymentMethod)
            .mandatory(FeeDetails::getAccountNumber,
                "generalReferralFeePaymentMethod=\"feePayByAccount\"")
            .optional(FeeDetails::getAccountReferenceNumber,
                "generalReferralFeePaymentMethod=\"feePayByAccount\"")
            .mandatory(FeeDetails::getHelpWithFeesReferenceNumber,
                "generalReferralFeePaymentMethod=\"feePayByHelp\"")
            .done()
            .done();
    }
}
