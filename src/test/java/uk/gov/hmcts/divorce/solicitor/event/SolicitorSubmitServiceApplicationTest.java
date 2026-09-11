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
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.SolicitorServiceApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.ServiceApplicationSubmitPaymentService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorSubmitServiceApplication.SOLICITOR_SUBMIT_SERVICE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SolicitorSubmitServiceApplicationTest {

    @InjectMocks
    private SolicitorSubmitServiceApplication solicitorSubmitServiceApplication;

    @Mock
    private ServiceApplicationSubmitPaymentService serviceApplicationSubmitPaymentService;

    @Mock
    private SolicitorServiceApplicationSubmittedNotification solicitorServiceApplicationSubmittedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Test
    void shouldAddSolicitorSubmitServiceApplicationEventToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorSubmitServiceApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_SUBMIT_SERVICE_APPLICATION);
    }

    @Test
    void shouldGrantCreateReadUpdateToApplicantSolicitorAndReadOnlyToCaseRoles() {
        ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorSubmitServiceApplication.configure(configBuilder);

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

    @Test
    void shouldReturnErrorAndNotArchiveWhenPaymentProcessingFails() {
        Applicant applicant = mock(Applicant.class);
        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .alternativeService(AlternativeService.builder().build())
            .build();

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        when(serviceApplicationSubmitPaymentService.processSubmitPayment(TEST_CASE_ID, caseData))
            .thenReturn(Optional.of("payment failed"));

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitServiceApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
        assertThat(response.getErrors()).containsExactly("payment failed");
        assertThat(response.getState()).isNull();

        verify(serviceApplicationSubmitPaymentService).processSubmitPayment(TEST_CASE_ID, caseData);
        verify(applicant, never()).archiveInterimApplicationOptions();
    }

    @Test
    void shouldArchiveAndSetAwaitingServiceConsiderationWhenPaymentMethodIsPba() {
        Applicant applicant = mock(Applicant.class);
        AlternativeService alternativeService = AlternativeService.builder().build();
        alternativeService.getServicePaymentFee().setPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT);

        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .alternativeService(alternativeService)
            .build();

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        when(serviceApplicationSubmitPaymentService.processSubmitPayment(TEST_CASE_ID, caseData))
            .thenReturn(Optional.empty());

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitServiceApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(response.getState()).isEqualTo(State.AwaitingServiceConsideration);

        verify(serviceApplicationSubmitPaymentService).processSubmitPayment(TEST_CASE_ID, caseData);
        verify(applicant).archiveInterimApplicationOptions();
    }

    @Test
    void shouldArchiveAndSetAwaitingServicePaymentWhenPaymentMethodIsHwf() {
        Applicant applicant = mock(Applicant.class);
        AlternativeService alternativeService = AlternativeService.builder().build();
        alternativeService.getServicePaymentFee().setPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_HWF);

        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .alternativeService(alternativeService)
            .build();

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        when(serviceApplicationSubmitPaymentService.processSubmitPayment(TEST_CASE_ID, caseData))
            .thenReturn(Optional.empty());

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitServiceApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
        assertThat(response.getState()).isEqualTo(State.AwaitingServicePayment);

        verify(serviceApplicationSubmitPaymentService).processSubmitPayment(TEST_CASE_ID, caseData);
        verify(applicant).archiveInterimApplicationOptions();
    }

    @Test
    void shouldReturnErrorInAboutToStartCallbackWhenPaymentReferenceIsPresent() {
        final CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .servicePaymentFee(FeeDetails.builder()
                    .paymentReference("123456")
                    .build())
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitServiceApplication.aboutToStart(details);
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).containsExactly(
            "The ongoing service application on this case has already been submitted and you cannot submit it again or amend it.");
    }

    @Test
    void shouldReturnErrorInAboutToStartCallbackWhenHWFReferenceIsPresent() {
        final CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .servicePaymentFee(FeeDetails.builder()
                    .helpWithFeesReferenceNumber("123456")
                    .build())
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitServiceApplication.aboutToStart(details);
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).containsExactly(
            "The ongoing service application on this case has already been submitted and you cannot submit it again or amend it."
        );
    }

    @Test
    void shouldNotReturnErrorInAboutToStartCallback() {
        final CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .servicePaymentFee(FeeDetails.builder()
                    .build())
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitServiceApplication.aboutToStart(details);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldSendSolicitorServiceApplicationSubmittedNotificationOnSubmittedCallback() {
        CaseData caseData = CaseData.builder().build();

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        SubmittedCallbackResponse response = solicitorSubmitServiceApplication.submitted(caseDetails, caseDetails);

        assertThat(response).isNotNull();
        verify(notificationDispatcher).send(solicitorServiceApplicationSubmittedNotification, caseData, TEST_CASE_ID);
    }
}
