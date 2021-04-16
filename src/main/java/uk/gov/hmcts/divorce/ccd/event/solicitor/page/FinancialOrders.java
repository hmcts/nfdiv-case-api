package uk.gov.hmcts.divorce.ccd.event.solicitor.page;


import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

public class FinancialOrders implements CcdPageConfiguration {

    @Override
    public void addTo(final FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .page("FinancialOrders")
            .pageLabel("Financial orders")
            .label(
                "financialOrdersEditMessage",
                "You can make changes at the end of your application.")
            .mandatory(CaseData::getFinancialOrder)
            .mandatory(
                CaseData::getFinancialOrderFor,
                "financialOrder=\"Yes\"");
    }
}
