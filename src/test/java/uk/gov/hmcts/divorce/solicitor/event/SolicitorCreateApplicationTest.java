package uk.gov.hmcts.divorce.solicitor.event;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.divorce.common.AddSystemUpdateRole;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.SolAboutTheSolicitor;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorCreateApplicationService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorCreateApplication.SOLICITOR_CREATE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SolicitorCreateApplicationTest {

    @Mock
    private SolicitorCreateApplicationService solicitorCreateApplicationService;

    @Mock
    private SolAboutTheSolicitor solAboutTheSolicitor;

    @Mock
    private AddSystemUpdateRole addSystemUpdateRole;

    @InjectMocks
    private SolicitorCreateApplication solicitorCreateApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorCreateApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_CREATE);
    }

    @Test
    void shouldSetPermissionForSolicitorAndSystemUpdateRoleWhenEnvironmentIsAat() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        when(addSystemUpdateRole.addIfConfiguredForEnvironment(anyList()))
            .thenReturn(List.of(SOLICITOR, CASEWORKER_SYSTEMUPDATE));

        solicitorCreateApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_CREATE);

        SetMultimap<UserRole, Permission> expectedRolesAndPermissions = ImmutableSetMultimap.<UserRole, Permission>builder()
            .put(SOLICITOR, C)
            .put(SOLICITOR, R)
            .put(SOLICITOR, U)
            .put(CASEWORKER_SYSTEMUPDATE, C)
            .put(CASEWORKER_SYSTEMUPDATE, R)
            .put(CASEWORKER_SYSTEMUPDATE, U)
            .put(CASEWORKER_SUPERUSER, R)
            .put(CASEWORKER_SUPERUSER, U)
            .put(CASEWORKER_COURTADMIN, R)
            .put(CASEWORKER_LEGAL_ADVISOR, R)
            .build();

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getGrants)
            .containsExactlyInAnyOrder(expectedRolesAndPermissions);

        verify(addSystemUpdateRole).addIfConfiguredForEnvironment(anyList());
    }

    @Test
    public void shouldPopulateMissingRequirementsFieldsInCaseData() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        details.setCreatedDate(LOCAL_DATE_TIME);

        when(solicitorCreateApplicationService.aboutToSubmit(details)).thenReturn(details);

        solicitorCreateApplication.aboutToSubmit(details, beforeDetails);

        verify(solicitorCreateApplicationService).aboutToSubmit(details);
    }
}
