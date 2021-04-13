package uk.gov.hmcts.divorce.api.ccd.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static uk.gov.hmcts.divorce.api.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.api.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.api.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.divorce.api.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.divorce.api.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.divorce.api.ccd.model.UserRole.CITIZEN;

public class DefaultAccess implements HasAccessControl {
    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(CITIZEN, Permissions.CREATE_READ_UPDATE);
        grants.putAll(CASEWORKER_DIVORCE_COURTADMIN_BETA, Permissions.READ);
        grants.putAll(CASEWORKER_DIVORCE_COURTADMIN, Permissions.READ);
        grants.putAll(CASEWORKER_DIVORCE_SOLICITOR, Permissions.READ);
        grants.putAll(CASEWORKER_DIVORCE_SUPERUSER, Permissions.READ);
        grants.putAll(CASEWORKER_DIVORCE_COURTADMIN_LA, Permissions.READ);

        return grants;
    }
}
