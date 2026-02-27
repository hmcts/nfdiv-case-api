package uk.gov.hmcts.divorce.common.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.D11GeneralApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.SearchGovRecordsApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralReason;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.generator.D11GeneralApplicationGenerator;
import uk.gov.hmcts.divorce.document.print.generator.SearchGovRecordsApplicationGenerator;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType.DIGITISED_GENERAL_APPLICATION_D11;
import static uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType.SEARCH_GOV_RECORDS;
import static uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod.FEE_PAY_BY_CARD;
import static uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod.FEE_PAY_BY_HWF;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralReferralPayment;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CitizenGeneralApplicationSubmissionServiceTest {

    @Mock
    private SearchGovRecordsApplicationSubmittedNotification searchGovRecordsNotification;

    @Mock
    private SearchGovRecordsApplicationGenerator searchGovRecordsGenerator;

    @Mock
    private D11GeneralApplicationGenerator d11Generator;

    @Mock
    private D11GeneralApplicationSubmittedNotification d11Notification;

    @InjectMocks
    private CitizenGeneralApplicationSubmissionService submissionService;

    @Nested
    class GenerateGeneralApplicationAnswerDocument {

        @Test
        void shouldDelegateToSearchGovRecordsGenerator() {
            CaseData caseData = buildCaseData(SEARCH_GOV_RECORDS);
            GeneralApplication application = buildApplication(GeneralApplicationType.DISCLOSURE_VIA_DWP);
            DivorceDocument document = DivorceDocument.builder().build();

            when(searchGovRecordsGenerator.generateDocument(
                TEST_CASE_ID, caseData.getApplicant1(), caseData, application
            )).thenReturn(document);

            DivorceDocument result = submissionService.generateGeneralApplicationAnswerDocument(
                TEST_CASE_ID, caseData.getApplicant1(), caseData, application
            );

            verify(searchGovRecordsGenerator)
                .generateDocument(TEST_CASE_ID, caseData.getApplicant1(), caseData, application);
            assertThat(result).isEqualTo(document);
        }
    }

    @Nested
    class SendNotifications {

        @Test
        void shouldDelegateToSearchGovRecordsNotification() {
            CaseData caseData = buildCaseData(SEARCH_GOV_RECORDS);
            GeneralApplication application = buildApplication(GeneralApplicationType.DISCLOSURE_VIA_DWP);

            submissionService.sendNotifications(TEST_CASE_ID, application, caseData);

            verify(searchGovRecordsNotification)
                .sendToApplicant1(caseData, TEST_CASE_ID, application);
        }
    }

    @Nested
    class CanBeAutoReferred {

        @Test
        void shouldReturnFalseWhenReferralAlreadyExists() {
            CaseData caseData = CaseData.builder()
                .generalReferral(GeneralReferral.builder()
                    .generalReferralReason(GeneralReferralReason.GENERAL_APPLICATION_REFERRAL)
                    .build())
                .build();

            boolean result = submissionService.canBeAutoReferred(
                caseData, GeneralApplicationType.DISCLOSURE_VIA_DWP
            );

            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalseWhenTypeCannotBeAutoReferred() {
            CaseData caseData = CaseData.builder().build();

            boolean result = submissionService.canBeAutoReferred(
                caseData, GeneralApplicationType.AMEND_APPLICATION
            );

            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnTrueWhenTypeCanBeAutoReferred() {
            CaseData caseData = CaseData.builder().build();

            boolean result = submissionService.canBeAutoReferred(
                caseData, GeneralApplicationType.DISCLOSURE_VIA_DWP
            );

            assertThat(result).isTrue();
        }
    }

    @Nested
    class SetEndState {
        @Test
        void shouldMoveToAwaitingDocuments() {
            CaseDetails<CaseData, State> details = new CaseDetails<>();
            details.setData(caseData());
            GeneralApplication application = buildApplication(FEE_PAY_BY_CARD, YesOrNo.YES, YesOrNo.YES);

            submissionService.setEndState(details, application);

            assertThat(details.getState()).isEqualTo(AwaitingDocuments);
        }

        @Test
        void shouldMoveToAwaitingGeneralReferralPaymentWhenNotCardPayment() {
            CaseDetails<CaseData, State> details = new CaseDetails<>();
            details.setData(caseData());
            GeneralApplication application = buildApplication(FEE_PAY_BY_HWF, null, YesOrNo.NO);

            submissionService.setEndState(details, application);

            assertThat(details.getState()).isEqualTo(AwaitingGeneralReferralPayment);
        }
    }

    @Nested
    class CollectSupportingDocuments {
        @Test
        void shouldReturnEmptyListWhenNotD11Application() {
            InterimApplicationOptions options = InterimApplicationOptions.builder()
                .interimApplicationType(SEARCH_GOV_RECORDS)
                .build();

            List<ListValue<DivorceDocument>> result =
                submissionService.collectSupportingDocuments(options);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldCollectD11SupportingDocuments() {
            DivorceDocument document = DivorceDocument.builder().build();
            ListValue<DivorceDocument> listValue = ListValue.<DivorceDocument>builder()
                .value(document)
                .build();

            GeneralApplicationD11JourneyOptions journeyOptions = mock(GeneralApplicationD11JourneyOptions.class);
            when(journeyOptions.evidenceOfPartnerSupportRequired()).thenReturn(true);
            when(journeyOptions.getPartnerAgreesDocs()).thenReturn(List.of(listValue));

            InterimApplicationOptions options = mock(InterimApplicationOptions.class);
            when(options.getInterimApplicationType()).thenReturn(DIGITISED_GENERAL_APPLICATION_D11);
            when(options.getGeneralApplicationD11JourneyOptions()).thenReturn(journeyOptions);
            when(options.hasUploadedSupportingDocuments()).thenReturn(false);

            List<ListValue<DivorceDocument>> result = submissionService.collectSupportingDocuments(options);

            assertThat(result).hasSize(1);
        }
    }

    private CaseData buildCaseData(InterimApplicationType type) {
        return CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .interimApplicationOptions(
                        InterimApplicationOptions.builder()
                            .interimApplicationType(type)
                            .build())
                    .build()
            ).build();
    }

    private GeneralApplication buildApplication(GeneralApplicationType type) {
        return GeneralApplication.builder()
            .generalApplicationType(type)
            .generalApplicationReceivedDate(LocalDateTime.now())
            .build();
    }

    private GeneralApplication buildApplication(
        ServicePaymentMethod paymentMethod,
        YesOrNo completedOnlinePayment,
        YesOrNo docsUploaded
    ) {
        return GeneralApplication.builder()
            .generalApplicationFee(
                FeeDetails.builder()
                    .paymentMethod(paymentMethod)
                    .hasCompletedOnlinePayment(completedOnlinePayment)
                    .build()
            )
            .generalApplicationType(GeneralApplicationType.DISCLOSURE_VIA_DWP)
            .generalApplicationDocsUploadedPreSubmission(docsUploaded)
            .build();
    }
}
