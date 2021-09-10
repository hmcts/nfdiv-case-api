package uk.gov.hmcts.divorce.divorcecase.model.access;

import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static org.assertj.core.data.MapEntry.entry;
import static org.assertj.guava.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;

class CaseworkerAccessBetaOnlyAccessTest {

    @Test
    void shouldGrantCaseworkerAccessBetaOnlyAccess() {

        final SetMultimap<HasRole, Permission> grants = new CaseworkerAccessBetaOnlyAccess().getGrants();

        assertThat(grants)
            .hasSize(7)
            .contains(
                entry(CITIZEN, R),
                entry(SOLICITOR, R),
                entry(CASEWORKER_SUPERUSER, R),
                entry(CASEWORKER_LEGAL_ADVISOR, R),
                entry(CASEWORKER_COURTADMIN, C),
                entry(CASEWORKER_COURTADMIN, R),
                entry(CASEWORKER_COURTADMIN, U)
            );
    }
}
