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
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.NOC_APPROVER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.ORGANISATION_CASE_ACCESS_ADMINISTRATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;

class CaseworkerWithAacSystemUserAccessTest {

    @Test
    void shouldGrantCaseworkerWithAacSystemUserAccess() {

        final SetMultimap<HasRole, Permission> grants = new CaseworkerWithAacSystemUserAccess().getGrants();

        assertThat(grants)
            .hasSize(21)
            .contains(
                entry(CITIZEN, R),
                entry(SOLICITOR, R),
                entry(CREATOR, R),
                entry(SUPER_USER, R),
                entry(CASE_WORKER, C),
                entry(CASE_WORKER, R),
                entry(CASE_WORKER, U),
                entry(LEGAL_ADVISOR, R),
                entry(JUDGE, R),
                entry(SYSTEMUPDATE, C),
                entry(SYSTEMUPDATE, R),
                entry(SYSTEMUPDATE, U),
                entry(SYSTEMUPDATE, D),
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
