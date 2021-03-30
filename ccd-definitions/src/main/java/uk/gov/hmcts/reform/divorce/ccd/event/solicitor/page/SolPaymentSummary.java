package uk.gov.hmcts.reform.divorce.ccd.event.solicitor.page;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

public class SolPaymentSummary implements CcdPageConfiguration {

    @Override
    public void addTo(FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .page("SolPaymentSummary")
            .mandatory(CaseData::getSolApplicationFeeOrderSummary)
            .label(
                "LabelSolPaySummaryFeeAccountPara-1",
                "Payment Method: Fee Account",
                "SolPaymentHowToPay=\"feePayByAccount\"")
            .label(
                "LabelSolicitorReference",
                "Your fee account reference: **${FeeAccountReference}**",
                "SolPaymentHowToPay=\"feePayByAccount\"")
            .label(
                "LabelSolPaySummaryHWFPara-1",
                "Payment Method: Help with fees",
                "SolPaymentHowToPay=\"feesHelpWith\"")
            .label(
                "LabelHelpWithFeesReferenceNumber",
                "Help with fee reference: **${helpWithFeesReferenceNumber}**",
                "SolPaymentHowToPay=\"feesHelpWith\"");
    }
}
