package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.service.CitizenGeneralApplicationSubmissionService;
import uk.gov.hmcts.divorce.common.service.GeneralReferralService;
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
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.citizen.event.CitizenGeneralApplicationPaymentMade.ERROR_UNABLE_TO_FIND_PAYMENT_PARTY;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.DECLINED;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PendingRefund;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;

@ExtendWith(MockitoExtension.class)
class CitizenGeneralApplicationPaymentMadeTest {
    @Mock
    private Clock clock;

    @Mock
    private CitizenGeneralApplicationSubmissionService submissionService;

    @Mock
    private GeneralReferralService generalReferralService;

    @InjectMocks
    private CitizenGeneralApplicationPaymentMade citizenGeneralApplicationPayment;

    @Test
    void shouldReturnErrorIfNoGeneralApplicationMatchesTheServiceRequest() {
        List<ListValue<Payment>> payments = singletonList(new ListValue<>("1", Payment.builder().amount(6000).status(SUCCESS).build()));

        final var beforeData = buildTestData();
        final var beforeDetails = CaseDetails.<CaseData, State>builder().data(beforeData).build();

        final var caseData = buildTestData();
        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseData.setGeneralApplications(null);
        caseData.getApplicant1().setGeneralAppPayments(payments);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenGeneralApplicationPayment.aboutToSubmit(
            caseDetails, beforeDetails
        );

        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void shouldReturnErrorIfPaymentValidationFails() {
        final var payments = List.of(new ListValue<>("1", Payment.builder().amount(6000).status(DECLINED).build()));

        final var caseData = buildTestData();
        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseData.getApplicant1().setGeneralAppPayments(payments);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenGeneralApplicationPayment.aboutToSubmit(
            caseDetails, caseDetails
        );

        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(ERROR_UNABLE_TO_FIND_PAYMENT_PARTY));
    }

    @Test
    @SuppressWarnings("unchecked")
    void givenValidPaymentMadeThenShouldSetPaymentDetailsAndSetPendingRefund() {
        setMockClock(clock);

        List<ListValue<Payment>> payments = singletonList(new ListValue<>(
            "1", Payment.builder()
            .amount(6000)
            .status(SUCCESS)
            .reference(TEST_REFERENCE)
            .serviceRequestReference(TEST_SERVICE_REFERENCE)
            .build())
        );

        final var beforeData = buildTestData();
        final var beforeDetails = CaseDetails.<CaseData, State>builder().data(beforeData).build();

        final var caseData = buildTestData();
        final var details = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setGeneralAppServiceRequest(TEST_SERVICE_REFERENCE);
        caseData.getApplicant1().setGeneralAppPayments(payments);
        details.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenGeneralApplicationPayment.aboutToSubmit(
            details, beforeDetails
        );

        GeneralApplication generalApplication = response.getData().getGeneralApplications().getFirst().getValue();

        assertThat(response.getState()).isEqualTo(PendingRefund);
        assertThat(generalApplication.getGeneralApplicationFee().getPaymentReference()).isEqualTo(TEST_REFERENCE);
    }

    @Test
    @SuppressWarnings("unchecked")
    void givenGeneralReferralAlreadyInProgressThenShouldKeepReferralAndSetPendingRefund() {
        setMockClock(clock);

        List<ListValue<Payment>> payments = singletonList(new ListValue<>(
            "1", Payment.builder()
            .amount(6000)
            .status(SUCCESS)
            .reference(TEST_REFERENCE)
            .serviceRequestReference(TEST_SERVICE_REFERENCE)
            .build())
        );

        final var beforeData = buildTestData();
        final var beforeDetails = CaseDetails.<CaseData, State>builder().data(beforeData).build();

        final var caseData = buildTestData();
        final var details = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setGeneralAppServiceRequest(TEST_SERVICE_REFERENCE);
        caseData.getApplicant1().setGeneralAppPayments(payments);
        caseData.setGeneralReferral(
            GeneralReferral.builder()
                .generalReferralReason(GeneralReferralReason.CASEWORKER_REFERRAL)
                .build()
        );
        details.setId(TEST_CASE_ID);

        GeneralApplication generalApp = caseData.getGeneralApplications().getFirst().getValue();

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenGeneralApplicationPayment.aboutToSubmit(
            details, beforeDetails
        );

        GeneralApplication generalApplication = response.getData().getGeneralApplications().getFirst().getValue();
        GeneralReferral generalReferral = response.getData().getGeneralReferral();

        assertThat(response.getState()).isEqualTo(PendingRefund);
        assertThat(generalApplication.getGeneralApplicationFee().getPaymentReference()).isEqualTo(TEST_REFERENCE);
        assertThat(generalReferral.getGeneralReferralReason()).isEqualTo(GeneralReferralReason.CASEWORKER_REFERRAL);
    }

