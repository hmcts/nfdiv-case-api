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
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;

class CaseworkerAndSuperUserAccessTest {

    @Test
    void shouldGrantCaseworkerAndSuperUserAccess() {

        final SetMultimap<HasRole, Permission> grants = new CaseworkerAndSuperUserAccess().getGrants();

        assertThat(grants)
            .hasSize(13)
            .contains(
                entry(LEGAL_ADVISOR, R),
                entry(JUDGE, R),
                entry(SUPER_USER, C),
                entry(SUPER_USER, R),
                entry(SUPER_USER, U),
                entry(SUPER_USER, D),
                entry(CASE_WORKER, C),
                entry(CASE_WORKER, R),
                entry(CASE_WORKER, U),
                entry(SYSTEMUPDATE, C),
                entry(SYSTEMUPDATE, R),
                entry(SYSTEMUPDATE, U),
                entry(SYSTEMUPDATE, D)
            );
    }
}
