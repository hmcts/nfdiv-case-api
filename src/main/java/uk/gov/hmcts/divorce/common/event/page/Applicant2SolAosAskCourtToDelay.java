package uk.gov.hmcts.divorce.common.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;

public class Applicant2SolAosAskCourtToDelay implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "intendToDelay=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolAosAskCourtToDelay")
            .pageLabel("Intend to delay")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getUnionType, ALWAYS_HIDE)
            .done()
            .complex(CaseData::getAcknowledgementOfService)
            .mandatory(AcknowledgementOfService::getIntendToDelay)
            .done();
    }
}
