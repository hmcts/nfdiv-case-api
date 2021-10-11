package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class AlternativeServicePaymentConfirmation implements CcdPageConfiguration  {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("alternativeServicePayment")
            .pageLabel("Payment - service application payment")
            .complex(CaseData::getAlternativeService)
                .mandatory(AlternativeService::getPaymentMethod)
                .mandatory(AlternativeService::getFeeAccountNumber, "paymentMethod = \"feePayByAccount\"")
                .optional(AlternativeService::getFeeAccountReferenceNumber, "paymentMethod = \"feePayByAccount\"")
                .mandatory(AlternativeService::getHelpWithFeesReferenceNumber, "paymentMethod = \"feePayByHelp\"")
            .done();
    }
}
