package uk.gov.hmcts.reform.divorce.ccd.ccdcase;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static uk.gov.hmcts.reform.divorce.ccd.model.Constants.CASE_TYPE;
import static uk.gov.hmcts.reform.divorce.ccd.model.Constants.JURISDICTION;
import static uk.gov.hmcts.reform.divorce.ccd.model.State.Draft;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CITIZEN;

public class NoFaultDivorce implements CcdConfiguration {

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder.caseType(CASE_TYPE, "No Fault Divorce case", "Handling of the dissolution of marriage");
        configBuilder.jurisdiction(JURISDICTION, "Family Divorce", "Family Divorce: dissolution of marriage");
        configBuilder.grant(Draft, "CRU", CITIZEN);
        configBuilder.grant(Draft, "R", CASEWORKER_DIVORCE_COURTADMIN_BETA);
        configBuilder.grant(Draft, "R", CASEWORKER_DIVORCE_COURTADMIN);
        configBuilder.grant(Draft, "R", CASEWORKER_DIVORCE_SOLICITOR);
        configBuilder.grant(Draft, "R", CASEWORKER_DIVORCE_SUPERUSER);
        configBuilder.grant(Draft, "R", CASEWORKER_DIVORCE_COURTADMIN_LA);
    }
}
