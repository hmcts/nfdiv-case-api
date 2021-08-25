package uk.gov.hmcts.divorce.divorcecase.model.access;

import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static org.assertj.core.data.MapEntry.entry;
import static org.assertj.guava.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.D;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;

class CaseworkerBetaAndSuperUserAccessTest {

    @Test
    void shouldGrantCaseworkerBetaAndSuperUserAccess() {

        final SetMultimap<HasRole, Permission> grants = new CaseworkerBetaAndSuperUserAccess().getGrants();

        assertThat(grants)
            .hasSize(7)
            .contains(
                entry(CASEWORKER_SUPERUSER, C),
                entry(CASEWORKER_SUPERUSER, R),
                entry(CASEWORKER_SUPERUSER, U),
                entry(CASEWORKER_SUPERUSER, D),
                entry(CASEWORKER_COURTADMIN_CTSC, C),
                entry(CASEWORKER_COURTADMIN_CTSC, R),
                entry(CASEWORKER_COURTADMIN_CTSC, U)
            );
    }
}