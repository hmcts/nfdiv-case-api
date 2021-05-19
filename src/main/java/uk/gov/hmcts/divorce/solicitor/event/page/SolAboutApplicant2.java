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
            .mandatory(CaseData::getApplicant2NameAsOnMarriageCertificate)
            .optional(
                CaseData::getApp2NameDifferentToMarriageCertExplain,
                "applicant2NameAsOnMarriageCertificate=\"Yes\"")
            .mandatory(CaseData::getInferredApplicant2Gender);
    }
}
