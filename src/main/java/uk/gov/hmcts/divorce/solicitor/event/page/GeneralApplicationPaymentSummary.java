package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;

public class GeneralApplicationPaymentSummary implements CcdPageConfiguration {

    private static final String SHOW_CONDITION_FEE_PBA = "generalApplicationFeePaymentMethod=\"feePayByAccount\"";

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("generalApplicationPaymentSummary")
            .complex(CaseData::getGeneralApplication)
                .complex(GeneralApplication::getGeneralApplicationFee)
                    .mandatoryNoSummary(FeeDetails::getOrderSummary)
                .done()
            .done()
            .label(
                "LabelGeneralApplicationPayAccountHeading",
                "Payment Method: Fee Account",
                SHOW_CONDITION_FEE_PBA)
            .label(
                "LabelGeneralApplicationPayAccountReference1",
                "Your pay account reference: **${generalApplicationFeeAccountReferenceNumber}**",
                SHOW_CONDITION_FEE_PBA)
            .label(
                "LabelGeneralApplicationPayHWFHeading",
                "Payment Method: Help with Fees",
                "generalApplicationFeePaymentMethod=\"feePayByHelp\"")
            .label(
                "LabelGeneralApplicationPayHWFReference",
                "Your Help with Fees reference: **${generalApplicationFeeHelpWithFeesReferenceNumber}**",
                "generalApplicationFeePaymentMethod=\"feePayByHelp\"")
            .label(
                "LabelGeneralApplicationPayTelephoneHeading",
                "Payment Method: Telephone",
                "generalApplicationFeePaymentMethod=\"feePayByTelephone\"");
    }
}
