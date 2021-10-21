package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

public class Applicant2SolUpdateAosApplicant1Application implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "miniApplicationLink=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder<CaseData, UserRole, State> pageBuilder) {
        pageBuilder
            .page("Applicant2SolUpdateAosApplicant1Application")
            .pageLabel("Review the applicant 1's application")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getTheApplicant2, ALWAYS_HIDE)
                .done()
            .complex(CaseData::getApplication)
                .readonly(Application::getMiniApplicationLink)
                .done()
            .complex(CaseData::getAcknowledgementOfService)
                .mandatory(AcknowledgementOfService::getConfirmReadPetition)
                .label("LabelRespSol-AOSConfirmRead",
                    "### ${labelContentTheApplicant2} has not read the petition\n\n"
                        + "${labelContentTheApplicant2} must have read the petition in order to respond.",
                    "confirmReadPetition=\"No\"")
                .done();
    }
}
