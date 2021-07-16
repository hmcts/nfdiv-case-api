package uk.gov.hmcts.divorce.divorcecase.model.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;

public class CaseworkerAccess implements HasAccessControl {

    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(CITIZEN, Permissions.READ);
        grants.putAll(SOLICITOR, Permissions.READ);
        grants.putAll(CASEWORKER_SUPERUSER, Permissions.READ);

        grants.putAll(CASEWORKER_COURTADMIN_CTSC, Permissions.CREATE_READ_UPDATE);
        grants.putAll(CASEWORKER_COURTADMIN_RDU, Permissions.CREATE_READ_UPDATE);
        grants.putAll(CASEWORKER_LEGAL_ADVISOR, Permissions.CREATE_READ_UPDATE);
        grants.putAll(CASEWORKER_SYSTEMUPDATE, Permissions.CREATE_READ_UPDATE);

        return grants;
    }
}
