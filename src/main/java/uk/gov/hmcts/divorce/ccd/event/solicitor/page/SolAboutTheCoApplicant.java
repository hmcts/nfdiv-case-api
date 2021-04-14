package uk.gov.hmcts.divorce.ccd.event.solicitor.page;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

public class SolAboutTheCoApplicant implements CcdPageConfiguration {

    @Override
    public void addTo(FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .page("SolAboutTheCoApplicant")
            .pageLabel("About the co-applicant")
            .label(
                "LabelSolAboutEditingApplication-AboutCoApplicant",
                "You can make changes at the end of your application.")
            .mandatory(CaseData::getCoApplicantFirstName)
            .mandatory(CaseData::getCoApplicantLastName)
            .mandatory(CaseData::getCoApplicantNameAsOnMarriageCertificate)
            .optional(
                CaseData::getCoApplicantNameDifferentToMarriageCertExplain,
                "coApplicantNameAsOnMarriageCertificate=\"Yes\"")
            .mandatory(CaseData::getInferredCoApplicantGender);
    }
}
