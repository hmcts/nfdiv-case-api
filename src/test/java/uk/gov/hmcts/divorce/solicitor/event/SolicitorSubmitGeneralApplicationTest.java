package uk.gov.hmcts.divorce.solicitor.event;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import jakarta.servlet.http.HttpServletRequest;
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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.service.CitizenGeneralApplicationSubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.solicitor.service.GeneralApplicationSubmitPaymentService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorSubmitGeneralApplication.SOLICITOR_SUBMIT_GENERAL_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class SolicitorSubmitGeneralApplicationTest {

    @InjectMocks
    private SolicitorSubmitGeneralApplication solicitorSubmitGeneralApplication;

    @Mock
    private GeneralApplicationSubmitPaymentService submitPaymentService;

    @Mock
    private CitizenGeneralApplicationSubmissionService citizenGeneralApplicationSubmissionService;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest request;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorSubmitGeneralApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_SUBMIT_GENERAL_APPLICATION);
    }

    @Test
    void shouldGrantCrudToApplicant1SolicitorAndReadOnlyToCaseRoles() {
        ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorSubmitGeneralApplication.configure(configBuilder);

        SetMultimap<UserRole, Permission> expected = ImmutableSetMultimap.<UserRole, Permission>builder()
            .put(APPLICANT_1_SOLICITOR, C)
            .put(APPLICANT_1_SOLICITOR, R)
            .put(APPLICANT_1_SOLICITOR, U)
            .put(APPLICANT_2_SOLICITOR, C)
            .put(APPLICANT_2_SOLICITOR, R)
            .put(APPLICANT_2_SOLICITOR, U)
            .put(CASE_WORKER, R)
            .put(SUPER_USER, R)
            .put(LEGAL_ADVISOR, R)
            .put(JUDGE, R)
            .build();

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getGrants)
            .containsExactlyInAnyOrder(expected);
    }

    @Test
    void shouldReturnErrorWhenNoActiveGeneralApplicationOnAboutToStart() {
        CaseData caseData = CaseData.builder().build();
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(true);
        when(citizenGeneralApplicationSubmissionService.findActiveGeneralApplication(caseData, caseData.getApplicant1()))
            .thenReturn(Optional.empty());

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitGeneralApplication.aboutToStart(caseDetails);

        assertThat(response.getErrors()).containsExactly("No active draft general application found for the acting applicant");
        assertThat(response.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldPopulateGeneralApplicationWhenActiveDraftExistsForApplicant1OnAboutToStart() {
        CaseData caseData = CaseData.builder().build();
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        final GeneralApplication activeGeneralApplication = GeneralApplication.builder().build();

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(true);
        when(citizenGeneralApplicationSubmissionService.findActiveGeneralApplication(caseData, caseData.getApplicant1()))
            .thenReturn(Optional.of(activeGeneralApplication));

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitGeneralApplication.aboutToStart(caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldPopulateGeneralApplicationWhenActiveDraftExistsForApplicant2OnAboutToStart() {
        CaseData caseData = CaseData.builder().build();
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        final GeneralApplication activeGeneralApplication = GeneralApplication.builder().build();

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(false);
        when(citizenGeneralApplicationSubmissionService.findActiveGeneralApplication(caseData, caseData.getApplicant2()))
            .thenReturn(Optional.of(activeGeneralApplication));

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitGeneralApplication.aboutToStart(caseDetails);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData().getGeneralApplication()).isEqualTo(activeGeneralApplication);
    }

    @Test
    void shouldReturnPaymentErrorAndNotProgressSubmissionWhenPaymentProcessingFails() {
        CaseData caseData = CaseData.builder().build();
        final GeneralApplication generalApplication = GeneralApplication.builder()
            .generalApplicationFee(FeeDetails.builder().paymentMethod(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT).build())
            .build();
        caseData.setGeneralApplication(generalApplication);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(true);
        when(submitPaymentService.processSubmitPayment(TEST_CASE_ID, caseData, caseData.getApplicant1()))
            .thenReturn(Optional.of("Payment failed"));

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitGeneralApplication.aboutToSubmit(details, details);

        assertThat(response.getErrors()).containsExactly("Payment failed");
        assertThat(response.getState()).isNull();
        assertThat(caseData.getGeneralApplication().getGeneralApplicationSubmittedOnline()).isNull();
    }

    @Test
    void shouldClearActiveGeneralApplicationAndMoveToReceivedWhenPaymentMethodIsPba() {
        CaseData caseData = CaseData.builder().build();
        final GeneralApplication generalApplication = GeneralApplication.builder()
            .generalApplicationFee(FeeDetails.builder().paymentMethod(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT).build())
            .build();
        caseData.setGeneralApplication(generalApplication);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(true);
        when(submitPaymentService.processSubmitPayment(TEST_CASE_ID, caseData, caseData.getApplicant1()))
            .thenReturn(Optional.empty());

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitGeneralApplication.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(State.GeneralApplicationReceived);
        assertThat(response.getData().getGeneralApplication().getGeneralApplicationSubmittedOnline()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldMoveToAwaitingPaymentWhenPaymentMethodIsNotPba() {
        CaseData caseData = CaseData.builder().build();
        final GeneralApplication generalApplication = GeneralApplication.builder()
            .generalApplicationFee(FeeDetails.builder().paymentMethod(ServicePaymentMethod.FEE_PAY_BY_HWF).build())
            .build();
        caseData.setGeneralApplication(generalApplication);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(true);
        when(submitPaymentService.processSubmitPayment(TEST_CASE_ID, caseData, caseData.getApplicant2()))
            .thenReturn(Optional.empty());

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitGeneralApplication.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getState()).isEqualTo(State.AwaitingGeneralApplicationPayment);
        assertThat(response.getData().getGeneralApplication().getGeneralApplicationSubmittedOnline()).isEqualTo(YesOrNo.YES);
    }

}
