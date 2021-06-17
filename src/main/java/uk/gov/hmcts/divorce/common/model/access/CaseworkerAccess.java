package uk.gov.hmcts.divorce.common.model.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.UserRole.CITIZEN;

public class CaseworkerAccess implements HasAccessControl {

    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(CITIZEN, Permissions.READ);
        grants.putAll(CASEWORKER_DIVORCE_SOLICITOR, Permissions.READ);
        grants.putAll(CASEWORKER_DIVORCE_SUPERUSER, Permissions.READ);

        grants.putAll(CASEWORKER_DIVORCE_COURTADMIN_BETA, Permissions.CREATE_READ_UPDATE);
        grants.putAll(CASEWORKER_DIVORCE_COURTADMIN, Permissions.CREATE_READ_UPDATE);
        grants.putAll(CASEWORKER_DIVORCE_COURTADMIN_LA, Permissions.CREATE_READ_UPDATE);

        return grants;
    }
}
