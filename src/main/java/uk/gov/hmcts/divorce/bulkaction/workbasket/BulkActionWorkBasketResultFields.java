package uk.gov.hmcts.divorce.bulkaction.workbasket;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

@Component
public class BulkActionWorkBasketResultFields implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        configBuilder
            .workBasketResultFields()
            .caseReferenceField();
    }
}
