package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

public class SolPaymentSummary implements CcdPageConfiguration {

    @Override
    public void addTo(FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .page("SolPaymentSummary")
            .mandatory(CaseData::getSolApplicationFeeOrderSummary)
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
                "Help with fee reference: **${helpWithFeesReferenceNumber}**",
                "solPaymentHowToPay=\"feesHelpWith\"");
    }
}
