package uk.gov.hmcts.divorce.divorcecase.model.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.TTL_PROFILE;

public class TtlProfileAccess implements HasAccessControl {
    @Override
    public SetMultimap<HasRole, Permission> getGrants() {

        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.putAll(TTL_PROFILE, Permissions.CREATE_READ_UPDATE);

        return grants;
    }
}
