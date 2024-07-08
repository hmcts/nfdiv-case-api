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
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationRequest;
import uk.gov.hmcts.ccd.sdk.type.DynamicListItem;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.NocCitizenToSolsNotifications;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseRoleID;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.noticeofchange.client.AssignCaseAccessClient;
import uk.gov.hmcts.divorce.noticeofchange.model.AcaRequest;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.NoticeType.ORG_REMOVED;
import static uk.gov.hmcts.divorce.noticeofchange.event.SystemRequestNoticeOfChange.NOC_JOINT_OFFLINE_CASE_ERROR;
import static uk.gov.hmcts.divorce.noticeofchange.event.SystemRequestNoticeOfChange.NOC_JUDICIAL_SEPARATION_CASE_ERROR;
import static uk.gov.hmcts.divorce.noticeofchange.event.SystemRequestNoticeOfChange.NOTICE_OF_CHANGE_REQUESTED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SystemRequestNoticeOfChangeTest {
    private static final String TEST_ORGANISATION_NAME = "organisation_name";
    private static final String TEST_ORGANISATION_ID = "organisation_id";

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @Mock
    private AssignCaseAccessClient assignCaseAccessClient;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private NocCitizenToSolsNotifications nocCitizenToSolsNotifications;

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

        assertThat(response.getErrors().size()).isEqualTo(1);
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

        assertThat(response.getErrors().size()).isEqualTo(1);
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

        assertThat(response.getErrors().size()).isEqualTo(1);
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

        assertThat(response.getErrors().size()).isZero();
    }

    @Test
    public void shouldCheckNoticeOfChangeApprovalByDelegatingToAssignCaseAccessClient() {
        CaseData data = caseData();
        data.setChangeOrganisationRequestField(getChangeOrganisationRequestField(
        ));

        CaseData beforeCaseData = caseData();
        final var details = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(data).build();
        final var beforeDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(beforeCaseData).build();
        final User systemUser = mock(User.class);
        final AcaRequest nocApiRequest = AcaRequest.acaRequest(details);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        systemRequestNoticeOfChange.submitted(details, beforeDetails);

        verify(assignCaseAccessClient).checkNocApproval(
            TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, nocApiRequest
        );

        verify(notificationDispatcher).sendNOC(nocCitizenToSolsNotifications, data, beforeCaseData,
                TEST_CASE_ID, true, ORG_REMOVED);
    }

    private ChangeOrganisationRequest<CaseRoleID> getChangeOrganisationRequestField() {
        DynamicListItem dynamicListItem = DynamicListItem.builder().label(
                "APPLICANT_1_SOLICITOR").code("[APPONESOLICITOR]").build();
        List<DynamicListItem> dynamicListItemList = new ArrayList<>();
        dynamicListItemList.add(dynamicListItem);

        ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequest = ChangeOrganisationRequest.<CaseRoleID>builder().build();
        changeOrganisationRequest.setCaseRoleId(CaseRoleID.builder().value(dynamicListItem).listItems(dynamicListItemList).build());
        changeOrganisationRequest.setCreatedBy(TEST_SOLICITOR_EMAIL);
        changeOrganisationRequest.setOrganisationToAdd(Organisation
                .builder().organisationId(TEST_ORGANISATION_ID).organisationName(TEST_ORG_NAME).build());
        changeOrganisationRequest.setOrganisationToRemove(Organisation
                .builder().organisationId(TEST_ORG_ID).organisationName(TEST_ORGANISATION_NAME).build());
        return changeOrganisationRequest;
    }
}
