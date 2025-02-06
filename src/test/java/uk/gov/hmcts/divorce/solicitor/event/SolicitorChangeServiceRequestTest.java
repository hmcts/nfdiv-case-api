package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.ReIssueApplicationService;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant1NoticeOfProceeding;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant2NoticeOfProceedings;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateD10Form;
import uk.gov.hmcts.divorce.common.notification.ApplicationIssuedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.REISSUE_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorChangeServiceRequest.NOT_ISSUED_ERROR;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorChangeServiceRequest.SOLICITOR_CHANGE_SERVICE_REQUEST;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemIssueSolicitorServicePack.SYSTEM_ISSUE_SOLICITOR_SERVICE_PACK;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithStatementOfTruth;

@ExtendWith(MockitoExtension.class)
class SolicitorChangeServiceRequestTest {

    @Mock
    IdamService idamService;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    CcdUpdateService ccdUpdateService;

    @Mock
    GenerateApplicant1NoticeOfProceeding generateApplicant1NoticeOfProceeding;

    @Mock
    GenerateApplicant2NoticeOfProceedings generateApplicant2NoticeOfProceedings;

    @Mock
    GenerateD10Form generateD10Form;

    @Mock
    ApplicationIssuedNotification applicationIssuedNotification;

    @Mock
    ReIssueApplicationService reIssueApplicationService;

    @InjectMocks
    private SolicitorChangeServiceRequest solicitorChangeServiceRequest;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorChangeServiceRequest.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_CHANGE_SERVICE_REQUEST);
    }

    @Test
    void shouldThrowErrorIfApplicationHasNotBeenIssued() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.setApplication(Application.builder().issueDate(null).build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorChangeServiceRequest.aboutToStart(caseDetails);

        assertThat(response.getErrors()).isEqualTo(List.of(NOT_ISSUED_ERROR));
    }

    @Test
    void shouldNotThrowErrorIfApplicationHasBeenIssued() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.getApplication().setIssueDate(LocalDate.of(2022, 01, 01));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorChangeServiceRequest.aboutToStart(caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldThrowErrorIfPersonalService() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        updatedCaseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorChangeServiceRequest.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).contains("You may not select Personal Service. Please select Solicitor or Court Service.");
    }

    @Test
    void shouldThrowErrorIfSolicitorServiceConfidential() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PRIVATE);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        updatedCaseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorChangeServiceRequest.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).contains("You may not select Solicitor Service if the respondent is confidential.");
    }

    @Test
    void shouldThrowErrorIfCourtServiceForOverseasAndNotConfidentialRespondentSoleApp() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setAddressOverseas(YesOrNo.YES);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        updatedCaseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorChangeServiceRequest.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).contains("Solicitor cannot select court service because the "
            + "respondent has an international address.");
    }

    @Test
    void shouldThrowErrorIfCourtServiceForOverseasAndNotConfidentialRespondentJointApp() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setAddressOverseas(YesOrNo.YES);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        updatedCaseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorChangeServiceRequest.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldChangeStateToAwaitingAosForCourtServiceAndRegenerateNOPD10AndD84WhenApplicationIssued() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplication().setIssueDate(now());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);
        when(generateD10Form.apply(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorChangeServiceRequest.aboutToSubmit(
            caseDetails, caseDetails);

        verify(generateApplicant1NoticeOfProceeding).apply(caseDetails);
        verify(generateApplicant2NoticeOfProceedings).apply(caseDetails);
        verify(generateD10Form).apply(caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(AwaitingAos);
    }

    @Test
    void shouldChangeStateToAwaitingServiceForSolicitorServiceAndRegenerateNOPWhenApplicationIssued() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        caseData.getApplication().setIssueDate(now());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        when(generateApplicant1NoticeOfProceeding.apply(caseDetails)).thenReturn(caseDetails);
        when(generateApplicant2NoticeOfProceedings.apply(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorChangeServiceRequest.aboutToSubmit(
            caseDetails, caseDetails);

        verify(generateApplicant1NoticeOfProceeding).apply(caseDetails);
        verify(generateApplicant2NoticeOfProceedings).apply(caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(AwaitingService);
    }

    @Test
    void shouldSetDueDateWhenServiceTypeChangedFromSolicitorToCourt() {
        final long dueDateOffsetDays = 16;
        ReflectionTestUtils.setField(solicitorChangeServiceRequest, "dueDateOffsetDays", dueDateOffsetDays);

        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseData beforeCaseData = caseDataWithStatementOfTruth();
        beforeCaseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(beforeCaseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorChangeServiceRequest.aboutToSubmit(
            caseDetails, beforeDetails);

        assertThat(response.getData().getDueDate()).isEqualTo(now().plusDays(dueDateOffsetDays));
    }

    @Test
    void shouldNotNotifyOnSubmittedCallbackIfSolicitorService() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingService);

        solicitorChangeServiceRequest.submitted(caseDetails, caseDetails);

        verifyNoInteractions(reIssueApplicationService);
    }

    @Test
    void shouldNotSubmitCcdSystemIssueSolicitorServicePackEventOrNotifyOnSubmittedCallbackIfCourtService() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingAos);

        solicitorChangeServiceRequest.submitted(caseDetails, caseDetails);

        verifyNoInteractions(ccdUpdateService);
        verifyNoInteractions(applicationIssuedNotification);
    }

    @Test
    void shouldNotifyOnSubmittedCallbackIfCourtServiceWhenApplicationIssued() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplication().setIssueDate(now());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingAos);

        solicitorChangeServiceRequest.submitted(caseDetails, caseDetails);

        verify(reIssueApplicationService).sendNotifications(caseDetails, REISSUE_CASE);
    }

    @Test
    void shouldSubmitCcdSystemIssueSolicitorServicePackEventAndNotifyOnSubmittedCallbackIfSolicitorServiceWhenApplicationIssued() {
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        caseData.getApplication().setIssueDate(now());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingService);
        caseDetails.setId(TEST_CASE_ID);

        solicitorChangeServiceRequest.submitted(caseDetails, caseDetails);

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_ISSUE_SOLICITOR_SERVICE_PACK, user, SERVICE_AUTHORIZATION);
        verify(applicationIssuedNotification).sendToApplicant1Solicitor(caseDetails.getData(), caseDetails.getId());
    }
}
