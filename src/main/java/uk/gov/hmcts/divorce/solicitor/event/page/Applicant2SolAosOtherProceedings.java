package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.Applicant;
import uk.gov.hmcts.divorce.common.model.CaseData;

public class Applicant2SolAosOtherProceedings implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolAosOtherProceedings")
            .pageLabel("Are there any other legal proceedings outside of England and Wales?")
            .complex(CaseData::getApplicant2)
            .mandatory(Applicant::getLegalProceedingsExist)
            .mandatory(Applicant::getLegalProceedingsDescription, "applicant2LegalProceedingsExist=\"Yes\"")
            .done();
    }
}
