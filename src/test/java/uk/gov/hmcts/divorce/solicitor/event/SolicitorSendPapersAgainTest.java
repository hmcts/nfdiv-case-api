package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.IssueApplicationService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.notification.SolicitorSendPapersAgainNotification;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorSendPapersAgain.SOLICITOR_RESEND_PAPERS;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_VALIDATION_ERROR;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.invalidCaseData;

@ExtendWith(MockitoExtension.class)
class SolicitorSendPapersAgainTest {

    @Mock
    private IssueApplicationService issueApplicationService;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private SolicitorSendPapersAgainNotification solicitorSendPapersAgainNotification;

    @InjectMocks
    private SolicitorSendPapersAgain solicitorSendPapersAgain;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorSendPapersAgain.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_RESEND_PAPERS);
    }

    @Test
    void aboutToSubmitShouldReturnErrorIfEmailIsRemoved() {

        final var caseData = invalidCaseData();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        details.setCreatedDate(LOCAL_DATE_TIME);

        when(issueApplicationService.updateServiceType(details)).thenReturn(details);
        when(issueApplicationService.validateIssueApplication(details)).thenReturn(Collections.singletonList(TEST_VALIDATION_ERROR));

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSendPapersAgain.aboutToSubmit(details, null);

        assertThat(response.getErrors())
            .contains(TEST_VALIDATION_ERROR);
    }

    @Test
    void shouldCallIssueApplicationServiceAndReturnCaseData() {

        final var caseData = TestDataHelper.caseData();
        final var expectedCaseData = CaseData.builder().build();
        final var expectedCaseDataWithServiceType = CaseData.builder().build();
        caseData.getApplication().setSolSignStatementOfTruth(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        details.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> expectedDetailsWithServiceType = new CaseDetails<>();
        expectedDetailsWithServiceType.setData(expectedCaseDataWithServiceType);
        expectedDetailsWithServiceType.setId(TEST_CASE_ID);
        expectedDetailsWithServiceType.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> expectedDetails = new CaseDetails<>();
        expectedDetails.setData(expectedCaseData);
        expectedDetails.setId(TEST_CASE_ID);
        expectedDetails.setCreatedDate(LOCAL_DATE_TIME);
        expectedDetails.setState(AwaitingService);

        when(issueApplicationService.issueApplication(expectedDetailsWithServiceType)).thenReturn(expectedDetails);
        when(issueApplicationService.updateServiceType(details)).thenReturn(expectedDetailsWithServiceType);
        when(issueApplicationService.validateIssueApplication(expectedDetailsWithServiceType)).thenReturn(Collections.emptyList());

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSendPapersAgain.aboutToSubmit(details, null);

        assertThat(response.getData()).isEqualTo(expectedCaseData);
        assertThat(response.getState()).isEqualTo(AwaitingService);
        verify(issueApplicationService).updateServiceType(details);
        verify(issueApplicationService).issueApplication(expectedDetailsWithServiceType);
        verify(issueApplicationService).validateIssueApplication(expectedDetailsWithServiceType);
    }

    @Test
    void shouldReturnMidEventErrorsWhenServiceMethodIsCourtServiceAndApplicant2AddressOverseas() {
        var caseData = caseData();

        caseData.getApplicant2().setAddressOverseas(YES);
        caseData.getApplication().setServiceMethod(ServiceMethod.COURT_SERVICE);

        var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorSendPapersAgain.midEvent(caseDetails, null);

        assertThat(response.getErrors()).isEqualTo(List.of(
            "You may not select court service because the respondent has an international address."));
    }

    @Test
    void shouldReturnMidEventErrorsWhenServiceMethodIsPersonalService() {
        var caseData = caseData();

        caseData.getApplication().setServiceMethod(ServiceMethod.PERSONAL_SERVICE);

        var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorSendPapersAgain.midEvent(caseDetails, null);

        assertThat(response.getErrors()).isEqualTo(List.of("You may not select personal service."));
    }

    @Test
    void shouldReturnMidEventErrorsWhenServiceMethodIsSolicitorServiceAndApplicant2AddressConfidential() {
        var caseData = caseData();

        caseData.getApplication().setServiceMethod(ServiceMethod.SOLICITOR_SERVICE);
        caseData.getApplicant2().setContactDetailsType(ContactDetailsType.PRIVATE);

        var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorSendPapersAgain.midEvent(caseDetails, null);

        assertThat(response.getErrors()).isEqualTo(List.of("You may not select solicitor service because the respondent is confidential."));
    }

    @Test
    void shouldReturnMidEventNoValidationErrorsWhenServiceMethodIsSolicitorServiceAndApplicant2AddressNonConfidential() {
        var caseData = caseData();

        caseData.getApplication().setServiceMethod(ServiceMethod.SOLICITOR_SERVICE);
        caseData.getApplicant2().setContactDetailsType(ContactDetailsType.PUBLIC);

        var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorSendPapersAgain.midEvent(caseDetails, null);

        assertThat(response.getErrors()).isNullOrEmpty();
    }

    @Test
    void shouldReturnMidEventNoValidationErrorsWhenServiceMethodIsCourtServiceAndApplicant2AddressNotOverseas() {
        var caseData = caseData();

        caseData.getApplication().setServiceMethod(ServiceMethod.COURT_SERVICE);
        caseData.getApplicant2().setAddressOverseas(NO);

        var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorSendPapersAgain.midEvent(caseDetails, null);

        assertThat(response.getErrors()).isNullOrEmpty();
    }

    @Test
    void shouldTriggerNotificationSolicitorSendPapersAgainSubmitted() {
        var caseData = caseData();

        caseData.getApplication().setServiceMethod(ServiceMethod.COURT_SERVICE);
        caseData.getApplicant2().setAddressOverseas(NO);

        var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();

        final SubmittedCallbackResponse response =
            solicitorSendPapersAgain.submitted(caseDetails, null);

        verify(notificationDispatcher).send(solicitorSendPapersAgainNotification, caseData, caseDetails.getId());
    }
}