    @Test
    @SuppressWarnings("unchecked")
    void givenPaymentForExpiredGeneralApplicationThenShouldMoveCaseToPendingRefund() {
        setMockClock(clock);

        List<ListValue<Payment>> successfulPayments = singletonList(new ListValue<>(
            "1", Payment.builder()
            .amount(6000)
            .status(SUCCESS)
            .reference(TEST_REFERENCE)
            .serviceRequestReference(TEST_SERVICE_REFERENCE)
            .build())
        );

        final var beforeData = buildTestData();
        beforeData.getApplicant1().setGeneralAppServiceRequest(null);
        beforeData.getApplicant1().setGeneralAppPayments(List.of(
            ListValue.<Payment>builder().value(Payment.builder()
                .status(PaymentStatus.IN_PROGRESS)
                .serviceRequestReference(TEST_SERVICE_REFERENCE)
                .build()).build()
        ));
        final var beforeDetails = CaseDetails.<CaseData, State>builder().data(beforeData).build();

        final var caseData = buildTestData();
        caseData.getApplicant1().setGeneralAppServiceRequest(null);
        caseData.getApplicant1().setGeneralAppPayments(successfulPayments);
        final var details = CaseDetails.<CaseData, State>builder().data(caseData).id(TEST_CASE_ID).build();

        GeneralApplication generalApp = caseData.getGeneralApplications().getFirst().getValue();

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenGeneralApplicationPayment.aboutToSubmit(
            details, beforeDetails
        );

        assertThat(response.getState()).isEqualTo(PendingRefund);
        assertThat(generalApp.getGeneralApplicationFee().getPaymentReference()).isEqualTo(TEST_REFERENCE);
    }

    @Test
    void shouldTriggerNotificationsByDelegatingToDispatcher() {
        final var caseData = buildTestData();
        caseData.getApplicant1().setGeneralAppServiceRequest(null);
        final var details = CaseDetails.<CaseData, State>builder()
            .data(caseData)
            .id(TEST_CASE_ID)
            .build();
        final var beforeData = buildTestData();
        final var beforeDetails = CaseDetails.<CaseData, State>builder()
            .data(beforeData)
            .id(TEST_CASE_ID)
            .build();
        beforeDetails.setState(AwaitingPronouncement);

        GeneralApplication generalApplication = caseData.getGeneralApplications().getFirst().getValue();

        when(submissionService.findActiveGeneralApplication(caseData, beforeData.getApplicant1()))
            .thenReturn(Optional.of(generalApplication));

        citizenGeneralApplicationPayment.submitted(details, beforeDetails);

        verify(submissionService).sendNotifications(
            TEST_CASE_ID, generalApplication, caseData
        );
        assertThat(details.getData().getApplication().getPreviousState()).isEqualTo(AwaitingPronouncement);
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
            .generalApplicationReceivedDate(LocalDateTime.now())
            .build();

        return CaseData.builder()
            .applicant1(Applicant.builder()
                .generalAppServiceRequest(TEST_SERVICE_REFERENCE)
                .generalAppPayments(List.of(
                    ListValue.<Payment>builder().value(Payment.builder().status(PaymentStatus.IN_PROGRESS).build()).build()
                ))
                .build())
            .generalApplications(List.of(
                ListValue.<GeneralApplication>builder()
                    .value(generalApplication)
                    .build()
            )).build();
    }
}
