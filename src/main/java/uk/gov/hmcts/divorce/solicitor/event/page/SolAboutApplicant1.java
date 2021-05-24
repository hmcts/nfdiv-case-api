package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
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
            .mandatory(CaseData::getApplicant1FirstName)
            .optional(CaseData::getApplicant1MiddleName)
            .mandatory(CaseData::getApplicant1LastName)
            .mandatory(CaseData::getApplicant1NameDifferentToMarriageCertificate,
                null,
                null,
                "Is applicant 1's name different to that on their marriage certificate?")
            .mandatory(CaseData::getApplicant1NameChangedHow,
                "applicant1NameDifferentToMarriageCertificate=\"Yes\"",
                null,
                "How did they change their name?")
            .mandatory(CaseData::getApplicant1NameChangedHowOtherDetails,
                "applicant1NameChangedHow=\"other\"",
                null,
                "If not through marriage or deed poll, please provide details of how they legally changed they name")
            .mandatory(CaseData::getDivorceWho,
                null,
                null,
                null,
                "Their husband or wife")
            .mandatory(CaseData::getInferredApplicant1Gender,
                null,
                null,
                "What is applicant 1's gender?")
            .complex(CaseData::getMarriageDetails)
                .mandatory(MarriageDetails::getIsSameSexCouple)
                .done()
            .mandatory(CaseData::getApplicant1Email)
            .optional(CaseData::getApplicant1PhoneNumber)
            .optional(CaseData::getApplicant1HomeAddress)
            .label("LabelHorizontalLine1-SolAboutApplicant1", DARK_HORIZONTAL_RULE)
            .mandatory(CaseData::getApplicant1ContactDetailsConfidential);
    }
}
