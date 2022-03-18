package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;

public class SolPaymentSummary implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "applicationFeeOrderSummary=\"ALWAYS_HIDE\"";

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
            .complex(CaseData::getApplication)
                .complex(Application::getApplicant2HelpWithFees)
                .readonlyNoSummary(HelpWithFees::getReferenceNumber, ALWAYS_HIDE)
                .done()
            .done()
            .label(
                "LabelHelpWithFeesReferenceNumber-Applicant2",
                "Applicant 2 Help with fee reference: **${applicant2HWFReferenceNumber}**",
                "solPaymentHowToPay=\"feesHelpWith\" AND applicationType=\"jointApplication\"");
    }
}
