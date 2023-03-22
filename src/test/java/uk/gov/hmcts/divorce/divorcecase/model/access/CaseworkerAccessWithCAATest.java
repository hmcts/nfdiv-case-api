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
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.ORGANISATION_CASE_ACCESS_ADMINISTRATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;

class CaseworkerAccessWithCAATest {

    @Test
    void shouldGrantCaseworkerAccess() {

        final SetMultimap<HasRole, Permission> grants = new CaseworkerWithCAAAccess().getGrants();

        assertThat(grants)
            .hasSize(22)
            .contains(
                entry(CITIZEN, R),
                entry(SOLICITOR, R),
                entry(SUPER_USER, R),
                entry(CASE_WORKER, C),
                entry(CASE_WORKER, R),
                entry(CASE_WORKER, U),
                entry(LEGAL_ADVISOR, C),
                entry(LEGAL_ADVISOR, R),
                entry(LEGAL_ADVISOR, U),
                entry(JUDGE, C),
                entry(JUDGE, R),
                entry(JUDGE, U),
                entry(SYSTEMUPDATE, C),
                entry(SYSTEMUPDATE, R),
                entry(SYSTEMUPDATE, U),
                entry(SYSTEMUPDATE, D),
                entry(ORGANISATION_CASE_ACCESS_ADMINISTRATOR, C),
                entry(ORGANISATION_CASE_ACCESS_ADMINISTRATOR, R),
                entry(ORGANISATION_CASE_ACCESS_ADMINISTRATOR, U),
                entry(CREATOR, C),
                entry(CREATOR, R),
                entry(CREATOR, U)
            );
    }
}
