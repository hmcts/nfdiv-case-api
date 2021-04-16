package uk.gov.hmcts.divorce.ccd.event.solicitor.page;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

public class SolAboutThePetitioner implements CcdPageConfiguration {

    @Override
    public void addTo(final FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .page("SolAboutThePetitioner")
            .pageLabel("About the petitioner")
            .label(
                "LabelSolAboutEditingApplication-AboutPetitioner",
                "You can make changes at the end of your application.")
            .mandatory(CaseData::getPetitionerFirstName)
            .mandatory(CaseData::getPetitionerLastName)
            .mandatory(CaseData::getPetitionerNameDifferentToMarriageCertificate)
            .label(
                "LabelSolAboutThePetPara-2",
                "About the petitioner",
                "petitionerNameDifferentToMarriageCertificate=\"Yes\"")
            .mandatory(CaseData::getPetitionerNameChangedHow, "petitionerNameDifferentToMarriageCertificate=\"Yes\"")
            .mandatory(CaseData::getPetitionerNameChangedHowOtherDetails, "petitionerNameChangedHow=\"other\"")
            .mandatory(CaseData::getDivorceWho)
            .mandatory(CaseData::getInferredPetitionerGender)
            .mandatory(CaseData::getMarriageIsSameSexCouple)
            .mandatory(CaseData::getDerivedPetitionerHomeAddress)
            .optional(CaseData::getPetitionerPhoneNumber)
            .optional(CaseData::getPetitionerEmail)
            .mandatory(CaseData::getPetitionerContactDetailsConfidential);
    }
}
