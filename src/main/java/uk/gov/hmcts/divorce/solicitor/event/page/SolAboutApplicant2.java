package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class SolAboutApplicant2 implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutApplicant2")
            .pageLabel("About the other party")
            .complex(CaseData::getApplicant2)
                .mandatoryWithLabel(Applicant::getFirstName,
                    "${labelContentTheApplicant2UC}'s first name")
                .optionalWithLabel(Applicant::getMiddleName,
                    "${labelContentTheApplicant2UC}'s middle name")
                .mandatoryWithLabel(Applicant::getLastName,
                    "${labelContentTheApplicant2UC}'s last name")
                .mandatoryWithLabel(Applicant::getNameDifferentToMarriageCertificate,
                    "Is ${labelContentTheApplicant2}'s name different to that on their marriage certificate?")
                .mandatoryWithoutDefaultValue(
                    Applicant::getNameChangedHow,
                    "applicant2NameDifferentToMarriageCertificate=\"Yes\"",
                    "How did they change their name?")
                .mandatoryWithoutDefaultValue(
                    Applicant::getNameChangedHowOtherDetails,
                    "applicant2NameChangedHow=\"other\"",
                    "If not through marriage or deed poll, please provide details of how they legally changed they name")
                .mandatoryWithLabel(Applicant::getGender,
                        "What is ${labelContentTheApplicant2}'s gender?")
                .done();
    }
}
