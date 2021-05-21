package uk.gov.hmcts.divorce.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import static uk.gov.hmcts.divorce.common.model.State.Draft;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;

@Component
public class NoFaultDivorce implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASE_TYPE = "NO_FAULT_DIVORCE11";
    public static final String JURISDICTION = "DIVORCE";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.setCallbackHost(System.getenv().getOrDefault("CASE_API_URL", "http://nfdiv-case-api:4013"));

        configBuilder.caseType(CASE_TYPE, CASE_TYPE, "Handling of the dissolution of marriage");
        configBuilder.jurisdiction(JURISDICTION, "Family Divorce", "Family Divorce: dissolution of marriage");

        configBuilder.grant(Draft, CREATE_READ_UPDATE, CITIZEN);
        configBuilder.grant(Draft, READ, CASEWORKER_DIVORCE_COURTADMIN_BETA);
        configBuilder.grant(Draft, READ, CASEWORKER_DIVORCE_COURTADMIN);
        configBuilder.grant(Draft, CREATE_READ_UPDATE, CASEWORKER_DIVORCE_SOLICITOR);
        configBuilder.grant(Draft, CREATE_READ_UPDATE, CASEWORKER_DIVORCE_SUPERUSER);
        configBuilder.grant(Draft, READ, CASEWORKER_DIVORCE_COURTADMIN_LA);
    }
}
