package uk.gov.hmcts.divorce.divorcecase.model.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.NOC_APPROVER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.ORGANISATION_CASE_ACCESS_ADMINISTRATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

public class AcaSystemUserAccess implements HasAccessControl {
    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(ORGANISATION_CASE_ACCESS_ADMINISTRATOR, CREATE_READ_UPDATE_DELETE);
        grants.putAll(NOC_APPROVER, CREATE_READ_UPDATE_DELETE);
        return grants;
    }
}
