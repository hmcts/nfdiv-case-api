package uk.gov.hmcts.divorce.ccd.event.solicitor.page;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

public class SolAboutTheApplicant implements CcdPageConfiguration {

    @Override
    public void addTo(final FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .page("SolAboutTheApplicant")
            .pageLabel("About the applicant")
            .label(
                "LabelSolAboutEditingApplication-AboutApplicant",
                "You can make changes at the end of your application.")
            .mandatory(CaseData::getApplicantFirstName)
            .mandatory(CaseData::getApplicantLastName)
            .mandatory(CaseData::getApplicantNameDifferentToMarriageCertificate)
            .label(
                "LabelSolAboutThePetPara-2",
                "About the applicant",
                "applicantNameDifferentToMarriageCertificate=\"Yes\"")
            .mandatory(CaseData::getApplicantNameChangedHow, "applicantNameDifferentToMarriageCertificate=\"Yes\"")
            .mandatory(CaseData::getApplicantNameChangedHowOtherDetails, "applicantNameChangedHow=\"other\"")
            .mandatory(CaseData::getDivorceWho)
            .mandatory(CaseData::getInferredApplicantGender)
            .mandatory(CaseData::getMarriageIsSameSexCouple)
            .mandatory(CaseData::getDerivedApplicantHomeAddress)
            .optional(CaseData::getApplicantPhoneNumber)
            .optional(CaseData::getApplicantEmail)
            .mandatory(CaseData::getApplicantContactDetailsConfidential);
    }
}
