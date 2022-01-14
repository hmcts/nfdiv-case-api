package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class CheckApplicant1SolicitorAnswers implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("checkTheirAnswers")
            .pageLabel("Check their answers")
            .complex(CaseData::getApplication)
                .mandatory(Application::getApplicant1SolicitorAnswersLink)
                .mandatoryWithLabel(
                    Application::getApplicant2ConfirmApplicant1Information,
                    "Are any corrections needed to the answers applicant 1 has provided?")
            .done()
            .page("additionalInformation")
            .showCondition("applicant2ConfirmApplicant1Information=\"Yes\"")
            .pageLabel("Additional information")
            .complex(CaseData::getApplication)
                .mandatoryWithLabel(Application::getApplicant2ExplainsApplicant1IncorrectInformation,"Additional information")
            .done();
    }
}
