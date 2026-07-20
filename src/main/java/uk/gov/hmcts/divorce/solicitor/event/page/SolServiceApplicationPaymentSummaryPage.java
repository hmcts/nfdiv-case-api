package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;

public class SolServiceApplicationPaymentSummaryPage implements CcdPageConfiguration {

    private static final String PBA = "servicePaymentFeePaymentMethod=\"feePayByAccount\"";
    private static final String HWF = "servicePaymentFeePaymentMethod=\"feePayByHelp\"";

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("solServiceAppPaySummary")
            .pageLabel("Submit service application")
            .complex(CaseData::getAlternativeService)
                .complex(AlternativeService::getServicePaymentFee)
                    .mandatoryNoSummary(FeeDetails::getOrderSummary, PBA, "")
                    .readonly(FeeDetails::getPaymentMethod)
                    .label("labelServicePaymentFeePaymentMethod",
                "You can change the payment method by using the “Amend service application” event")
                    .mandatory(FeeDetails::getPbaNumbers, PBA)
                    .mandatory(FeeDetails::getAccountReferenceNumber, PBA)
                    .mandatory(FeeDetails::getHelpWithFeesReferenceNumber, HWF)
                .done()
            .done();
    }
}
