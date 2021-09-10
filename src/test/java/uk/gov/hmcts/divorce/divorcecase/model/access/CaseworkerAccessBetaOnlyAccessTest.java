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
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;

class CaseworkerAccessBetaOnlyAccessTest {

    @Test
    void shouldGrantCaseworkerAccessBetaOnlyAccess() {

        final SetMultimap<HasRole, Permission> grants = new CaseworkerAccessBetaOnlyAccess().getGrants();

        assertThat(grants)
            .hasSize(7)
            .contains(
                entry(CITIZEN, R),
                entry(SOLICITOR, R),
                entry(SUPER_USER, R),
                entry(LEGAL_ADVISOR, R),
                entry(CASE_WORKER, C),
                entry(CASE_WORKER, R),
                entry(CASE_WORKER, U)
            );
    }
}
