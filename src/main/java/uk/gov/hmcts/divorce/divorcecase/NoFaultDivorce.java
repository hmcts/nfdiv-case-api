package uk.gov.hmcts.divorce.divorcecase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.AddSystemUpdateRole;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class NoFaultDivorce implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASE_TYPE = "NFD";
    public static final String JURISDICTION = "DIVORCE";

    @Autowired
    private AddSystemUpdateRole addSystemUpdateRole;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.setCallbackHost(System.getenv().getOrDefault("CASE_API_URL", "http://nfdiv-case-api:4013"));

        configBuilder.caseType(CASE_TYPE, CASE_TYPE, "Handling of the dissolution of marriage");
        configBuilder.jurisdiction(JURISDICTION, "Family Divorce", "Family Divorce: dissolution of marriage");
        configBuilder.omitHistoryForRoles(SOLICITOR, APPLICANT_2_SOLICITOR);

        configBuilder.grant(Draft, CREATE_READ_UPDATE, CITIZEN);
        configBuilder.grant(Draft, READ, CASEWORKER_COURTADMIN_CTSC);
        configBuilder.grant(Draft, READ, CASEWORKER_COURTADMIN_RDU);
        configBuilder.grant(Draft, CREATE_READ_UPDATE, SOLICITOR);
        configBuilder.grant(Draft, CREATE_READ_UPDATE, CASEWORKER_SUPERUSER);
        configBuilder.grant(Draft, READ, CASEWORKER_LEGAL_ADVISOR);

        String environment = System.getenv().getOrDefault("ENVIRONMENT", null);

        if (addSystemUpdateRole.isEnvironmentAat()) {
            configBuilder.grant(Draft, CREATE_READ_UPDATE, CASEWORKER_SYSTEMUPDATE);
        }
    }
}
