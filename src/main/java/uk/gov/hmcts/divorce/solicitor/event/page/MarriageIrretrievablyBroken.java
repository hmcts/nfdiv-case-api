package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

public class MarriageIrretrievablyBroken implements CcdPageConfiguration {
    @Override
    public void addTo(
        final FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder
    ) {

        fieldCollectionBuilder
            .page("MarriageIrretrievablyBroken")
            .pageLabel("Has the marriage irretrievably broken down (it cannot be saved)?")
            .label(
                "marriageIrretrievablyBrokenPara-1",
                "The marriage must have irretrievably broken down for the petitioner to get a divorce. "
                    + "This means it cannot be saved.")
            .mandatory(CaseData::getScreenHasMarriageBroken)
            .label(
                "MarriageNotIrretrievablyBroken",
                "The marriage must have irretrievably broken down for the petitioner to get a divorce. "
                    + "This is the law in England and Wales.",
                "screenHasMarriageBroken=\"No\""
            );
    }
}
