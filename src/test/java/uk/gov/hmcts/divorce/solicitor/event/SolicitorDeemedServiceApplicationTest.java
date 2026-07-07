package uk.gov.hmcts.divorce.solicitor.event;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorDeemedServiceApplication.SOLICITOR_DEEMED_SERVICE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class SolicitorDeemedServiceApplicationTest {

    @InjectMocks
    private SolicitorDeemedServiceApplication solicitorDeemedServiceApplication;

    @Test
    void shouldAddSolicitorDeemedServiceApplicationEventToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorDeemedServiceApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_DEEMED_SERVICE_APPLICATION);
    }

    @Test
    void shouldGrantCreateReadUpdateToApplicantSolicitorAndReadOnlyToCaseRoles() {
        ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorDeemedServiceApplication.configure(configBuilder);

        SetMultimap<UserRole, Permission> expectedRolesAndPermissions = ImmutableSetMultimap.<UserRole, Permission>builder()
            .put(APPLICANT_1_SOLICITOR, C)
            .put(APPLICANT_1_SOLICITOR, R)
            .put(APPLICANT_1_SOLICITOR, U)
            .put(CASE_WORKER, R)
            .put(SUPER_USER, R)
            .put(LEGAL_ADVISOR, R)
            .put(JUDGE, R)
            .build();

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getGrants)
            .containsExactly(expectedRolesAndPermissions);
    }
}
