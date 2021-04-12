package uk.gov.hmcts.reform.divorce.ccd.event;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static uk.gov.hmcts.reform.divorce.ccd.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.reform.divorce.ccd.access.Permissions.READ;
import static uk.gov.hmcts.reform.divorce.ccd.model.State.Draft;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CITIZEN;

public class PatchCase implements CcdConfiguration {

    public static final String PATCH_CASE = "patch-case";

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(PATCH_CASE)
            .forState(Draft)
            .name("Patch case")
            .description("Patch a divorce or dissolution")
            .displayOrder(2)
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .grant(READ,
                CASEWORKER_DIVORCE_COURTADMIN_BETA,
                CASEWORKER_DIVORCE_COURTADMIN,
                CASEWORKER_DIVORCE_SOLICITOR,
                CASEWORKER_DIVORCE_SUPERUSER,
                CASEWORKER_DIVORCE_COURTADMIN_LA);
    }
}
