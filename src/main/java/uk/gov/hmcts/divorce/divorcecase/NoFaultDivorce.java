package uk.gov.hmcts.divorce.divorcecase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RetiredFields;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static java.lang.System.getenv;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;

@Component
@Slf4j
public class NoFaultDivorce implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASE_TYPE = "NFD";
    public static final String CASE_TYPE_DESCRIPTION = "New Law Case";
    public static final String JURISDICTION = "DIVORCE";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.addPreEventHook(RetiredFields::migrate);
        configBuilder.setCallbackHost(getenv().getOrDefault("CASE_API_URL", "http://localhost:4013"));

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
        configBuilder.omitHistoryForRoles(APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR);

        // to shutter the service within xui uncomment this line
        // configBuilder.shutterService();
        log.info("Building definition for " + getenv().getOrDefault("ENVIRONMENT", ""));
    }
}
