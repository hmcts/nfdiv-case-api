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
                    "${labelContentApplicant2UC}'s first name")
                .optional(Applicant::getMiddleName,
                    null,
                    null,
                    "${labelContentApplicant2UC}'s middle name",
                    "If they have a middle name then you must enter it to avoid amendments later."
                    )
                .mandatoryWithLabel(Applicant::getLastName,
                    "${labelContentApplicant2UC}'s last name")
                .mandatory(Applicant::getNameDifferentToMarriageCertificate,
                    null,
                    null,
                    "Is ${labelContentTheApplicant2}'s name different from the name on the "
                        + "${labelContentMarriageOrCivilPartnership} certificate?",
                    "")
                .mandatoryWithoutDefaultValue(
                    Applicant::getNameChangedHow,
                    "applicant2NameDifferentToMarriageCertificate=\"Yes\"",
                    "What evidence will be provided for the name change?")
                .mandatoryWithoutDefaultValue(
                    Applicant::getNameChangedHowOtherDetails,
                    "applicant2NameChangedHow=\"other\"",
                    "If not through marriage or deed poll, please provide details of how they legally changed they name")
                .optionalWithLabel(Applicant::getGender,
                        "Is ${labelContentTheApplicant2} male or female?")
                .mandatory(
                    Applicant::getDivorceWho,
                    null,
                    null,
                    "What is ${labelContentTheApplicant2}"
                )
                .done();
    }
}
