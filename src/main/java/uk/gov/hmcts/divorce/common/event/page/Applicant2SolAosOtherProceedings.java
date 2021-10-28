package uk.gov.hmcts.divorce.common.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class Applicant2SolAosOtherProceedings implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolAosOtherProceedings")
            .pageLabel("Are there any other legal proceedings outside of England and Wales?")
            .complex(CaseData::getApplicant2)
            .mandatory(Applicant::getLegalProceedings)
            .mandatory(Applicant::getLegalProceedingsDetails, "applicant2LegalProceedings=\"Yes\"")
            .done();
    }
}
