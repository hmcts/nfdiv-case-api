package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

public class OtherLegalProceedings implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("OtherLegalProceedings")
            .pageLabel("Other legal proceedings")
            .label(
                "legalProceedingsEditMessage",
                "You can make changes at the end of your application.")
            .mandatory(CaseData::getLegalProceedings)
            .mandatory(
                CaseData::getLegalProceedingsDetails,
                "legalProceedings=\"Yes\""
            );
    }
}
