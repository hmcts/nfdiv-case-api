package uk.gov.hmcts.divorce.divorcecase.model.access;

import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import static org.assertj.core.data.MapEntry.entry;
import static org.assertj.guava.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;

class DefaultStateAccessExcludingCAATest {

    @Test
    void shouldGrantSolicitorsAndCitizens() {

        final SetMultimap<HasRole, Permission> grants = new DefaultStateAccessExcludingCAA().getGrants();

        assertThat(grants)
            .hasSize(5)
            .contains(
                entry(CREATOR, R),
                entry(APPLICANT_2, R),
                entry(APPLICANT_1_SOLICITOR, R),
                entry(APPLICANT_2_SOLICITOR, R),
                entry(JUDGE, R)
            );
    }
}
