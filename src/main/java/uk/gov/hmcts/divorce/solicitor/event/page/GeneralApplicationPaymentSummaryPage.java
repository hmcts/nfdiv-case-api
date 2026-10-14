package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;

public class GeneralApplicationPaymentSummaryPage implements CcdPageConfiguration {

    private static final String PBA = "generalApplicationFeePaymentMethod=\"feePayByAccount\"";
    private static final String HWF = "generalApplicationFeePaymentMethod=\"feePayByHelp\"";

    private static final String HWF_REFERENCE_LABEL = "Applicant’s Help With Fees reference";

    private static final String HWF_REFERENCE_HINT = "Enter a Help With Fees reference that is specific to this application. You cannot "
        + "reuse a reference that has been used for a different application.";

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("generalApplicationPaymentSummary")
            .complex(CaseData::getGeneralApplication)
                .complex(GeneralApplication::getGeneralApplicationFee)
                    .mandatoryNoSummary(FeeDetails::getOrderSummary, PBA, "")
                    .readonly(FeeDetails::getPaymentMethod)
                    .label("paymentMethodLabel",
                "You can change the payment method by using the \"Amend General Application\" event")
                    .mandatory(FeeDetails::getPbaNumbers, PBA)
                    .mandatory(FeeDetails::getAccountReferenceNumber, PBA)
                    .mandatory(FeeDetails::getHelpWithFeesReferenceNumber, HWF, null, HWF_REFERENCE_LABEL, HWF_REFERENCE_HINT)
                .done()
            .done();
    }
}
