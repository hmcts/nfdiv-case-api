package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;

import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.DARK_HORIZONTAL_RULE;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.JOINT_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLE_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_JOINT_PREVIEW_BANNER;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

public class SolAboutApplicant1 implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutApplicant1")
            .pageLabel("About the applicant")
            .label(
                "LabelNFDBanner-SolAboutApplicant1",
                SOLICITOR_NFD_PREVIEW_BANNER,
                SOLE_APPLICATION_CONDITION)
            .label(
                "LabelNFDJointBanner-SolAboutApplicant1",
                SOLICITOR_NFD_JOINT_PREVIEW_BANNER,
                JOINT_APPLICATION_CONDITION)
            .complex(CaseData::getApplicant1)
                .mandatoryWithLabel(Applicant::getFirstName,
                    "The applicant's first name")
                .optionalWithLabel(Applicant::getMiddleName,
                    "The applicant's middle name")
                .mandatoryWithLabel(Applicant::getLastName,
                    "The applicant's last name")
                .mandatoryWithLabel(Applicant::getNameDifferentToMarriageCertificate,
                    "Is the applicant's name different to that on their marriage certificate?")
                .mandatoryWithoutDefaultValue(Applicant::getNameChangedHow,
                    "applicant1NameDifferentToMarriageCertificate=\"Yes\"",
                    "How did they change their name?")
                .mandatoryWithoutDefaultValue(Applicant::getNameChangedHowOtherDetails,
                    "applicant1NameChangedHow=\"other\"",
                    "If not through marriage or deed poll, please provide details of how they legally changed they name")
                .done()
            .complex(CaseData::getApplication)
                .mandatoryWithLabel(Application::getDivorceWho,
                    "Their husband or wife")
                .done()
            .complex(CaseData::getApplicant1)
                .mandatoryWithLabel(Applicant::getGender,
                    "What is the applicant's gender?")
                .done()
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .mandatory(MarriageDetails::getIsSameSexCouple)
                    .done()
                .done()
            .complex(CaseData::getApplicant1)
                .mandatoryWithLabel(Applicant::getEmail,
                    "The applicant's email address")
                .optionalWithLabel(Applicant::getPhoneNumber,
                    "The applicant's phone number")
                .optionalWithLabel(Applicant::getHomeAddress,
                    "The applicant's home address")
                .label("LabelHorizontalLine1-SolAboutApplicant1", DARK_HORIZONTAL_RULE)
                .mandatoryWithLabel(Applicant::getContactDetailsConfidential,
                    "Keep the applicant's contact details private from ${labelContentTheApplicant2}?")
                .done();
    }
}
