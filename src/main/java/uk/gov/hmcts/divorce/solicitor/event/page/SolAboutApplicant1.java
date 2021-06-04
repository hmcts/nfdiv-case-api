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
            .pageLabel("About applicant 1")
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
                    "Applicant 1's first name")
                .optional(Applicant::getMiddleName,
                    null,
                    null,
                    "Applicant 1's middle name")
                .mandatory(Applicant::getLastName,
                    null,
                    null,
                    "Applicant 1's last name")
                .mandatory(Applicant::getNameDifferentToMarriageCertificate,
                    null,
                    null,
                    "Is applicant 1's name different to that on their marriage certificate?")
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
                    "What is applicant 1's gender?")
                .done()
            .complex(CaseData::getMarriageDetails)
                .mandatory(MarriageDetails::getIsSameSexCouple)
                .done()
            .complex(CaseData::getApplicant1)
                .mandatory(Applicant::getEmail,
                    null,
                    null,
                    "Applicant 1's email address")
                .optional(Applicant::getPhoneNumber,
                    null,
                    null,
                    "Applicant 1's phone number")
                .optional(Applicant::getHomeAddress,
                    null,
                    null,
                    "Applicant 1's home address")
                .label("LabelHorizontalLine1-SolAboutApplicant1", DARK_HORIZONTAL_RULE)
                .mandatory(Applicant::getContactDetailsConfidential,
                    null,
                    null,
                    "Keep Applicant 1's contact details private from applicant 2?")
                .done();
    }
}
