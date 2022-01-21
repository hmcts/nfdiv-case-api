package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;

public class GeneralReferralPaymentSummary implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("GeneralReferralPaymentSummary")
            .complex(CaseData::getGeneralReferral)
            .complex(GeneralReferral::getGeneralReferralFee)
                .mandatoryNoSummary(FeeDetails::getOrderSummary)
                .done()
            .done()
            .label(
                "LabelGeneralReferralPayAccountHeading",
                "Payment Method: Fee Account",
                "generalReferralFeePaymentMethod=\"feePayByAccount\"")
            .label(
                "LabelGeneralReferralPayAccountReference1",
                "Your pay account reference: **${generalReferralFeeAccountReferenceNumber}**",
                "generalReferralFeePaymentMethod=\"feePayByAccount\"")
            .label(
                "LabelGeneralReferralPayAccountReference2",
                "Fee account number: **${generalReferralFeeAccountNumber}**",
                "generalReferralFeePaymentMethod=\"feePayByAccount\"")
            .label(
                "LabelGeneralReferralPayHWFHeading",
                "Payment Method: Help with Fees",
                "generalReferralFeePaymentMethod=\"feePayByHelp\"")
            .label(
                "LabelGeneralReferralPayHWFReference",
                "Your Help with Fees reference: **${generalReferralFeeHelpWithFeesReferenceNumber}**",
                "generalReferralFeePaymentMethod=\"feePayByHelp\"")
            .label(
                "LabelGeneralReferralPayTelephoneHeading",
                "Payment Method: Telephone",
                "generalReferralFeePaymentMethod=\"feePayByTelephone\"");
    }
}
