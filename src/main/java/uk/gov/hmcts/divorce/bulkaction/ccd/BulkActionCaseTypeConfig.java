package uk.gov.hmcts.divorce.bulkaction.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.ScheduledForCreate;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class BulkActionCaseTypeConfig implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String CASE_TYPE = "NO_FAULT_DIVORCE_BulkAction";
    public static final String JURISDICTION = "DIVORCE";

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        configBuilder.setCallbackHost(System.getenv().getOrDefault("CASE_API_URL", "http://nfdiv-case-api:4013"));

        configBuilder.caseType(CASE_TYPE, CASE_TYPE, "Handling of the dissolution of marriage");
        configBuilder.jurisdiction(JURISDICTION, "Family Divorce", "Family Divorce: dissolution of marriage");

        configBuilder.grant(ScheduledForCreate, CREATE_READ_UPDATE, CITIZEN);
    }
}
