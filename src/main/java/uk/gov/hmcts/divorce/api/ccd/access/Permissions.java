package uk.gov.hmcts.divorce.api.ccd.access;

import uk.gov.hmcts.ccd.sdk.api.Permission;

import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.api.Permission.CRU;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;

public final class Permissions {

    public static final Set<Permission> CREATE_READ_UPDATE = CRU;
    public static final Set<Permission> READ_UPDATE = Set.of(R, U);
    public static final Set<Permission> READ = Set.of(R);

    private Permissions() {
    }
}
