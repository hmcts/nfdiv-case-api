package uk.gov.hmcts.divorce.noticeofchange.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.noticeofchange.client.AssignCaseAccessClient;
import uk.gov.hmcts.divorce.noticeofchange.model.AcaRequest;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.noticeofchange.event.SystemRequestNoticeOfChange.NOC_JOINT_OFFLINE_CASE_ERROR;
import static uk.gov.hmcts.divorce.noticeofchange.event.SystemRequestNoticeOfChange.NOC_JUDICIAL_SEPARATION_CASE_ERROR;
import static uk.gov.hmcts.divorce.noticeofchange.event.SystemRequestNoticeOfChange.NOTICE_OF_CHANGE_REQUESTED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SystemRequestNoticeOfChangeTest {
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @Mock
    private AssignCaseAccessClient assignCaseAccessClient;

    @InjectMocks
    private SystemRequestNoticeOfChange systemRequestNoticeOfChange;

    @Test
    public void configure() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemRequestNoticeOfChange.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(NOTICE_OF_CHANGE_REQUESTED);
    }

    @Test
    public void shouldReturnValidationErrorForJSCases() {
        final var details = new CaseDetails<CaseData, State>();
        CaseData data = caseData();
        data.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);
        details.setData(data);

        final AboutToStartOrSubmitResponse<CaseData, State> response = systemRequestNoticeOfChange.aboutToStart(details);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).isEqualTo(
            String.format(NOC_JUDICIAL_SEPARATION_CASE_ERROR, details.getId())
        );
    }

    @Test
    public void shouldReturnValidationErrorForJointCaseWithApp1Offline() {
        final var details = new CaseDetails<CaseData, State>();
        CaseData data = caseData();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.setApplicant1(Applicant.builder().offline(YesOrNo.YES).build());
        details.setData(data);

        final AboutToStartOrSubmitResponse<CaseData, State> response = systemRequestNoticeOfChange.aboutToStart(details);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).isEqualTo(
            String.format(NOC_JOINT_OFFLINE_CASE_ERROR, details.getId())
        );
    }

    @Test
    public void shouldReturnValidationErrorForJointCaseWithApp2Offline() {
        final var details = new CaseDetails<CaseData, State>();
        CaseData data = caseData();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.setApplicant2(Applicant.builder().offline(YesOrNo.YES).build());
        details.setData(data);

        final AboutToStartOrSubmitResponse<CaseData, State> response = systemRequestNoticeOfChange.aboutToStart(details);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).isEqualTo(
            String.format(NOC_JOINT_OFFLINE_CASE_ERROR, details.getId())
        );
    }

    @Test
    public void shouldNotReturnValidationErrorWhenCaseIsSuitableForNoc() {
        final var details = new CaseDetails<CaseData, State>();
        CaseData data = caseData();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.setApplicant2(Applicant.builder().offline(YesOrNo.YES).build());
        details.setData(data);

        final AboutToStartOrSubmitResponse<CaseData, State> response = systemRequestNoticeOfChange.aboutToStart(details);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    public void shouldCheckNoticeOfChangeApprovalByDelegatingToAssignCaseAccessClient() {
        final var details = new CaseDetails<CaseData, State>();
        final var beforeDetails = new CaseDetails<CaseData, State>();

        final User systemUser = mock(User.class);
        final AcaRequest nocApiRequest = AcaRequest.acaRequest(details);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        systemRequestNoticeOfChange.submitted(details, beforeDetails);

        verify(assignCaseAccessClient).checkNocApproval(
            TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, nocApiRequest
        );
    }
}
