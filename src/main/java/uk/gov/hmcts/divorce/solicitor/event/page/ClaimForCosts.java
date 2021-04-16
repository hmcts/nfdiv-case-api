package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

public class ClaimForCosts implements CcdPageConfiguration {

    @Override
    public void addTo(final FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .page("ClaimForCosts")
            .pageLabel("Claim for costs")
            .label(
                "claimForCostsEditMessage",
                "You can make changes at the end of your application.")
            .label(
                "LabelClaimForCostsPara-1",
                "A claim for costs can include all the fees the petitioner has to pay during the divorce, such as "
                    + "application fees, solicitor fees and any extra court fees.")
            .mandatory(CaseData::getDivorceCostsClaim);
    }
}
