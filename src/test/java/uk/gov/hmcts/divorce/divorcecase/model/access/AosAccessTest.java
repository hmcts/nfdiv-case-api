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
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.RESPONDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;

class AosAccessTest {

    @Test
    void shouldGrantAosAccess() {

        final SetMultimap<HasRole, Permission> grants = new AosAccess().getGrants();

        assertThat(grants)
            .hasSize(11)
            .contains(
                entry(SYSTEMUPDATE, C),
                entry(SYSTEMUPDATE, R),
                entry(SYSTEMUPDATE, U),
                entry(CASE_WORKER, R),
                entry(LEGAL_ADVISOR, R),
                entry(APPLICANT_2_SOLICITOR, C),
                entry(APPLICANT_2_SOLICITOR, R),
                entry(APPLICANT_2_SOLICITOR, U),
                entry(RESPONDENT, C),
                entry(RESPONDENT, R),
                entry(RESPONDENT, U)
            );
    }
}
