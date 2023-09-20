package uk.gov.hmcts.divorce.bulkaction.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkCaseRetiredFields;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Optional;

import static java.lang.System.getenv;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class BulkActionCaseTypeConfig implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String CASE_TYPE = "NO_FAULT_DIVORCE_BulkAction";
    public static final String CASE_TYPE_DESCRIPTION = "New Law Bulk Case";
    public static final String JURISDICTION = "DIVORCE";

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        configBuilder.addPreEventHook(BulkCaseRetiredFields::migrate);
        configBuilder.setCallbackHost(System.getenv().getOrDefault("CASE_API_URL", "http://localhost:4013"));

        var prNumber = ofNullable(getenv().get("SERVICE_NAME"))
            .map(serviceName -> serviceName.replaceAll("[^0-9]", ""))
            .or(() -> ofNullable(getenv().get("CHANGE_ID")));

        var caseType = prNumber
            .map(num -> CASE_TYPE + "_PR_" + num)
            .orElse(CASE_TYPE);

        var caseTypeDescription = prNumber
            .map(num -> CASE_TYPE_DESCRIPTION + "_PR_" + num)
            .orElse(CASE_TYPE_DESCRIPTION);

        configBuilder.caseType(caseType, caseTypeDescription, "Handling of the dissolution of marriage");
        configBuilder.jurisdiction(JURISDICTION, "Family Divorce", "Family Divorce: dissolution of marriage");

        configBuilder.grant(Created, CREATE_READ_UPDATE, CASE_WORKER, SUPER_USER, SYSTEMUPDATE);
    }
}
