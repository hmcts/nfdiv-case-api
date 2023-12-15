package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;

public class SolFinalOrderPaymentSummary implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "applicant2SolFinalOrderFeeOrderSummary=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolFinalOrderPaymentSummary")
            .pageLabel("Order Summary")
            .complex(CaseData::getFinalOrder)
                .mandatory(FinalOrder::getApplicant2SolFinalOrderFeeOrderSummary)
                .complex(FinalOrder::getApplicant2SolFinalOrderHelpWithFees)
                    .readonlyNoSummary(HelpWithFees::getReferenceNumber, ALWAYS_HIDE)
                .done()
            .done()
            .label(
                "LabelSolPaySummaryFeeAccountPara-1",
                "Payment Method: Fee Account",
                "applicant2SolPaymentHowToPay=\"feePayByAccount\"")
            .label(
                "LabelSolicitorReference",
                "Your fee account reference: **${feeAccountReference}**",
                "applicant2SolPaymentHowToPay=\"feePayByAccount\"")
            .label(
                "LabelSolPaySummaryHWFPara-1",
                "Payment Method: Help with fees",
                "applicant2SolPaymentHowToPay=\"feesHelpWith\"")
            .label(
                "LabelHelpWithFeesReferenceNumber",
                "Help with fee reference: **${app2SolFoHWFReferenceNumber}**",
                "applicant2SolPaymentHowToPay=\"feesHelpWith\"")
            .done();
    }
}
