package uk.gov.hmcts.divorce.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.AlternativeServiceApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.BailiffServiceApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.DeemedServiceApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.SearchGovRecordsApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.generator.AlternativeServiceApplicationGenerator;
import uk.gov.hmcts.divorce.document.print.generator.BailiffServiceApplicationGenerator;
import uk.gov.hmcts.divorce.document.print.generator.DeemedServiceApplicationGenerator;
import uk.gov.hmcts.divorce.document.print.generator.SearchGovRecordsApplicationGenerator;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType.DISPENSE_WITH_SERVICE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class InterimApplicationSubmissionServiceTest {
    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private DeemedServiceApplicationSubmittedNotification deemedNotification;

    @Mock
    private SearchGovRecordsApplicationSubmittedNotification searchGovRecordsApplicationSubmittedNotification;

    @Mock
    private BailiffServiceApplicationSubmittedNotification bailiffNotification;

    @Mock
    private AlternativeServiceApplicationSubmittedNotification alternativeServiceApplicationSubmittedNotification;

    @Mock
    private DeemedServiceApplicationGenerator deemedServiceApplicationGenerator;

    @Mock
    private BailiffServiceApplicationGenerator bailiffServiceApplicationGenerator;

    @Mock
    private AlternativeServiceApplicationGenerator alternativeServiceApplicationGenerator;

    @Mock
    private SearchGovRecordsApplicationGenerator searchGovRecordsApplicationGenerator;

    @InjectMocks
    private InterimApplicationSubmissionService interimApplicationSubmissionService;

    @Test
    void shouldDelegateToDeemedServiceApplicationGeneratorWhenApplicationTypeIsDeemed() {
        long caseId = TEST_CASE_ID;
        CaseData caseData = CaseData.builder()
                .applicant1(
                    Applicant.builder()
                        .interimApplicationOptions(
                            InterimApplicationOptions.builder()
                                .interimApplicationType(InterimApplicationType.DEEMED_SERVICE)
                                .build())
                        .build()
                ).build();

        DivorceDocument generatedDocument = DivorceDocument.builder().build();
        when(deemedServiceApplicationGenerator.generateDocument(caseId, caseData.getApplicant1(), caseData))
            .thenReturn(generatedDocument);

        DivorceDocument result = interimApplicationSubmissionService.generateAnswerDocument(caseId, caseData.getApplicant1(), caseData);

        verify(deemedServiceApplicationGenerator).generateDocument(caseId, caseData.getApplicant1(), caseData);
        assertThat(result).isEqualTo(generatedDocument);
    }


    @Test
    void shouldDelegateToBailiffServiceApplicationGeneratorWhenApplicationTypeIsBailiff() {
        long caseId = TEST_CASE_ID;
        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .interimApplicationOptions(
                        InterimApplicationOptions.builder()
                            .interimApplicationType(InterimApplicationType.BAILIFF_SERVICE)
                            .build())
                    .build()
            ).build();

        DivorceDocument generatedDocument = DivorceDocument.builder().build();
        when(bailiffServiceApplicationGenerator.generateDocument(caseId, caseData.getApplicant1(), caseData))
            .thenReturn(generatedDocument);

        DivorceDocument result = interimApplicationSubmissionService.generateAnswerDocument(caseId, caseData.getApplicant1(), caseData);

        verify(bailiffServiceApplicationGenerator).generateDocument(caseId, caseData.getApplicant1(), caseData);
        assertThat(result).isEqualTo(generatedDocument);
    }

    @Test
    void shouldThrowUnsupportedOperationExceptionWhenApplicationTypeIsNotRecognised() {
        long caseId = TEST_CASE_ID;
        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .interimApplicationOptions(
                        InterimApplicationOptions.builder()
                            .interimApplicationType(DISPENSE_WITH_SERVICE)  // Update this when DISPENSE_WITH_SERVICE is implemented
                            .build())
                    .build()
            ).build();

        assertThrows(
            UnsupportedOperationException.class,
            () -> interimApplicationSubmissionService.generateAnswerDocument(caseId, caseData.getApplicant1(), caseData)
        );
    }

    @Test
    void shouldDelegateToDeemedServiceNotificationWhenApplicationTypeIsDeemed() {
        long caseId = TEST_CASE_ID;
        CaseData caseData = CaseData.builder().build();

        interimApplicationSubmissionService.sendNotifications(caseId, AlternativeServiceType.DEEMED, caseData);

        verify(notificationDispatcher).send(deemedNotification, caseData, caseId);
    }

    @Test
    void shouldDelegateToBailiffServiceNotificationWhenApplicationTypeIsBailiff() {
        long caseId = TEST_CASE_ID;
        CaseData caseData = CaseData.builder().build();

        interimApplicationSubmissionService.sendNotifications(caseId, AlternativeServiceType.BAILIFF, caseData);

        verify(notificationDispatcher).send(bailiffNotification, caseData, caseId);
    }

    @Test
    void shouldDelegateToAlternativeServiceApplicationGeneratorWhenApplicationTypeIsAlternativeService() {
        long caseId = TEST_CASE_ID;
        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .interimApplicationOptions(
                        InterimApplicationOptions.builder()
                            .interimApplicationType(InterimApplicationType.ALTERNATIVE_SERVICE)
                            .build())
                    .build()
            ).build();

        DivorceDocument generatedDocument = DivorceDocument.builder().build();
        when(alternativeServiceApplicationGenerator.generateDocument(caseId, caseData.getApplicant1(), caseData))
            .thenReturn(generatedDocument);

        DivorceDocument result = interimApplicationSubmissionService.generateAnswerDocument(caseId, caseData.getApplicant1(), caseData);

        assertThat(result).isEqualTo(generatedDocument);
    }

    @Test
    void shouldDelegateToAlternativeServiceNotificationWhenApplicationTypeIsAlternativeService() {
        long caseId = TEST_CASE_ID;
        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .interimApplicationOptions(
                        InterimApplicationOptions.builder()
                            .interimApplicationType(InterimApplicationType.ALTERNATIVE_SERVICE)
                            .build())
                    .build()
            ).build();

        interimApplicationSubmissionService.sendNotifications(caseId, AlternativeServiceType.ALTERNATIVE_SERVICE, caseData);

        verify(notificationDispatcher).send(alternativeServiceApplicationSubmittedNotification, caseData, caseId);
    }

    @Test
    void shouldDelegateToSearchGovRecordsApplicationGeneratorWhenApplicationTypeIsSearchGovRecords() {
        long caseId = TEST_CASE_ID;
        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .interimApplicationOptions(
                        InterimApplicationOptions.builder()
                            .interimApplicationType(InterimApplicationType.SEARCH_GOV_RECORDS)
                            .build())
                    .build()
            ).build();

        DivorceDocument generatedDocument = DivorceDocument.builder().build();
        when(searchGovRecordsApplicationGenerator.generateDocument(caseId, caseData.getApplicant1(), caseData))
            .thenReturn(generatedDocument);

        DivorceDocument result = interimApplicationSubmissionService.generateAnswerDocument(caseId, caseData.getApplicant1(), caseData);

        verify(searchGovRecordsApplicationGenerator).generateDocument(caseId, caseData.getApplicant1(), caseData);
        assertThat(result).isEqualTo(generatedDocument);
    }

    @Test
    void shouldDelegateToSearchGovRecordsNotificationWhenApplicationTypeIsSearchGovRecords() {
        long caseId = TEST_CASE_ID;
        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .interimApplicationOptions(
                        InterimApplicationOptions.builder()
                            .interimApplicationType(InterimApplicationType.SEARCH_GOV_RECORDS)
                            .build())
                    .build()
            ).build();

        interimApplicationSubmissionService.sendNotifications(caseId, null, caseData);

        verify(notificationDispatcher).send(searchGovRecordsApplicationSubmittedNotification, caseData, caseId);
    }
}
