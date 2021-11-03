package uk.gov.hmcts.divorce.bulkaction.ccd.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

@Component
public class BulkActionCaseTypeTab implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        configBuilder.tab("bulkCaseList", "Bulk case list")
            .field(BulkActionCaseData::getCaseTitle)
            .field(BulkActionCaseData::getCourtName)
            .field(BulkActionCaseData::getDateAndTimeOfHearing)
            .field(BulkActionCaseData::getPronouncementJudge)
            .field(BulkActionCaseData::getHasJudgePronounced)
            .field(BulkActionCaseData::getPronouncedDate)
            .field(BulkActionCaseData::getDateFinalOrderEligibleFrom)
            .field(BulkActionCaseData::getBulkListCaseDetails);

        configBuilder.tab("unprocessedBulkCaseList", "Unprocessed bulk case list")
            .showCondition("erroredCaseDetails=\"*\"")
            .field(BulkActionCaseData::getErroredCaseDetails);
    }
}
