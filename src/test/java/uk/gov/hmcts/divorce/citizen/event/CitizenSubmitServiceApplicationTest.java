package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.service.InterimApplicationSubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceDifferentWays;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceMediumType;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceMethod;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DeemedServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSubmitServiceApplication.AWAITING_DECISION_ERROR;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;

@ExtendWith(MockitoExtension.class)
class CitizenSubmitServiceApplicationTest {
    @Mock
    private PaymentSetupService paymentSetupService;

    @Mock
    private Clock clock;

    @Mock
    InterimApplicationSubmissionService interimApplicationSubmissionService;

    @Mock
    DocumentRemovalService documentRemovalService;

    @InjectMocks
    private CitizenSubmitServiceApplication citizenSubmitServiceApplication;

    private OrderSummary orderSummary;

    @Test
    void givenServiceApplicationInProgressThenRejectNewSubmission() {
        CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .alternativeServiceType(AlternativeServiceType.ALTERNATIVE_SERVICE)
                .build()
            ).build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);


        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitServiceApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(AWAITING_DECISION_ERROR));
    }

    @Test
    void givenCitizenWillMakePaymentThenChangeStateToAwaitingServicePaymentAndSetOrderSummary() {
        setMockClock(clock);

        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .firstName(TEST_FIRST_NAME)
                    .interimApplicationOptions(InterimApplicationOptions.builder()
                        .interimAppsUseHelpWithFees(YesOrNo.NO)
                        .interimAppsCannotUploadDocs(YesOrNo.YES)
                        .interimApplicationType(InterimApplicationType.DEEMED_SERVICE)
                        .deemedServiceJourneyOptions(DeemedServiceJourneyOptions.builder().build())
                        .build())
                    .build()
            ).build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        when(paymentSetupService.createServiceApplicationOrderSummary(any(AlternativeService.class), eq(TEST_CASE_ID)))
            .thenReturn(orderSummary);

        when(paymentSetupService.createServiceApplicationPaymentServiceRequest(
            any(AlternativeService.class), eq(TEST_CASE_ID), eq(TEST_FIRST_NAME)
        )).thenReturn(TEST_SERVICE_REFERENCE);

        DivorceDocument generatedApplication = DivorceDocument.builder().build();
        when(interimApplicationSubmissionService.generateAnswerDocument(
            TEST_CASE_ID, caseData.getApplicant1(), caseData
        )).thenReturn(generatedApplication);


        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitServiceApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        AlternativeService alternativeService = response.getData().getAlternativeService();
        assertThat(response.getState()).isEqualTo(State.AwaitingServicePayment);
        assertThat(alternativeService.getServiceApplicationAnswers()).isEqualTo(generatedApplication);
        assertThat(alternativeService.getServicePaymentFee().getOrderSummary()).isEqualTo(orderSummary);
        assertThat(alternativeService.getServicePaymentFee().getServiceRequestReference()).isEqualTo(TEST_SERVICE_REFERENCE);
        assertThat(alternativeService.getAlternativeServiceFeeRequired()).isEqualTo(YesOrNo.YES);
        assertThat(alternativeService.getServiceApplicationSubmittedOnline()).isEqualTo(YesOrNo.YES);
        assertThat(alternativeService.getServiceApplicationDocsUploadedPreSubmission()).isEqualTo(YesOrNo.NO);
        assertThat(alternativeService.getAlternativeServiceType()).isEqualTo(AlternativeServiceType.DEEMED);
        assertThat(alternativeService.getAlternativeServiceMediumSelected()).isNull();
    }

    @Test
    void givenCitizenWillNotMakePaymentButAllDocsHaveBeenSubmittedThenChangeStateToAwaitingServicePayment() {
        setMockClock(clock);

        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .firstName(TEST_FIRST_NAME)
                    .interimApplicationOptions(InterimApplicationOptions.builder()
                        .interimAppsUseHelpWithFees(YesOrNo.YES)
                        .interimAppsCannotUploadDocs(YesOrNo.NO)
                        .interimApplicationType(InterimApplicationType.DEEMED_SERVICE)
                        .deemedServiceJourneyOptions(DeemedServiceJourneyOptions.builder().build())
                        .build())
                    .build()
            ).build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        DivorceDocument generatedApplication = DivorceDocument.builder().build();
        when(interimApplicationSubmissionService.generateAnswerDocument(
            TEST_CASE_ID, caseData.getApplicant1(), caseData
        )).thenReturn(generatedApplication);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitServiceApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        AlternativeService alternativeService = response.getData().getAlternativeService();
        assertThat(response.getState()).isEqualTo(State.AwaitingServicePayment);
        assertThat(alternativeService.getServiceApplicationAnswers()).isEqualTo(generatedApplication);
        assertThat(alternativeService.getServicePaymentFee().getOrderSummary()).isNull();
        assertThat(alternativeService.getServicePaymentFee().getServiceRequestReference()).isNull();
        assertThat(alternativeService.getAlternativeServiceFeeRequired()).isNotEqualTo(YesOrNo.YES);
        assertThat(alternativeService.getServiceApplicationSubmittedOnline()).isEqualTo(YesOrNo.YES);
        assertThat(alternativeService.getServiceApplicationDocsUploadedPreSubmission()).isEqualTo(YesOrNo.YES);
        assertThat(alternativeService.getAlternativeServiceType()).isEqualTo(AlternativeServiceType.DEEMED);
        assertThat(alternativeService.getAlternativeServiceMediumSelected()).isNull();
    }

    @Test
    void givenCitizenWillNotMakePaymentButDocsHaveNotBeenSubmittedThenChangeStateToAwaitingApplicant() {
        setMockClock(clock);

        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .firstName(TEST_FIRST_NAME)
                    .interimApplicationOptions(InterimApplicationOptions.builder()
                        .interimAppsUseHelpWithFees(YesOrNo.YES)
                        .interimAppsCannotUploadDocs(YesOrNo.YES)
                        .interimApplicationType(InterimApplicationType.DEEMED_SERVICE)
                        .deemedServiceJourneyOptions(DeemedServiceJourneyOptions.builder().build())
                        .build())
                    .build()
            ).build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        DivorceDocument generatedApplication = DivorceDocument.builder().build();
        when(interimApplicationSubmissionService.generateAnswerDocument(
            TEST_CASE_ID, caseData.getApplicant1(), caseData
        )).thenReturn(generatedApplication);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitServiceApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        AlternativeService alternativeService = response.getData().getAlternativeService();
        assertThat(response.getState()).isEqualTo(State.AwaitingDocuments);
        assertThat(alternativeService.getServiceApplicationAnswers()).isEqualTo(generatedApplication);
        assertThat(alternativeService.getServicePaymentFee().getOrderSummary()).isNull();
        assertThat(alternativeService.getServicePaymentFee().getServiceRequestReference()).isNull();
        assertThat(alternativeService.getAlternativeServiceFeeRequired()).isNotEqualTo(YesOrNo.YES);
        assertThat(alternativeService.getServiceApplicationSubmittedOnline()).isEqualTo(YesOrNo.YES);
        assertThat(alternativeService.getServiceApplicationDocsUploadedPreSubmission()).isEqualTo(YesOrNo.NO);
        assertThat(alternativeService.getAlternativeServiceType()).isEqualTo(AlternativeServiceType.DEEMED);
        assertThat(alternativeService.getAlternativeServiceMediumSelected()).isNull();
    }

    @Test
    void shouldDeleteEvidenceDocsIfUserIndicatedTheyWontProvideEvidence() {
        setMockClock(clock);

        var evidenceDocs = List.of(
            ListValue.<DivorceDocument>builder()
                .value(DivorceDocument.builder().build())
                .build()
        );

        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .firstName(TEST_FIRST_NAME)
                    .interimApplicationOptions(InterimApplicationOptions.builder()
                        .interimAppsCanUploadEvidence(YesOrNo.NO)
                        .interimAppsEvidenceDocs(evidenceDocs)
                        .interimAppsUseHelpWithFees(YesOrNo.YES)
                        .interimAppsCannotUploadDocs(YesOrNo.YES)
                        .interimApplicationType(InterimApplicationType.DEEMED_SERVICE)
                        .deemedServiceJourneyOptions(DeemedServiceJourneyOptions.builder().build())
                        .build())
                    .build()
            ).build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        DivorceDocument generatedApplication = DivorceDocument.builder().build();
        when(interimApplicationSubmissionService.generateAnswerDocument(
            TEST_CASE_ID, caseData.getApplicant1(), caseData
        )).thenReturn(generatedApplication);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitServiceApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        verify(documentRemovalService).deleteDocument(evidenceDocs);

        AlternativeService alternativeService = response.getData().getAlternativeService();
        assertThat(response.getState()).isEqualTo(State.AwaitingDocuments);
        assertThat(alternativeService.getServiceApplicationDocuments()).isEqualTo(null);
    }


    @Test
    void shouldTriggerNotificationsIfApplicationSubmittedWithHelpWithFees() {
        CaseData caseData = CaseData.builder().build();
        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        caseData.setAlternativeService(
            AlternativeService.builder()
                .alternativeServiceFeeRequired(YesOrNo.NO)
                .alternativeServiceType(AlternativeServiceType.DEEMED)
                .build()
        );

        citizenSubmitServiceApplication.submitted(caseDetails, caseDetails);

        verify(interimApplicationSubmissionService).sendNotifications(
            TEST_CASE_ID, AlternativeServiceType.DEEMED, caseData
        );
    }

    @Test
    void shouldNotTriggerNotificationsIfApplicationRequiresPayment() {
        CaseData caseData = CaseData.builder().build();
        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        caseData.setAlternativeService(
            AlternativeService.builder()
                .alternativeServiceFeeRequired(YesOrNo.YES)
                .alternativeServiceType(AlternativeServiceType.DEEMED)
                .build()
        );

        citizenSubmitServiceApplication.submitted(caseDetails, caseDetails);

        verifyNoInteractions(interimApplicationSubmissionService);
    }

    @Test
    void shouldSetAlternativeServiceFieldsWhenServiceIsAlternativeServiceAndMethodIsEmail() {
        setMockClock(clock);

        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .firstName(TEST_FIRST_NAME)
                    .interimApplicationOptions(InterimApplicationOptions.builder()
                        .interimAppsUseHelpWithFees(YesOrNo.YES)
                        .interimAppsCannotUploadDocs(YesOrNo.YES)
                        .interimApplicationType(InterimApplicationType.ALTERNATIVE_SERVICE)
                        .alternativeServiceJourneyOptions(
                            AlternativeServiceJourneyOptions.builder()
                                .altServiceMethod(AlternativeServiceMethod.EMAIL)
                                .altServiceDifferentWays(Set.of(AlternativeServiceDifferentWays.TEXT_MESSAGE))
                                .build())
                        .build())
                    .build()
            ).build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        DivorceDocument generatedApplication = DivorceDocument.builder().build();
        when(interimApplicationSubmissionService.generateAnswerDocument(
            TEST_CASE_ID, caseData.getApplicant1(), caseData
        )).thenReturn(generatedApplication);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitServiceApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        AlternativeService alternativeService = response.getData().getAlternativeService();
        assertThat(response.getState()).isEqualTo(State.AwaitingDocuments);
        assertThat(alternativeService.getServiceApplicationAnswers()).isEqualTo(generatedApplication);
        assertThat(alternativeService.getServicePaymentFee().getOrderSummary()).isNull();
        assertThat(alternativeService.getServicePaymentFee().getServiceRequestReference()).isNull();
        assertThat(alternativeService.getAlternativeServiceFeeRequired()).isNotEqualTo(YesOrNo.YES);
        assertThat(alternativeService.getServiceApplicationSubmittedOnline()).isEqualTo(YesOrNo.YES);
        assertThat(alternativeService.getServiceApplicationDocsUploadedPreSubmission()).isEqualTo(YesOrNo.NO);
        assertThat(alternativeService.getAlternativeServiceType()).isEqualTo(AlternativeServiceType.ALTERNATIVE_SERVICE);
        assertThat(alternativeService.getAlternativeServiceMediumSelected()).containsExactly(AlternativeServiceMediumType.EMAIL);
    }

    @Test
    void shouldSetAlternativeServiceFieldsWhenServiceIsAlternativeServiceAndMethodIsEmailAndDifferent() {
        setMockClock(clock);

        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .firstName(TEST_FIRST_NAME)
                    .interimApplicationOptions(InterimApplicationOptions.builder()
                        .interimAppsUseHelpWithFees(YesOrNo.YES)
                        .interimAppsCannotUploadDocs(YesOrNo.YES)
                        .interimApplicationType(InterimApplicationType.ALTERNATIVE_SERVICE)
                        .alternativeServiceJourneyOptions(
                            AlternativeServiceJourneyOptions.builder()
                                .altServiceMethod(AlternativeServiceMethod.EMAIL_AND_DIFFERENT)
                                .altServiceDifferentWays(Set.of(AlternativeServiceDifferentWays.TEXT_MESSAGE,
                                    AlternativeServiceDifferentWays.OTHER))
                                .build())
                        .build())
                    .build()
            ).build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        DivorceDocument generatedApplication = DivorceDocument.builder().build();
        when(interimApplicationSubmissionService.generateAnswerDocument(
            TEST_CASE_ID, caseData.getApplicant1(), caseData
        )).thenReturn(generatedApplication);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitServiceApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        AlternativeService alternativeService = response.getData().getAlternativeService();
        assertThat(response.getState()).isEqualTo(State.AwaitingDocuments);
        assertThat(alternativeService.getServiceApplicationAnswers()).isEqualTo(generatedApplication);
        assertThat(alternativeService.getServicePaymentFee().getOrderSummary()).isNull();
        assertThat(alternativeService.getServicePaymentFee().getServiceRequestReference()).isNull();
        assertThat(alternativeService.getAlternativeServiceFeeRequired()).isNotEqualTo(YesOrNo.YES);
        assertThat(alternativeService.getServiceApplicationSubmittedOnline()).isEqualTo(YesOrNo.YES);
        assertThat(alternativeService.getServiceApplicationDocsUploadedPreSubmission()).isEqualTo(YesOrNo.NO);
        assertThat(alternativeService.getAlternativeServiceType()).isEqualTo(AlternativeServiceType.ALTERNATIVE_SERVICE);
        assertThat(alternativeService.getAlternativeServiceMediumSelected().size()).isEqualTo(3);
        assertThat(alternativeService.getAlternativeServiceMediumSelected())
            .containsExactlyInAnyOrder(AlternativeServiceMediumType.EMAIL,
                AlternativeServiceMediumType.TEXT, AlternativeServiceMediumType.OTHER);
    }
}
