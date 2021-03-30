package uk.gov.hmcts.reform.divorce.ccd.access;

import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ccd.sdk.api.Permission.CRU;
import static uk.gov.hmcts.reform.divorce.ccd.Permissions.READ;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CITIZEN;

public class DefaultAccessTest {

    @Test
    void shouldAddGrantsToMultiMap() {
        DefaultAccess access = new DefaultAccess();
        SetMultimap<HasRole, Permission> grants = access.getGrants();

        assertEquals(grants.get(CITIZEN), CRU);
        assertEquals(grants.get(CASEWORKER_DIVORCE_COURTADMIN_BETA), READ);
        assertEquals(grants.get(CASEWORKER_DIVORCE_COURTADMIN), READ);
        assertEquals(grants.get(CASEWORKER_DIVORCE_SOLICITOR), READ);
        assertEquals(grants.get(CASEWORKER_DIVORCE_SUPERUSER), READ);
        assertEquals(grants.get(CASEWORKER_DIVORCE_COURTADMIN_LA), READ);
    }
}
