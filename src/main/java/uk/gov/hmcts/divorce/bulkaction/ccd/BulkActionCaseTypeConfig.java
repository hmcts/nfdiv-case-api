package uk.gov.hmcts.divorce.bulkaction.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkCaseRetiredFields;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static java.lang.System.getenv;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class BulkActionCaseTypeConfig implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    private static final String CASE_TYPE = "NO_FAULT_DIVORCE_BulkAction";
    public static final String CASE_TYPE_DESCRIPTION = "New Law Bulk Case";
    public static final String JURISDICTION = "DIVORCE";

    public static String getCaseType() {
        return ofNullable(getenv().get("CHANGE_ID"))
            .map(num -> CASE_TYPE + "-" + num)
            .orElse(CASE_TYPE);
    }

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        configBuilder.addPreEventHook(BulkCaseRetiredFields::migrate);
        configBuilder.setCallbackHost(System.getenv().getOrDefault("CASE_API_URL", "http://localhost:4013"));

        var caseTypeDescription = ofNullable(getenv().get("CHANGE_ID"))
            .map(num -> CASE_TYPE_DESCRIPTION + "-" + num)
            .orElse(CASE_TYPE_DESCRIPTION);

        configBuilder.caseType(getCaseType(), caseTypeDescription, "Handling of the dissolution of marriage");
        configBuilder.jurisdiction(JURISDICTION, "Family Divorce", "Family Divorce: dissolution of marriage");

        configBuilder.grant(Created, CREATE_READ_UPDATE, CASE_WORKER, SUPER_USER, SYSTEMUPDATE);
    }
}
