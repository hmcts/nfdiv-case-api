package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;

public class GeneralApplicationPaymentConfirmation implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("generalApplicationPayment")
            .pageLabel("Payment - general application payment")
            .complex(CaseData::getGeneralApplication)
                .complex(GeneralApplication::getGeneralApplicationFee)
                .mandatory(FeeDetails::getPaymentMethod)
                .mandatory(FeeDetails::getPbaNumbers,
                    "generalApplicationFeePaymentMethod = \"feePayByAccount\"")
                .mandatory(FeeDetails::getAccountReferenceNumber,
                    "generalApplicationFeePaymentMethod = \"feePayByAccount\"")
                .mandatory(FeeDetails::getHelpWithFeesReferenceNumber,
                    "generalApplicationFeePaymentMethod = \"feePayByHelp\"")
                .done()
            .done();
    }
}
