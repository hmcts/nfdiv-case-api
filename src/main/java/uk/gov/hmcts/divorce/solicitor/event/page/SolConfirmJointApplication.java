package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class SolConfirmJointApplication implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("ConfirmJointApplication")
            .pageLabel("Confirm Joint Application")
            .showCondition("applicationType=\"jointApplication\"")
            .complex(CaseData::getApplication)
            .readonly(Application::getApplicant2SolicitorAnswersLink)
            .done();
    }
}
