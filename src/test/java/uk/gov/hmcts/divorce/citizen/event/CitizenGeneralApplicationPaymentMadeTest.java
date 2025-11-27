package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.service.InterimApplicationSubmissionService;
import uk.gov.hmcts.divorce.common.service.PaymentValidatorService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralReason;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralType;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.service.PaymentValidatorService.ERROR_PAYMENT_INCOMPLETE;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.DECLINED;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;

@ExtendWith(MockitoExtension.class)
class CitizenGeneralApplicationPaymentMadeTest {
    @Mock
    private PaymentValidatorService paymentValidatorService;

    @Mock
    private Clock clock;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private InterimApplicationSubmissionService interimApplicationSubmissionService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private CitizenGeneralApplicationPaymentMade citizenGeneralApplicationPayment;

    @BeforeEach
    void stubDependencies() {
        when(request.getHeader(AUTHORIZATION)).thenReturn(AUTHORIZATION);
        when(ccdAccessService.isApplicant1(AUTHORIZATION, TEST_CASE_ID)).thenReturn(true);
    }

    @Test
    void shouldReturnErrorIfNoGeneralApplicationMatchesTheServiceRequest() {
        List<ListValue<Payment>> payments = singletonList(new ListValue<>("1", Payment.builder().amount(6000).status(DECLINED).build()));

        final var caseData = buildTestData();
        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseData.setGeneralApplications(null);
        caseData.getApplicant1().setGeneralAppPayments(payments);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenGeneralApplicationPayment.aboutToSubmit(
            caseDetails, caseDetails
        );

        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void shouldReturnErrorIfPaymentValidationFails() {
        List<ListValue<Payment>> payments = singletonList(new ListValue<>("1", Payment.builder().amount(6000).status(DECLINED).build()));

        final var caseData = buildTestData();
        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseData.getApplicant1().setGeneralAppPayments(payments);
        caseDetails.setId(TEST_CASE_ID);

        when(paymentValidatorService.validatePayments(payments, TEST_CASE_ID))
            .thenReturn(Collections.singletonList(ERROR_PAYMENT_INCOMPLETE));

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenGeneralApplicationPayment.aboutToSubmit(
            caseDetails, caseDetails
        );

        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(ERROR_PAYMENT_INCOMPLETE));
    }

    @Test
    void givenValidPaymentMadeThenShouldSetPaymentDetailsAndMakeGeneralReferral() {
        setMockClock(clock);

        List<ListValue<Payment>> payments = singletonList(new ListValue<>(
            "1", Payment.builder().amount(6000).status(SUCCESS).reference(TEST_REFERENCE).build())
        );

        final var caseData = buildTestData();
        final var details = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setGeneralAppPayments(payments);
        details.setId(TEST_CASE_ID);

        when(paymentValidatorService.validatePayments(payments, TEST_CASE_ID)).thenReturn(
            Collections.emptyList()
        );
        when(paymentValidatorService.getLastPayment(payments)).thenReturn(payments.getLast().getValue());

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenGeneralApplicationPayment.aboutToSubmit(details, details);

        GeneralApplication generalApplication = response.getData().getGeneralApplications().getFirst().getValue();
        GeneralReferral generalReferral = response.getData().getGeneralReferral();

        assertThat(response.getState()).isEqualTo(AwaitingGeneralConsideration);
        assertThat(generalApplication.getGeneralApplicationFee().getPaymentReference()).isEqualTo(TEST_REFERENCE);
        assertThat(generalReferral.getGeneralReferralReason()).isEqualTo(GeneralReferralReason.GENERAL_APPLICATION_REFERRAL);
        assertThat(generalReferral.getGeneralReferralType()).isEqualTo(GeneralReferralType.DISCLOSURE_VIA_DWP);
        assertThat(generalReferral.getGeneralReferralDocument()).isEqualTo(generalApplication.getGeneralApplicationDocument());
        assertThat(generalReferral.getGeneralReferralDocuments()).isEqualTo(generalApplication.getGeneralApplicationDocuments());
    }

