package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.MarriageDetails;

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
                "LabelNFDBanner-AboutApplicant1",
                SOLICITOR_NFD_PREVIEW_BANNER,
                SOLE_APPLICATION_CONDITION)
            .label(
                "LabelNFJointDBanner-AboutApplicant1",
                SOLICITOR_NFD_JOINT_PREVIEW_BANNER,
                JOINT_APPLICATION_CONDITION)
            .mandatory(CaseData::getApplicant1FirstName)
            .optional(CaseData::getApplicant1MiddleName)
            .mandatory(CaseData::getApplicant1LastName)
            .mandatory(CaseData::getApplicant1NameDifferentToMarriageCertificate)
            .label(
                "LabelSolAboutApp1Para-2",
                "About Applicant 1",
                "applicant1NameDifferentToMarriageCertificate=\"Yes\"")
            .mandatory(CaseData::getApplicant1NameChangedHow, "applicant1NameDifferentToMarriageCertificate=\"Yes\"")
            .mandatory(CaseData::getApplicant1NameChangedHowOtherDetails, "applicant1NameChangedHow=\"other\"")
            .mandatory(CaseData::getDivorceWho)
            .mandatory(CaseData::getInferredApplicant1Gender)
            .complex(CaseData::getMarriageDetails)
                .mandatory(MarriageDetails::getIsSameSexCouple)
                .done()
            .mandatory(CaseData::getApplicant1HomeAddress)
            .optional(CaseData::getApplicant1PhoneNumber)
            .optional(CaseData::getApplicant1Email)
            .mandatory(CaseData::getApplicant1ContactDetailsConfidential);
    }
}
