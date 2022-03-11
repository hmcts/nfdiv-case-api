package uk.gov.hmcts.divorce.divorcecase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RetiredFields;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;

@Component
public class NoFaultDivorce implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASE_TYPE = "NFD";
    public static final String JURISDICTION = "DIVORCE";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.addPreEventHook(RetiredFields::migrate);
        configBuilder.setCallbackHost(System.getenv().getOrDefault("CASE_API_URL", "http://localhost:4013"));

        configBuilder.caseType(CASE_TYPE, "New Law Case", "Handling of the dissolution of marriage");
        configBuilder.jurisdiction(JURISDICTION, "Family Divorce", "Family Divorce: dissolution of marriage");
        configBuilder.omitHistoryForRoles(APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR);
    }
}