    @Test
    void givenValidPaymentMadeForWelshApplicationThenShouldSetStateToWelshTranslationReview() {
        setMockClock(clock);

        List<ListValue<Payment>> payments = singletonList(new ListValue<>(
            "1", Payment.builder().amount(6000).status(SUCCESS).reference(TEST_REFERENCE).build())
        );

        final var caseData = buildTestData();
        final var details = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setGeneralAppPayments(payments);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        details.setId(TEST_CASE_ID);

        when(paymentValidatorService.validatePayments(payments, TEST_CASE_ID)).thenReturn(
            Collections.emptyList()
        );
        when(paymentValidatorService.getLastPayment(payments)).thenReturn(payments.getLast().getValue());

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenGeneralApplicationPayment.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(WelshTranslationReview);
        assertThat(response.getData().getApplication().getWelshPreviousState()).isEqualTo(AwaitingGeneralConsideration);
    }

    @Test
    void givenGeneralReferralAlreadyInProgressThenShouldNotCreateNewReferral() {
        setMockClock(clock);

        List<ListValue<Payment>> payments = singletonList(new ListValue<>(
            "1", Payment.builder().amount(6000).status(SUCCESS).reference(TEST_REFERENCE).build())
        );

        final var caseData = buildTestData();
        final var details = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setGeneralAppPayments(payments);
        caseData.setGeneralReferral(
            GeneralReferral.builder()
                .generalReferralReason(GeneralReferralReason.CASEWORKER_REFERRAL)
                .build()
        );
        details.setId(TEST_CASE_ID);

        when(paymentValidatorService.validatePayments(payments, TEST_CASE_ID)).thenReturn(
            Collections.emptyList()
        );
        when(paymentValidatorService.getLastPayment(payments)).thenReturn(payments.getLast().getValue());

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenGeneralApplicationPayment.aboutToSubmit(details, details);

        GeneralApplication generalApplication = response.getData().getGeneralApplications().getFirst().getValue();
        GeneralReferral generalReferral = response.getData().getGeneralReferral();

        assertThat(response.getState()).isEqualTo(GeneralApplicationReceived);
        assertThat(generalApplication.getGeneralApplicationFee().getPaymentReference()).isEqualTo(TEST_REFERENCE);
        assertThat(generalReferral.getGeneralReferralReason()).isEqualTo(GeneralReferralReason.CASEWORKER_REFERRAL);
    }

    @Test
    void shouldTriggerNotificationsByDelegatingToDispatcher() {
        final var caseData = buildTestData();
        final var details = CaseDetails.<CaseData, State>builder()
            .data(caseData)
            .id(TEST_CASE_ID)
            .build();
        final var beforeData = buildTestData();
        final var beforeDetails = CaseDetails.<CaseData, State>builder()
            .data(beforeData)
            .id(TEST_CASE_ID)
            .build();

        citizenGeneralApplicationPayment.submitted(details, beforeDetails);

        verify(interimApplicationSubmissionService).sendGeneralApplicationNotifications(
            TEST_CASE_ID, caseData.getGeneralApplications().getFirst().getValue(), caseData
        );
    }

    private CaseData buildTestData() {
        GeneralApplication generalApplication = GeneralApplication.builder()
            .generalApplicationParty(GeneralParties.APPLICANT)
            .generalApplicationType(GeneralApplicationType.DISCLOSURE_VIA_DWP)
            .generalApplicationDocument(DivorceDocument.builder().build())
            .generalApplicationDocuments(List.of(
                ListValue.<DivorceDocument>builder().value(DivorceDocument.builder().build()).build()
            ))
            .generalApplicationFee(FeeDetails.builder()
                .paymentMethod(ServicePaymentMethod.FEE_PAY_BY_CARD)
                .serviceRequestReference(TEST_SERVICE_REFERENCE)
                .build())
            .generalApplicationReceivedDate(LocalDateTime.of(2020, 1, 1, 1, 1, 1))
            .build();

        return CaseData.builder()
            .applicant1(Applicant.builder()
                .generalAppServiceRequest(TEST_SERVICE_REFERENCE)
                .build())
            .generalApplications(List.of(
                ListValue.<GeneralApplication>builder()
                    .value(generalApplication)
                    .build()
            )).build();
    }
}
