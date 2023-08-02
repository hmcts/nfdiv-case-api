package uk.gov.hmcts.divorce.common.event.page;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

@Slf4j
public class Applicant2FinalOrderExplainTheDelay implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolicitorExplainTheDelay")
            .complex(CaseData::getFinalOrder)
            .showCondition("isFinalOrderOverdue=\"Yes\"")
                .mandatory(FinalOrder::getApplicant2FinalOrderLateExplanation)
                .done()
            .label(
                "StatementOfTruth",
                "The applicant believes that the facts stated in the application are true."
            )
            .complex(CaseData::getApplicant2)
                .complex(Applicant::getSolicitor)
                .mandatory(Solicitor::getName)
                .mandatory(Solicitor::getFirmName)
                .done();
    }
}
