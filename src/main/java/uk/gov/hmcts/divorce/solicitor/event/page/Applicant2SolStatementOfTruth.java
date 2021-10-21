package uk.gov.hmcts.divorce.solicitor.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

@Component
public class Applicant2SolStatementOfTruth implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder<CaseData, UserRole, State> pageBuilder) {

        pageBuilder
            .page("Applicant2SolStatementOfTruth")
            .pageLabel("Statement of truth and reconciliation")
            .label("LabelApplicant2SolStatementOfTruth-AoSReview", "### Review the answers in your Acknowledgement "
                + "of Service below. If you wish to change any of your answers, please go back and use the 'Update AoS' action")
            .complex(CaseData::getAcknowledgementOfService)
                .readonlyNoSummary(AcknowledgementOfService::getConfirmReadPetition)
                .readonlyNoSummary(AcknowledgementOfService::getJurisdictionAgree)
                .readonlyNoSummary(AcknowledgementOfService::getJurisdictionDisagreeReason)
                .readonlyNoSummary(AcknowledgementOfService::getLegalProceedingsExist)
                .readonlyNoSummary(AcknowledgementOfService::getLegalProceedingsDescription)
                .label("LabelApplicant2SolStatementOfTruth-SOT", "## Statement of truth")
                .mandatory(AcknowledgementOfService::getStatementOfTruth)
                .label("LabelApplicant2SolStatementOfTruth-Prayer", "## Prayer")
                .mandatory(AcknowledgementOfService::getPrayerHasBeenGiven)
                .done();
    }
}
