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
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.NOC_APPROVER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.ORGANISATION_CASE_ACCESS_ADMINISTRATOR;

class AcaSystemUserAccessTest {

    @Test
    void shouldGrantOrganisationPolicyAccess() {

        final SetMultimap<HasRole, Permission> grants = new AcaSystemUserAccess().getGrants();

        assertThat(grants)
            .hasSize(8)
            .contains(
                entry(ORGANISATION_CASE_ACCESS_ADMINISTRATOR, C),
                entry(ORGANISATION_CASE_ACCESS_ADMINISTRATOR, R),
                entry(ORGANISATION_CASE_ACCESS_ADMINISTRATOR, U),
                entry(ORGANISATION_CASE_ACCESS_ADMINISTRATOR, D),
                entry(NOC_APPROVER, C),
                entry(NOC_APPROVER, R),
                entry(NOC_APPROVER, U),
                entry(NOC_APPROVER, D)
            );
    }
}
