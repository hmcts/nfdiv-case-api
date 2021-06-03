package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.ConfidentialAddress;
import uk.gov.hmcts.divorce.common.model.Gender;
import uk.gov.hmcts.divorce.common.model.Jurisdiction;
import uk.gov.hmcts.divorce.common.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorSubmitApplicationService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSubmitApplication.CITIZEN_SUBMIT;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.IN_PROGRESS;

@ExtendWith(MockitoExtension.class)
class CitizenSubmitApplicationTest {

    @Mock
    private SolicitorSubmitApplicationService solicitorSubmitApplicationService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private CitizenSubmitApplication citizenSubmitApplication;
    private OrderSummary orderSummary;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        citizenSubmitApplication.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId()).isEqualTo(CITIZEN_SUBMIT);
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
        setValidCaseData(caseData).setPrayerHasBeenGiven(YesOrNo.NO);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo("PrayerHasBeenGiven must be YES");
    }

    @Test
    public void givenEventStartedWithValidCaseThenChangeStateAndSetOrderSummaryAndPendingPayment() {
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
        assertThat(response.getData().getApplicationFeeOrderSummary()).isEqualTo(orderSummary);
        assertThat(response.getData().getPayments())
            .usingElementComparatorIgnoringFields("id") // id is random uuid
            .containsExactlyInAnyOrder(pendingPayment());

        verify(paymentService).getOrderSummary();
    }

    @Test
    public void givenEventStartedWithValidCaseThenChangeStateAwaitingHwfDecision() {
        final long caseId = 2L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
        setValidCaseData(caseData).setHelpWithFeesAppliedForFees(YesOrNo.YES);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.AwaitingHWFDecision);
        assertThat(response.getData().getPayments()).isNull();
    }

    private OrderSummary orderSummary() {
        return OrderSummary
            .builder()
            .paymentTotal("55000")
            .build();
    }

    private CaseData setValidCaseData(CaseData caseData) {
        caseData.setApplicant1FirstName("First Name");
        caseData.setApplicant1LastName("Last Name");
        caseData.setApplicant2FirstName("First Name");
        caseData.setApplicant2LastName("Last Name");
        caseData.setFinancialOrder(YesOrNo.NO);
        caseData.setHelpWithFeesAppliedForFees(YesOrNo.NO);
        caseData.setInferredApplicant1Gender(Gender.FEMALE);
        caseData.setInferredApplicant2Gender(Gender.MALE);
        caseData.setApplicant1ContactDetailsConfidential(ConfidentialAddress.KEEP);
        caseData.setPrayerHasBeenGiven(YesOrNo.YES);
        caseData.setMarriageApplicant1Name("Full name");
        caseData.setStatementOfTruth(YesOrNo.YES);
        caseData.getMarriageDetails().setDate(LocalDate.now().minus(2, ChronoUnit.YEARS));
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT));
        jurisdiction.setBothLastHabituallyResident(YesOrNo.YES);
        caseData.setJurisdiction(jurisdiction);
        return caseData;
    }

    private ListValue<Payment> pendingPayment() {
        Payment payment = Payment
            .builder()
            .paymentAmount(55000)
            .paymentStatus(IN_PROGRESS)
            .build();

        ListValue<Payment> listValuePayment = ListValue
            .<Payment>builder()
            .value(payment)
            .id(UUID.randomUUID().toString())
            .build();
        return listValuePayment;
    }

}
