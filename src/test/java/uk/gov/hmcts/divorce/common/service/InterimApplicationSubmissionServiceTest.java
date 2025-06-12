package uk.gov.hmcts.divorce.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.generator.DeemedServiceApplicationGenerator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class InterimApplicationSubmissionServiceTest {
    @Mock
    private DeemedServiceApplicationGenerator deemedServiceApplicationGenerator;

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
    void shouldThrowUnsupportedOperationExceptionWhenApplicationTypeIsNotRecognised() {
        long caseId = TEST_CASE_ID;
        CaseData caseData = CaseData.builder()
            .applicant1(
                Applicant.builder()
                    .interimApplicationOptions(
                        InterimApplicationOptions.builder()
                            .interimApplicationType(null)  // Unrecognized application type
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
}
