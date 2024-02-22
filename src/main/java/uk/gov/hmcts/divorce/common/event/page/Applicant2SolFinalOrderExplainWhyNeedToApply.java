package uk.gov.hmcts.divorce.common.event.page;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

@Slf4j
public class Applicant2SolFinalOrderExplainWhyNeedToApply implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "FOExplainWhyNeedToApplyApp2StatementOfTruth=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolicitorWhyNeedToApply")
            .pageLabel("Finalising your divorce")
            .complex(CaseData::getFinalOrder)
                .readonlyNoSummary(FinalOrder::getApplicant2SolFinalOrderFeeInPounds, ALWAYS_HIDE)
            .done()
            .label(
                "FODescription",
                "If the applicant will not or cannot apply to finalise the divorce, then you can apply on behalf of the respondent instead."
            )
            .label(
                "FODescriptionFees",
                "There will be a fee of **£${applicant2SolFinalOrderFeeInPounds}**, unless the respondent is eligible for Help with Fees."
            )
            .label(
                "FORequestText",
                "*I want permission to apply for a final order on behalf of the respondent, and to finalise the divorce.*"
            )
            .complex(CaseData::getFinalOrder)
                .mandatory(FinalOrder::getApplicant2SolFinalOrderWhyNeedToApply)
            .done()
            .label(
                "FOExplainWhyNeedToApplyApp2StatementOfTruth",
                "The respondent believes that the facts stated in the application are true."
            )
            .complex(CaseData::getApplicant2)
                .complex(Applicant::getSolicitor)
                    .mandatory(Solicitor::getName)
                    .mandatory(Solicitor::getFirmName)
                .done()
            .done();
    }
}
