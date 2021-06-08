package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.Applicant;
import uk.gov.hmcts.divorce.common.model.CaseData;

import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.JOINT_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLE_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_JOINT_PREVIEW_BANNER;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

public class SolAboutApplicant2 implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutApplicant2")
            .pageLabel("About applicant 2")
            .label(
                "LabelNFDBanner-AboutApplicant2",
                SOLICITOR_NFD_PREVIEW_BANNER,
                SOLE_APPLICATION_CONDITION)
            .label(
                "LabelNFDJointBanner-AboutApplicant2",
                SOLICITOR_NFD_JOINT_PREVIEW_BANNER,
                JOINT_APPLICATION_CONDITION)
            .complex(CaseData::getApplicant2)
                .mandatory(Applicant::getFirstName,
                    null,
                    null,
                    "Applicant 2's first name")
                .optional(Applicant::getMiddleName,
                    null,
                    null,
                    "Applicant 2's middle name")
                .mandatory(Applicant::getLastName,
                    null,
                    null,
                    "Applicant 2's last name")
                .mandatory(Applicant::getNameDifferentToMarriageCertificate, null, null,
                    "Is applicant 2's name different to that on their marriage certificate?")
                .mandatory(
                    Applicant::getNameChangedHow,
                    "applicant2NameDifferentToMarriageCertificate=\"Yes\"", null,
                    "How did they change their name?")
                .mandatory(
                    Applicant::getNameChangedHowOtherDetails,
                    "applicant2NameChangedHow=\"other\"",
                    null,
                    "If not through marriage or deed poll, please provide details of how they legally changed they name")
                .mandatory(Applicant::getGender, null, null,
                        "What is applicant 2's gender?")
                .done()
            .complex(CaseData::getApplicant2)
                .optional(Applicant::getLanguagePreferenceWelsh, null, null,
                "If known, please specify if applicant 2 will want their document also in welsh",
                "An english copy will still be included");
    }
}
