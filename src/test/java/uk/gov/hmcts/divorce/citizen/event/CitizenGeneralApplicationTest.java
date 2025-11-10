package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
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
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DeemedServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.SearchGovRecordsJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

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
import static uk.gov.hmcts.divorce.citizen.event.CitizenGeneralApplication.AWAITING_PAYMENT_ERROR;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSubmitServiceApplication.AOS_SUBMITTED_BY_PARTNER;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralApplicationPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;

@ExtendWith(MockitoExtension.class)
class CitizenGeneralApplicationTest {
    @Mock
    private PaymentSetupService paymentSetupService;

    @Mock
    private Clock clock;

    @Mock
    InterimApplicationSubmissionService interimApplicationSubmissionService;

    @Mock
    DocumentRemovalService documentRemovalService;

    @Mock
    CcdAccessService ccdAccessService;

    @Mock
    HttpServletRequest request;

    @InjectMocks
    private CitizenGeneralApplication citizenGeneralApplication;

    private OrderSummary orderSummary;

    @Test
    void givenGeneralApplicationPaymentIsInProgressThenRejectNewSubmission() {
        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .generalAppServiceRequest("dummy-service-request")
                    .build()
            ).build();
        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        when(request.getHeader(AUTHORIZATION)).thenReturn(AUTHORIZATION);
        when(ccdAccessService.isApplicant1(AUTHORIZATION, TEST_CASE_ID)).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenGeneralApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(AWAITING_PAYMENT_ERROR));
    }

    @Test
    void givenAosIsSubmittedThenRejectGeneralApplicationDWPSubmission() {
        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder().build()
            ).build();
        InterimApplicationOptions applicationOptions = InterimApplicationOptions.builder()
            .interimAppsUseHelpWithFees(YesOrNo.YES)
            .interimAppsCannotUploadDocs(YesOrNo.NO)
            .interimApplicationType(InterimApplicationType.SEARCH_GOV_RECORDS)
            .deemedServiceJourneyOptions(DeemedServiceJourneyOptions.builder().build())
            .build();
        caseData.getApplicant1().setInterimApplicationOptions(applicationOptions);

        caseData.getAcknowledgementOfService().setDateAosSubmitted(
            LocalDateTime.of(2021, 10, 26, 10, 0, 0));

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        when(request.getHeader(AUTHORIZATION)).thenReturn(AUTHORIZATION);
        when(ccdAccessService.isApplicant1(AUTHORIZATION, TEST_CASE_ID)).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenGeneralApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(AOS_SUBMITTED_BY_PARTNER));
    }

    @Test
    void shouldArchiveApplicationOptions() {
        setMockClock(clock);

        InterimApplicationOptions applicationOptions = InterimApplicationOptions.builder()
            .interimAppsUseHelpWithFees(YesOrNo.YES)
            .interimAppsCannotUploadDocs(YesOrNo.NO)
            .interimApplicationType(InterimApplicationType.SEARCH_GOV_RECORDS)
            .deemedServiceJourneyOptions(DeemedServiceJourneyOptions.builder().build())
            .build();

        when(request.getHeader(AUTHORIZATION)).thenReturn(AUTHORIZATION);
        when(ccdAccessService.isApplicant1(AUTHORIZATION, TEST_CASE_ID)).thenReturn(true);

        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .firstName(TEST_FIRST_NAME)
                    .interimApplicationOptions(applicationOptions)
                    .build()
            ).build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        DivorceDocument generatedApplication = DivorceDocument.builder().build();
        when(interimApplicationSubmissionService.generateGeneralApplicationAnswerDocument(
            eq(TEST_CASE_ID), eq(caseData.getApplicant1()), eq(caseData), any(GeneralApplication.class)
        )).thenReturn(generatedApplication);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenGeneralApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        Applicant applicant = response.getData().getApplicant1();
        assertThat(applicant.getInterimApplicationOptions()).isEqualTo(new InterimApplicationOptions());
        assertThat(applicant.getInterimApplications().size()).isEqualTo(1);
        assertThat(applicant.getInterimApplications().getFirst().getValue().getOptions()).isEqualTo(applicationOptions);
    }


    @Test
    void givenCitizenWillMakePaymentThenChangeStateToAwaitingGeneralApplicationPaymentAndSetOrderSummary() {
        setMockClock(clock);

        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .firstName(TEST_FIRST_NAME)
                    .interimApplicationOptions(InterimApplicationOptions.builder()
                        .interimAppsUseHelpWithFees(YesOrNo.NO)
                        .interimAppsCannotUploadDocs(YesOrNo.YES)
                        .interimApplicationType(InterimApplicationType.SEARCH_GOV_RECORDS)
                        .searchGovRecordsJourneyOptions(SearchGovRecordsJourneyOptions.builder().build())
                        .build())
                    .build()
            ).build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        when(request.getHeader(AUTHORIZATION)).thenReturn(AUTHORIZATION);
        when(ccdAccessService.isApplicant1(AUTHORIZATION, TEST_CASE_ID)).thenReturn(true);
        when(paymentSetupService.createGeneralApplicationOrderSummary(TEST_CASE_ID))
            .thenReturn(orderSummary);
        when(paymentSetupService.createGeneralApplicationPaymentServiceRequest(
            orderSummary, TEST_CASE_ID, TEST_FIRST_NAME
        )).thenReturn(TEST_SERVICE_REFERENCE);

        DivorceDocument generatedApplication = DivorceDocument.builder().build();
        when(interimApplicationSubmissionService.generateGeneralApplicationAnswerDocument(
            eq(TEST_CASE_ID), eq(caseData.getApplicant1()), eq(caseData), any(GeneralApplication.class)
        )).thenReturn(generatedApplication);


        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenGeneralApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        GeneralApplication generalApplication = response.getData().getGeneralApplications().getLast().getValue();
        assertThat(response.getState()).isEqualTo(AwaitingGeneralApplicationPayment);
        assertThat(generalApplication.getGeneralApplicationDocument()).isEqualTo(generatedApplication);
        assertThat(generalApplication.getGeneralApplicationFee().getOrderSummary()).isEqualTo(orderSummary);
        assertThat(generalApplication.getGeneralApplicationFee().getServiceRequestReference()).isEqualTo(TEST_SERVICE_REFERENCE);
        assertThat(generalApplication.getGeneralApplicationFee().getPaymentMethod())
            .isEqualTo(ServicePaymentMethod.FEE_PAY_BY_CARD);
        assertThat(generalApplication.getGeneralApplicationSubmittedOnline()).isEqualTo(YesOrNo.YES);
        assertThat(generalApplication.getGeneralApplicationType()).isEqualTo(GeneralApplicationType.DISCLOSURE_VIA_DWP);
    }

    @Test
    void givenCitizenUsedHelpWithFeesThenChangeStateToGeneralApplicationReceived() {
        setMockClock(clock);

        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .firstName(TEST_FIRST_NAME)
                    .interimApplicationOptions(InterimApplicationOptions.builder()
                        .interimAppsUseHelpWithFees(YesOrNo.YES)
                        .interimAppsHwfRefNumber(TEST_SERVICE_REFERENCE)
                        .interimAppsCannotUploadDocs(YesOrNo.YES)
                        .interimApplicationType(InterimApplicationType.SEARCH_GOV_RECORDS)
                        .searchGovRecordsJourneyOptions(SearchGovRecordsJourneyOptions.builder().build())
                        .build())
                    .build()
            ).build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        when(request.getHeader(AUTHORIZATION)).thenReturn(AUTHORIZATION);
        when(ccdAccessService.isApplicant1(AUTHORIZATION, TEST_CASE_ID)).thenReturn(true);

        DivorceDocument generatedApplication = DivorceDocument.builder().build();
        when(interimApplicationSubmissionService.generateGeneralApplicationAnswerDocument(
            eq(TEST_CASE_ID), eq(caseData.getApplicant1()), eq(caseData), any(GeneralApplication.class)
        )).thenReturn(generatedApplication);


        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenGeneralApplication.aboutToSubmit(
            caseDetails, caseDetails
        );

        GeneralApplication generalApplication = response.getData().getGeneralApplications().getLast().getValue();
        assertThat(response.getState()).isEqualTo(GeneralApplicationReceived);
        assertThat(generalApplication.getGeneralApplicationDocument()).isEqualTo(generatedApplication);
        assertThat(generalApplication.getGeneralApplicationFee().getPaymentMethod())
            .isEqualTo(ServicePaymentMethod.FEE_PAY_BY_HWF);
        assertThat(generalApplication.getGeneralApplicationFee().getHelpWithFeesReferenceNumber())
            .isEqualTo(TEST_SERVICE_REFERENCE);
        assertThat(generalApplication.getGeneralApplicationSubmittedOnline()).isEqualTo(YesOrNo.YES);
        assertThat(generalApplication.getGeneralApplicationType()).isEqualTo(GeneralApplicationType.DISCLOSURE_VIA_DWP);
    }

    @Test
    void shouldTriggerSearchGovRecordsNotificationsIfApplicationSubmittedWithHelpWithFees() {
        final var beforeDetails = CaseDetails.<CaseData, State>builder().data(
            buildCaseData(InterimApplicationType.SEARCH_GOV_RECORDS)
        ).build();
        final var afterDetails = CaseDetails.<CaseData, State>builder().data(
            buildCaseData(InterimApplicationType.SEARCH_GOV_RECORDS)
        ).build();
        beforeDetails.setId(TEST_CASE_ID);
        afterDetails.setId(TEST_CASE_ID);

        GeneralApplication generalApplication = GeneralApplication.builder()
            .generalApplicationFee(FeeDetails.builder()
                .paymentMethod(ServicePaymentMethod.FEE_PAY_BY_HWF)
                .build())
            .build();

        afterDetails.getData().setGeneralApplications(
            List.of(ListValue.<GeneralApplication>builder().value(
                generalApplication
            ).build())
        );

        citizenGeneralApplication.submitted(afterDetails, beforeDetails);

        verify(interimApplicationSubmissionService).sendGeneralApplicationNotifications(
            TEST_CASE_ID, generalApplication, afterDetails.getData()
        );
    }

    @Test
    void shouldNotTriggerSearchGovRecordsNotificationsIfApplicationSubmittedWithHelpWithFees() {
        final var beforeDetails = CaseDetails.<CaseData, State>builder().data(
            buildCaseData(InterimApplicationType.SEARCH_GOV_RECORDS)
        ).build();
        final var afterDetails = CaseDetails.<CaseData, State>builder().data(
            buildCaseData(InterimApplicationType.SEARCH_GOV_RECORDS)
        ).build();
        beforeDetails.setId(TEST_CASE_ID);
        afterDetails.setId(TEST_CASE_ID);

        GeneralApplication generalApplication = GeneralApplication.builder()
            .generalApplicationFee(FeeDetails.builder()
                .paymentMethod(ServicePaymentMethod.FEE_PAY_BY_CARD)
                .build())
            .build();

        afterDetails.getData().setGeneralApplications(
            List.of(ListValue.<GeneralApplication>builder().value(
                generalApplication
            ).build())
        );

        citizenGeneralApplication.submitted(afterDetails, beforeDetails);

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
