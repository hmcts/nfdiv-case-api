package uk.gov.hmcts.reform.divorce.ccd.event.solicitor.page;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

public class SolAboutThePetitioner implements CcdPageConfiguration {

    @Override
    public void addTo(final FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .page("SolAboutThePetitioner")
            .pageLabel("About the petitioner")
            .label(
                "LabelSolAboutEditingApplication-AboutPetitioner",
                "You can make changes at the end of your application.")
            .mandatory(CaseData::getD8PetitionerFirstName)
            .mandatory(CaseData::getD8PetitionerLastName)
            .mandatory(CaseData::getD8PetitionerNameDifferentToMarriageCert)
            .label(
                "LabelSolAboutThePetPara-2",
                "About the petitioner",
                "D8PetitionerNameDifferentToMarriageCert=\"Yes\"")
            .mandatory(CaseData::getD8PetitionerNameChangedHow, "D8PetitionerNameDifferentToMarriageCert=\"Yes\"")
            .mandatory(CaseData::getD8PetitionerNameChangedHowOtherDetails, "D8PetitionerNameChangedHow=\"other\"")
            .mandatory(CaseData::getD8DivorceWho)
            .mandatory(CaseData::getD8InferredPetitionerGender)
            .mandatory(CaseData::getD8MarriageIsSameSexCouple)
            .mandatory(CaseData::getD8DerivedPetitionerHomeAddress)
            .optional(CaseData::getD8PetitionerPhoneNumber)
            .optional(CaseData::getD8PetitionerEmail)
            .mandatory(CaseData::getPetitionerContactDetailsConfidential);
    }
}
