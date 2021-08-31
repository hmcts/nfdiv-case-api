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
import uk.gov.hmcts.divorce.divorcecase.model.ConfidentialAddress;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.Jurisdiction;
import uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorSubmitApplicationService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSubmitApplication.CITIZEN_SUBMIT;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
class CitizenSubmitApplicationTest {

    @Mock
    private SolicitorSubmitApplicationService solicitorSubmitApplicationService;

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
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(13);
        assertThat(response.getErrors().get(0)).isEqualTo("Applicant1FirstName cannot be empty or null");
    }

    @Test
    public void givenEventStartedWithInvalidCaseThenGiveValidationErrors() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
        setValidCaseData(caseData).getApplication().setApplicant1PrayerHasBeenGiven(YesOrNo.NO);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo("Applicant1PrayerHasBeenGiven must be YES");
    }

    @Test
    public void givenEventStartedWithValidCaseThenChangeStateAndSetOrderSummary() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
        setValidCaseData(caseData);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        var orderSummary = orderSummary();

        when(paymentService.getOrderSummary())
            .thenReturn(
                orderSummary()
            );

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.AwaitingPayment);
        assertThat(response.getData().getApplication().getApplicationFeeOrderSummary()).isEqualTo(orderSummary);

        verify(paymentService).getOrderSummary();
    }

    @Test
    public void givenEventStartedWithValidCaseThenChangeStateAwaitingHwfDecision() {
        final long caseId = 2L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
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

    private OrderSummary orderSummary() {
        return OrderSummary
            .builder()
            .paymentTotal("55000")
            .build();
    }

    private CaseData setValidCaseData(CaseData caseData) {
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setApplicant1(getApplicant());
        caseData.getApplicant1().setContactDetailsConfidential(ConfidentialAddress.KEEP);
        caseData.getApplicant1().setFinancialOrder(YesOrNo.NO);
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplication().setApplicant1HelpWithFees(
            HelpWithFees.builder()
                .needHelp(NO)
                .build()
        );

        caseData.getApplication().setApplicant1PrayerHasBeenGiven(YesOrNo.YES);
        caseData.getApplication().getMarriageDetails().setApplicant1Name("Full name");
        caseData.getApplication().setApplicant1StatementOfTruth(YesOrNo.YES);
        caseData.getApplication().getMarriageDetails().setDate(LocalDate.now().minus(2, ChronoUnit.YEARS));
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setJurisdictionConnections(Set.of(JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT));
        jurisdiction.setBothLastHabituallyResident(YesOrNo.YES);
        caseData.getApplication().setJurisdiction(jurisdiction);
        return caseData;
    }

}
