package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class SolPaymentSummary implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolPaymentSummary")
            .pageLabel("Order Summary")
            .complex(CaseData::getApplication)
                .mandatory(Application::getApplicationFeeOrderSummary)
                .done()
            .label(
                "LabelSolPaySummaryFeeAccountPara-1",
                "Payment Method: Fee Account",
                "solPaymentHowToPay=\"feePayByAccount\"")
            .label(
                "LabelSolicitorReference",
                "Your fee account reference: **${feeAccountReference}**",
                "solPaymentHowToPay=\"feePayByAccount\"")
            .label(
                "LabelSolPaySummaryHWFPara-1",
                "Payment Method: Help with fees",
                "solPaymentHowToPay=\"feesHelpWith\"")
            .label(
                "LabelHelpWithFeesReferenceNumber",
                "Help with fee reference: **${applicant1HWFReferenceNumber}**",
                "solPaymentHowToPay=\"feesHelpWith\" AND applicationType=\"soleApplication\"")
            .label(
                "LabelHelpWithFeesReferenceNumber-Applicant1",
                "Applicant 1 Help with fee reference: **${applicant1HWFReferenceNumber}**",
                "solPaymentHowToPay=\"feesHelpWith\" AND applicationType=\"jointApplication\"")
            .label(
                "LabelHelpWithFeesReferenceNumber-Applicant2",
                "Applicant 2 Help with fee reference: **${applicant2HWFReferenceNumber}**",
                "solPaymentHowToPay=\"feesHelpWith\" "
                    + "AND applicationType=\"jointApplication\" "
                    + "AND applicant2HWFReferenceNumber=\"*\"");
    }
}
