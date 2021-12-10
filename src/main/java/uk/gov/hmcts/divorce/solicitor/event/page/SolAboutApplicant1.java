package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;

public class SolAboutApplicant1 implements CcdPageConfiguration {

    private static final String DARK_HORIZONTAL_RULE =
        "![Dark Rule](https://raw.githubusercontent.com/hmcts/nfdiv-case-api/master/resources/image/LabelDarkHorizontalRule.png)";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutApplicant1")
            .pageLabel("About the applicant")
            .complex(CaseData::getApplicant1)
                .mandatoryWithLabel(Applicant::getFirstName,
                    "${labelContentApplicantsOrApplicant1s} first name")
                .optionalWithLabel(Applicant::getMiddleName,
                    "${labelContentApplicantsOrApplicant1s} middle name")
                .mandatoryWithLabel(Applicant::getLastName,
                    "${labelContentApplicantsOrApplicant1s} last name")
                .mandatoryWithLabel(Applicant::getNameDifferentToMarriageCertificate,
                    "Is ${labelContentApplicantsOrApplicant1s} name different to that on the"
                        + " ${labelContentMarriageOrCivilPartnership} certificate?")
                .mandatoryWithoutDefaultValue(Applicant::getNameChangedHow,
                "applicant1NameDifferentToMarriageCertificate=\"Yes\"",
                "What evidence will be provided for the name change? ")
                .mandatoryWithoutDefaultValue(Applicant::getNameChangedHowOtherDetails,
                "applicant1NameChangedHow=\"other\"",
                "If not through marriage or deed poll, please provide details of how they legally changed they name")
                .optionalWithLabel(Applicant::getGender,
                "Is ${labelContentTheApplicantOrApplicant1} male or female?")
                .done()
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .mandatory(MarriageDetails::getFormationType)
                    .done()
                .done()
            .label("contactDetails", "# The ${labelContentApplicantsOrApplicant1s} contact details")
            .complex(CaseData::getApplicant1)
                .mandatoryWithLabel(Applicant::getEmail,
                    "${labelContentApplicantsOrApplicant1s} email address")
                .optionalWithLabel(Applicant::getPhoneNumber,
                    "${labelContentApplicantsOrApplicant1s} phone number")
                .mandatoryWithLabel(Applicant::getHomeAddress,
                    "${labelContentApplicantsOrApplicant1s} home address")
                .label("LabelHorizontalLine1-SolAboutApplicant1", DARK_HORIZONTAL_RULE)
                .mandatory(Applicant::getContactDetailsType)
                .done();
    }
}
