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
import uk.gov.hmcts.divorce.common.service.InterimApplicationSubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DeemedServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSubmitServiceApplication.AWAITING_DECISION_ERROR;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServicePayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
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
    void givenAosIsSubmittedThenRejectServiceApplicationSubmission() {
        CaseData caseData = CaseData.builder().build();
        caseData.getAcknowledgementOfService().setDateAosSubmitted(
            LocalDateTime.of(2021, 10, 26, 10, 0, 0));

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);


        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitServiceApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        assertThat(response.getErrors()).isEqualTo(Collections.singletonList("Partner has responded to application."));
    }

    @Test
    void givenCitizenWillMakePaymentThenChangeStateToAwaitingServicePaymentAndSetOrderSummary() {
        setMockClock(clock);

        CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(
                Applicant.builder()
                    .firstName(TEST_FIRST_NAME)
                    .interimApplicationOptions(InterimApplicationOptions.builder()
                        .interimAppsUseHelpWithFees(NO)
                        .interimAppsCannotUploadDocs(YES)
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
        when(interimApplicationSubmissionService.generateServiceApplicationAnswerDocument(
            TEST_CASE_ID, caseData.getApplicant1(), caseData
        )).thenReturn(generatedApplication);


        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitServiceApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        AlternativeService alternativeService = response.getData().getAlternativeService();
        assertThat(response.getState()).isEqualTo(AwaitingServicePayment);
        assertThat(alternativeService.getServiceApplicationAnswers()).isEqualTo(generatedApplication);
        assertThat(alternativeService.getServicePaymentFee().getOrderSummary()).isEqualTo(orderSummary);
        assertThat(alternativeService.getServicePaymentFee().getServiceRequestReference()).isEqualTo(TEST_SERVICE_REFERENCE);
        assertThat(alternativeService.getAlternativeServiceFeeRequired()).isEqualTo(YES);
        assertThat(alternativeService.getServicePaymentFee().getPaymentMethod())
            .isEqualTo(ServicePaymentMethod.FEE_PAY_BY_CARD);
        assertThat(alternativeService.getServiceApplicationSubmittedOnline()).isEqualTo(YES);
        assertThat(alternativeService.getServiceApplicationDocsUploadedPreSubmission()).isEqualTo(NO);
        assertThat(alternativeService.getAlternativeServiceType()).isEqualTo(AlternativeServiceType.DEEMED);
    }

    @Test
    void givenCitizenWillNotMakePaymentButAllDocsHaveBeenSubmittedThenChangeStateToAwaitingServicePayment() {
        setMockClock(clock);

        CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(
                Applicant.builder()
                    .firstName(TEST_FIRST_NAME)
                    .interimApplicationOptions(InterimApplicationOptions.builder()
                        .interimAppsUseHelpWithFees(YES)
                        .interimAppsCannotUploadDocs(NO)
                        .interimApplicationType(InterimApplicationType.DEEMED_SERVICE)
                        .deemedServiceJourneyOptions(DeemedServiceJourneyOptions.builder().build())
                        .build())
                    .build()
            ).build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        DivorceDocument generatedApplication = DivorceDocument.builder().build();
        when(interimApplicationSubmissionService.generateServiceApplicationAnswerDocument(
            TEST_CASE_ID, caseData.getApplicant1(), caseData
        )).thenReturn(generatedApplication);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitServiceApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        AlternativeService alternativeService = response.getData().getAlternativeService();
        assertThat(response.getState()).isEqualTo(AwaitingServicePayment);
        assertThat(alternativeService.getServiceApplicationAnswers()).isEqualTo(generatedApplication);
        assertThat(alternativeService.getServicePaymentFee().getOrderSummary()).isNull();
        assertThat(alternativeService.getServicePaymentFee().getServiceRequestReference()).isNull();
        assertThat(alternativeService.getAlternativeServiceFeeRequired()).isEqualTo(YES);
        assertThat(alternativeService.getServicePaymentFee().getPaymentMethod())
            .isEqualTo(ServicePaymentMethod.FEE_PAY_BY_HWF);
        assertThat(alternativeService.getServiceApplicationSubmittedOnline()).isEqualTo(YES);
        assertThat(alternativeService.getServiceApplicationDocsUploadedPreSubmission()).isEqualTo(YES);
        assertThat(alternativeService.getAlternativeServiceType()).isEqualTo(AlternativeServiceType.DEEMED);
    }

    @Test
    void givenCitizenSubmitsWithWelshLanguagePreferenceThenChangeStateToWelshTranslationReview() {
        setMockClock(clock);

        CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(
                Applicant.builder()
                    .firstName(TEST_FIRST_NAME)
                    .interimApplicationOptions(InterimApplicationOptions.builder()
                        .interimAppsUseHelpWithFees(YES)
                        .interimAppsCannotUploadDocs(NO)
                        .interimApplicationType(InterimApplicationType.DEEMED_SERVICE)
                        .deemedServiceJourneyOptions(DeemedServiceJourneyOptions.builder().build())
                        .build())
                    .languagePreferenceWelsh(YES)
                    .build()
            ).build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        DivorceDocument generatedApplication = DivorceDocument.builder().build();
        when(interimApplicationSubmissionService.generateServiceApplicationAnswerDocument(
            TEST_CASE_ID, caseData.getApplicant1(), caseData
        )).thenReturn(generatedApplication);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitServiceApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        assertThat(response.getState()).isEqualTo(WelshTranslationReview);
        assertThat(response.getData().getApplication().getWelshPreviousState()).isEqualTo(AwaitingServicePayment);
    }

    @Test
    void shouldArchiveApplicationOptions() {
        setMockClock(clock);

        InterimApplicationOptions applicationOptions = InterimApplicationOptions.builder()
            .interimAppsUseHelpWithFees(YES)
            .interimAppsCannotUploadDocs(NO)
            .interimApplicationType(InterimApplicationType.DEEMED_SERVICE)
            .deemedServiceJourneyOptions(DeemedServiceJourneyOptions.builder().build())
            .build();

        CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(
                Applicant.builder()
                    .firstName(TEST_FIRST_NAME)
                    .interimApplicationOptions(applicationOptions)
                    .build()
            ).build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        DivorceDocument generatedApplication = DivorceDocument.builder().build();
        when(interimApplicationSubmissionService.generateServiceApplicationAnswerDocument(
            TEST_CASE_ID, caseData.getApplicant1(), caseData
        )).thenReturn(generatedApplication);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitServiceApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        Applicant applicant = response.getData().getApplicant1();
        assertThat(applicant.getInterimApplicationOptions()).isEqualTo(new InterimApplicationOptions());
        assertThat(applicant.getInterimApplications().size()).isEqualTo(1);
        assertThat(applicant.getInterimApplications().getFirst().getValue().getOptions()).isEqualTo(applicationOptions);
    }

    @Test
    void givenCitizenWillNotMakePaymentButDocsHaveNotBeenSubmittedThenChangeStateToAwaitingApplicant() {
        setMockClock(clock);

        CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(
                Applicant.builder()
                    .firstName(TEST_FIRST_NAME)
                    .interimApplicationOptions(InterimApplicationOptions.builder()
                        .interimAppsUseHelpWithFees(YES)
                        .interimAppsCannotUploadDocs(YES)
                        .interimApplicationType(InterimApplicationType.DEEMED_SERVICE)
                        .deemedServiceJourneyOptions(DeemedServiceJourneyOptions.builder().build())
                        .build())
                    .build()
            ).build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        DivorceDocument generatedApplication = DivorceDocument.builder().build();
        when(interimApplicationSubmissionService.generateServiceApplicationAnswerDocument(
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
        assertThat(alternativeService.getAlternativeServiceFeeRequired()).isEqualTo(YES);
        assertThat(alternativeService.getServiceApplicationSubmittedOnline()).isEqualTo(YES);
        assertThat(alternativeService.getServiceApplicationDocsUploadedPreSubmission()).isEqualTo(NO);
        assertThat(alternativeService.getAlternativeServiceType()).isEqualTo(AlternativeServiceType.DEEMED);
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
            .applicationType(SOLE_APPLICATION)
            .applicant1(
                Applicant.builder()
                    .firstName(TEST_FIRST_NAME)
                    .interimApplicationOptions(InterimApplicationOptions.builder()
                        .interimAppsCanUploadEvidence(NO)
                        .interimAppsEvidenceDocs(evidenceDocs)
                        .interimAppsUseHelpWithFees(YES)
                        .interimAppsCannotUploadDocs(YES)
                        .interimApplicationType(InterimApplicationType.DEEMED_SERVICE)
                        .deemedServiceJourneyOptions(DeemedServiceJourneyOptions.builder().build())
                        .build())
                    .build()
            ).build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        DivorceDocument generatedApplication = DivorceDocument.builder().build();
        when(interimApplicationSubmissionService.generateServiceApplicationAnswerDocument(
            TEST_CASE_ID, caseData.getApplicant1(), caseData
        )).thenReturn(generatedApplication);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSubmitServiceApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        verify(documentRemovalService).deleteDocument(evidenceDocs);

        AlternativeService alternativeService = response.getData().getAlternativeService();
        assertThat(response.getState()).isEqualTo(State.AwaitingDocuments);
        assertThat(alternativeService.getServiceApplicationDocuments()).isNull();
    }


    @Test
    void shouldTriggerDeemedServiceNotificationsIfApplicationSubmittedWithHelpWithFees() {
        CaseData caseData = buildCaseData(InterimApplicationType.DEEMED_SERVICE);

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        caseData.setAlternativeService(
            AlternativeService.builder()
                .servicePaymentFee(
                    FeeDetails.builder()
                        .paymentMethod(ServicePaymentMethod.FEE_PAY_BY_HWF)
                        .build()
                )
                .alternativeServiceType(AlternativeServiceType.DEEMED)
                .build()
        );

        citizenSubmitServiceApplication.submitted(caseDetails, caseDetails);

        verify(interimApplicationSubmissionService).sendServiceApplicationNotifications(
            TEST_CASE_ID, AlternativeServiceType.DEEMED, caseData
        );
    }

    @Test
    void shouldNotTriggerDeemedServiceNotificationsIfApplicationRequiresPayment() {
        CaseData caseData = buildCaseData(InterimApplicationType.DEEMED_SERVICE);

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        caseData.setAlternativeService(
            AlternativeService.builder()
                .servicePaymentFee(
                    FeeDetails.builder()
                        .paymentMethod(ServicePaymentMethod.FEE_PAY_BY_CARD)
                        .build()
                )
                .alternativeServiceFeeRequired(YES)
                .alternativeServiceType(AlternativeServiceType.DEEMED)
                .build()
        );

        citizenSubmitServiceApplication.submitted(caseDetails, caseDetails);

        verifyNoInteractions(interimApplicationSubmissionService);
    }

    private CaseData buildCaseData(InterimApplicationType interimApplicationType) {
        return CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .interimApplicationOptions(
                        InterimApplicationOptions.builder()
                            .interimApplicationType(interimApplicationType)
                            .build())
                    .build()
            ).build();
    }
}
