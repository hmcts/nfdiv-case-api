package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

public class SolAboutThePetitioner implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutThePetitioner")
            .pageLabel("About the petitioner")
            .label(
                "LabelSolAboutEditingApplication-AboutPetitioner",
                "You can make changes at the end of your application.")
            .mandatory(CaseData::getPetitionerFirstName)
            .optional(CaseData::getPetitionerMiddleName)
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
