package uk.gov.hmcts.divorce.ccd.event.solicitor.page;

import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

public class OtherLegalProceedings implements CcdPageConfiguration {
    @Override
    public void addTo(FieldCollection.FieldCollectionBuilder<CaseData, Event.EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .page("OtherLegalProceedings")
            .pageLabel("Other legal proceedings")
            .label(
                "legalProceedingsEditMessage",
                "You can make changes at the end of your application.")
            .mandatory(CaseData::getLegalProceedings)
            .mandatory(
                CaseData::getLegalProceedingsDetails,
                "legalProceedings=\"Yes\""
            );
    }
}
