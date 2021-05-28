package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
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
            .mandatory(CaseData::getApplicant2FirstName)
            .optional(CaseData::getApplicant2MiddleName)
            .mandatory(CaseData::getApplicant2LastName)
            .mandatory(CaseData::getApplicant2NameDifferentToMarriageCertificate, null, null,
                "Is applicant 2's name different to that on their marriage certificate?")
            .mandatory(
                CaseData::getApplicant2NameChangedHow,
                "applicant2NameDifferentToMarriageCertificate=\"Yes\"", null,
                "How did they change their name?")
            .mandatory(
                CaseData::getApplicant2NameChangedHowOtherDetails,
                "applicant2NameChangedHow=\"other\"",
                null,
                "If not through marriage or deed poll, please provide details of how they legally changed they name")
            .mandatory(CaseData::getInferredApplicant2Gender, null, null,
                "What is applicant 2's gender?")
            .optional(CaseData::getApplicant2WelshLanguagePreference, null, null,
                "If known, please specify if applicant 2 will want their document also in welsh",
                "An english copy will still be included");
    }
}
