package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;

public class AlternativeServicePaymentConfirmation implements CcdPageConfiguration  {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("alternativeServicePayment")
            .pageLabel("Payment - service application payment")
            .complex(CaseData::getAlternativeService)
                .complex(AlternativeService::getServicePaymentFee)
                .mandatory(FeeDetails::getPaymentMethod)
                .mandatory(FeeDetails::getAccountNumber, "servicePaymentFeePaymentMethod = \"feePayByAccount\"")
                .optional(FeeDetails::getAccountReferenceNumber, "servicePaymentFeePaymentMethod = \"feePayByAccount\"")
                .mandatory(FeeDetails::getHelpWithFeesReferenceNumber, "servicePaymentFeePaymentMethod = \"feePayByHelp\"")
                .done()
            .done();
    }
}
