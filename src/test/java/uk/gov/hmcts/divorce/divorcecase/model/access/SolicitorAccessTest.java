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
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;

class SolicitorAccessTest {

    @Test
    void shouldGrantAosAccess() {

        final SetMultimap<HasRole, Permission> grants = new SolicitorAccess().getGrants();

        assertThat(grants)
            .hasSize(3)
            .contains(
                entry(SOLICITOR, C),
                entry(SOLICITOR, R),
                entry(SOLICITOR, U)
            );
    }
}
