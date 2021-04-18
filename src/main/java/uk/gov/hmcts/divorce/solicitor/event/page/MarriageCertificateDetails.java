package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

public class MarriageCertificateDetails implements CcdPageConfiguration {
    @Override
    public void addTo(FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {
        fieldCollectionBuilder
            .page("MarriageCertificateDetails")
            .pageLabel("Marriage certificate details")
            .label(
                "marriageCertDetailsEditMessage",
                "You can make changes at the end of your application.")
            .mandatory(CaseData::getMarriageDate)
            .mandatory(CaseData::getMarriagePetitionerName)
            .mandatory(CaseData::getMarriageRespondentName)
            .mandatory(CaseData::getMarriedInUk)
            .mandatory(
                CaseData::getMarriagePlaceOfMarriage,
                "marriedInUk=\"No\""
            )
            .mandatory(
                CaseData::getCountryName,
                "marriedInUk=\"No\""
            );
    }
}
