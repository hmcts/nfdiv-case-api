package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

public class SolAboutApplicant2 implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutApplicant2")
            .pageLabel("About applicant 2")
            .mandatory(CaseData::getApplicant2FirstName)
            .optional(CaseData::getApplicant2MiddleName)
            .mandatory(CaseData::getApplicant2LastName)
            .mandatory(CaseData::getApplicant2NameDifferentToMarriageCertificate, null, null,
                "Is applicant 2's name different to that on their marriage certificate?")
            .mandatory(
                CaseData::getApplicant2NameChangedHow,
                "applicant2NameDifferentToMarriageCertificate=\"Yes\"", null,
                "How did they change their name?")
            .optional(
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
