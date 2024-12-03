package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.service.SubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.Jurisdiction;
import uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorSubmitJointApplicationService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSubmitApplication.CITIZEN_SUBMIT;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.DissolveDivorce.DISSOLVE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_ISSUE;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_DIVORCE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_DIVORCE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
class CitizenSubmitApplicationTest {

    @Mock
    private SolicitorSubmitJointApplicationService solicitorSubmitJointApplicationService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private SubmissionService submissionService;

    @InjectMocks
    private CitizenSubmitApplication citizenSubmitApplication;
    private OrderSummary orderSummary;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenSubmitApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_SUBMIT);
    }

    @Test
    public void givenEventStartedWithEmptyCaseThenGiveValidationErrors() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();
        caseData.getApplicant2().setEmail("onlineApplicant2@email.com");
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(14);
        assertThat(response.getErrors()).contains("Applicant1FirstName cannot be empty or null");
        assertThat(response.getErrors()).contains("ApplicationType cannot be empty or null");
    }

    @Test
    public void givenEventStartedWithInvalidCaseThenGiveValidationErrors() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();
        setValidCaseData(caseData);

        caseData.getApplicant1().getApplicantPrayer().setPrayerDissolveDivorce(emptySet());

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo("Applicant 1 must confirm prayer to dissolve their marriage (get a divorce)");
    }

    @Test
    public void givenEventStartedWithValidCaseThenChangeStateAndSetOrderSummary() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();
        setValidCaseData(caseData);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        var orderSummary = orderSummary();

        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_DIVORCE, EVENT_ISSUE, KEYWORD_DIVORCE))
            .thenReturn(orderSummary());

        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_DIVORCE, EVENT_ISSUE, KEYWORD_DIVORCE))
            .thenReturn(orderSummary());

        when(paymentService.createServiceRequestReference(
            null, caseId, caseData.getApplicant1().getFullName(), orderSummary
        )).thenReturn(TEST_SERVICE_REFERENCE);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.AwaitingPayment);
        assertThat(response.getData().getApplication().getApplicationFeeOrderSummary()).isEqualTo(orderSummary);
        assertThat(response.getData().getApplication().getApplicationFeeServiceRequestReference()).isEqualTo(TEST_SERVICE_REFERENCE);

        verify(paymentService).getOrderSummaryByServiceEvent(SERVICE_DIVORCE, EVENT_ISSUE, KEYWORD_DIVORCE);
    }

    @Test
    public void givenEventStartedWithValidJointCaseThenChangeStateAndSetOrderSummary() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();
        setValidCaseData(caseData);

        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.getApplicant2().setFirstName("App");
        caseData.getApplicant2().setLastName("Two");
        caseData.getApplication().setApplicant2StatementOfTruth(YES);
        caseData.getApplicant2().getApplicantPrayer().setPrayerDissolveDivorce(Set.of(DISSOLVE_DIVORCE));
        caseData.getApplication().getMarriageDetails().setApplicant2Name("App Two");

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        var orderSummary = orderSummary();

        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_DIVORCE, EVENT_ISSUE, KEYWORD_DIVORCE))
            .thenReturn(orderSummary());

        when(paymentService.createServiceRequestReference(
            null, caseId, caseData.getApplicant1().getFullName(), orderSummary
        )).thenReturn(TEST_SERVICE_REFERENCE);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.AwaitingPayment);
        assertThat(response.getData().getApplication().getApplicationFeeOrderSummary()).isEqualTo(orderSummary);
        assertThat(response.getData().getApplication().getApplicationFeeServiceRequestReference()).isEqualTo(TEST_SERVICE_REFERENCE);

        verify(paymentService).getOrderSummaryByServiceEvent(SERVICE_DIVORCE, EVENT_ISSUE, KEYWORD_DIVORCE);
    }

    @Test
    public void givenEventStartedWithValidCaseThenChangeStateAwaitingHwfDecision() {
        final long caseId = 2L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();
        setValidCaseData(caseData).getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final CaseDetails<CaseData, State> newDetails = new CaseDetails<>();
        newDetails.setState(State.AwaitingHWFDecision);
        newDetails.setData(caseData);

        when(submissionService.submitApplication(caseDetails))
            .thenReturn(newDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.AwaitingHWFDecision);
        assertThat(response.getData().getApplication().getApplicationPayments()).isNull();
    }

    @Test
    public void givenEventStartedWithValidJointCaseThenChangeStateAwaitingHwfDecision() {
        final long caseId = 2L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();
        setValidCaseData(caseData).getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.getApplicant2().setFirstName("App");
        caseData.getApplicant2().setLastName("Two");
        caseData.getApplication().setApplicant2HelpWithFees(
            HelpWithFees
                .builder()
                .needHelp(YES)
                .build()
        );
        caseData.getApplication().setApplicant2StatementOfTruth(YES);
        caseData.getApplicant2().getApplicantPrayer().setPrayerDissolveDivorce(Set.of(DISSOLVE_DIVORCE));
        caseData.getApplication().getMarriageDetails().setApplicant2Name("App Two");

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final CaseDetails<CaseData, State> newDetails = new CaseDetails<>();
        newDetails.setState(State.AwaitingHWFDecision);
        newDetails.setData(caseData);

        when(submissionService.submitApplication(caseDetails))
            .thenReturn(newDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.AwaitingHWFDecision);
        assertThat(response.getData().getApplication().getApplicationPayments()).isNull();
    }

    private OrderSummary orderSummary() {
        return OrderSummary
            .builder()
            .paymentTotal("55000")
            .build();
    }

    private CaseData setValidCaseData(CaseData caseData) {
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setApplicant1(getApplicant());
        caseData.getApplicant1().setContactDetailsType(PRIVATE);
        caseData.getApplicant1().setFinancialOrder(YesOrNo.NO);
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplication().setApplicant1HelpWithFees(
            HelpWithFees.builder()
                .needHelp(NO)
                .build()
        );

        caseData.getApplicant1().getApplicantPrayer().setPrayerDissolveDivorce(Set.of(DISSOLVE_DIVORCE));
        caseData.getApplication().getMarriageDetails().setApplicant1Name("Full name");
        caseData.getApplication().setApplicant1StatementOfTruth(YesOrNo.YES);
        caseData.getApplication().getMarriageDetails().setDate(LocalDate.now().minus(2, ChronoUnit.YEARS));
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT));
        jurisdiction.setBothLastHabituallyResident(YesOrNo.YES);
        caseData.getApplication().setJurisdiction(jurisdiction);
        return caseData;
    }
}
