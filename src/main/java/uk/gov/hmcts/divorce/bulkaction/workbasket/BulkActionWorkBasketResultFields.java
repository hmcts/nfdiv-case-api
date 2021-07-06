package uk.gov.hmcts.divorce.bulkaction.workbasket;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

@Component
public class BulkActionWorkBasketResultFields implements CCDConfig<BulkActionCaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, State, UserRole> configBuilder) {
        configBuilder
            .workBasketResultFields()
            .caseReferenceField();
    }
}
