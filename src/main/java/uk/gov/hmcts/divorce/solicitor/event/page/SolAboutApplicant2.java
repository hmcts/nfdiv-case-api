package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.JOINT_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLE_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_JOINT_PREVIEW_BANNER;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

public class SolAboutApplicant2 implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutApplicant2")
            .pageLabel("About the respondent")
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
                    "The respondent's first name")
                .optional(Applicant::getMiddleName,
                    null,
                    null,
                    "The respondent's middle name")
                .mandatory(Applicant::getLastName,
                    null,
                    null,
                    "The respondent's last name")
                .mandatory(Applicant::getNameDifferentToMarriageCertificate, null, null,
                    "Is the respondent's name different to that on their marriage certificate?")
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
                        "What is the respondent's gender?")
                .done()
            .complex(CaseData::getApplicant2)
                .optional(Applicant::getLanguagePreferenceWelsh, null, null,
                "Include a Welsh copy of all generated divorce documents for the respondent?",
                "An English copy will still be included");
    }
}
