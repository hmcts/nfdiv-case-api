package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

public class SolPayment implements CcdPageConfiguration {

    @Override
    public void addTo(
        final FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .page("SolPayment")
            //.midEventWebhook() TODO: Add mid event webhook to retrieve pba numbers
            .pageLabel("Payment")
            .label(
                "LabelSolPaymentPara-1",
                "Amount to pay: **£${solApplicationFeeInPounds}**")
            .mandatory(CaseData::getSolPaymentHowToPay);
    }
}
