package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.Applicant;
import uk.gov.hmcts.divorce.common.model.Application;
import uk.gov.hmcts.divorce.common.model.CaseData;

public class Applicant2SolUpdateAosApplicant1Application implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolUpdateAosApplicant1Application")
            .pageLabel("Review the applicant 1's application")
            .complex(CaseData::getApplication)
                .readonly(Application::getMiniApplicationLink)
                .done()
            .complex(CaseData::getApplicant2)
            .mandatory(Applicant::getConfirmReadPetition)
            .label("LabelRespSol-AOSConfirmRead",
                "### The respondent has not read the petition\n\n"
                    + "The respondent must have read the petition in order to respond.",
              "applicant2ConfirmReadPetition=\"No\"")
            .done();
    }
}
