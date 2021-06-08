package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.Applicant;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.MarriageDetails;

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
                .mandatory(Applicant::getFirstName,
                    null,
                    null,
                    "The applicant's first name")
                .optional(Applicant::getMiddleName,
                    null,
                    null,
                    "The applicant's middle name")
                .mandatory(Applicant::getLastName,
                    null,
                    null,
                    "The applicant's last name")
                .mandatory(Applicant::getNameDifferentToMarriageCertificate,
                    null,
                    null,
                    "Is the applicant's name different to that on their marriage certificate?")
                .mandatory(Applicant::getNameChangedHow,
                    "applicant1NameDifferentToMarriageCertificate=\"Yes\"",
                    null,
                    "How did they change their name?")
                .mandatory(Applicant::getNameChangedHowOtherDetails,
                    "applicant1NameChangedHow=\"other\"",
                    null,
                    "If not through marriage or deed poll, please provide details of how they legally changed they name")
                .done()
            .mandatory(CaseData::getDivorceWho,
                null,
                null,
                null,
                "Their husband or wife")
            .complex(CaseData::getApplicant1)
                .mandatory(Applicant::getGender,
                    null,
                    null,
                    "What is the applicant's gender?")
                .done()
            .complex(CaseData::getMarriageDetails)
                .mandatory(MarriageDetails::getIsSameSexCouple)
                .done()
            .complex(CaseData::getApplicant1)
                .mandatory(Applicant::getEmail,
                    null,
                    null,
                    "The applicant's email address")
                .optional(Applicant::getPhoneNumber,
                    null,
                    null,
                    "The applicant's phone number")
                .optional(Applicant::getHomeAddress,
                    null,
                    null,
                    "The applicant's home address")
                .label("LabelHorizontalLine1-SolAboutApplicant1", DARK_HORIZONTAL_RULE)
                .mandatory(Applicant::getContactDetailsConfidential,
                    null,
                    null,
                    "Keep the applicant's contact details private from applicant 2?")
                .done();
    }
}
