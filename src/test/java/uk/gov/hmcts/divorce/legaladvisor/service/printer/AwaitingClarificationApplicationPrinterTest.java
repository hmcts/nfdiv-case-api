package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class AwaitingClarificationApplicationPrinterTest {

    @Mock
    private AwaitingAmendedOrClarificationApplicationCommonPrinter awaitingAmendedOrClarificationApplicationCommonPrinter;

    @InjectMocks
    private AwaitingClarificationApplicationPrinter awaitingClarificationApplicationPrinter;

    @Captor
    ArgumentCaptor<MissingDocumentsValidation> missingDocumentsValidationCaptor;

    @Test
    void shouldPrintAwaitingClarificationApplicationPack() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).build())
            .applicant2(Applicant.builder().build())
            .isJudicialSeparation(YesOrNo.NO)
            .build();

        awaitingClarificationApplicationPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        verify(awaitingAmendedOrClarificationApplicationCommonPrinter).sendLetters(
            eq(caseData),
            eq(TEST_CASE_ID),
            eq(caseData.getApplicant1()),
            missingDocumentsValidationCaptor.capture(),
            eq(awaitingClarificationApplicationPrinter.AWAITING_CLARIFICATION_APPLICATION_LETTER_TYPE)
        );

        final MissingDocumentsValidation missingDocumentsValidation = missingDocumentsValidationCaptor.getValue();
        assertThat(missingDocumentsValidation.message).isEqualTo(awaitingClarificationApplicationPrinter.MISSING_DOCUMENTS_MESSAGE);
        assertThat(missingDocumentsValidation.documentTypeList)
            .isEqualTo(awaitingClarificationApplicationPrinter.MISSING_DOCUMENTS_TYPE_LIST);
        assertThat(missingDocumentsValidation.expectedDocumentsSize)
            .isEqualTo(awaitingClarificationApplicationPrinter.MISSING_DOCUMENTS_EXPECTED_DOCUMENTS_SIZE);
    }

    @Test
    void shouldPrintAwaitingClarificationApplicationPackForJudicialSeparation() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).solicitorRepresented(NO).build())
            .applicant2(Applicant.builder().build())
            .isJudicialSeparation(YES)
            .build();

        awaitingClarificationApplicationPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        verify(awaitingAmendedOrClarificationApplicationCommonPrinter).sendLetters(
            eq(caseData),
            eq(TEST_CASE_ID),
            eq(caseData.getApplicant1()),
            missingDocumentsValidationCaptor.capture(),
            eq(awaitingClarificationApplicationPrinter.AWAITING_CLARIFICATION_APPLICATION_LETTER_TYPE)
        );

        final MissingDocumentsValidation missingDocumentsValidation = missingDocumentsValidationCaptor.getValue();
        assertThat(missingDocumentsValidation.message)
            .isEqualTo(awaitingClarificationApplicationPrinter.MISSING_DOCUMENTS_JUDICIAL_SEPARATION_MESSAGE);
        assertThat(missingDocumentsValidation.documentTypeList)
            .isEqualTo(awaitingClarificationApplicationPrinter.MISSING_DOCUMENTS_JUDICIAL_SEPARATION_TYPE_LIST);
        assertThat(missingDocumentsValidation.expectedDocumentsSize)
            .isEqualTo(awaitingClarificationApplicationPrinter.MISSING_DOCUMENTS_JUDICIAL_SEPARATION_EXPECTED_DOCUMENTS_SIZE);
    }

    @Test
    void shouldPrintAwaitingClarificationApplicationSolicitorPackForJudicialSeparation() {

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).solicitorRepresented(YES).build())
            .applicant2(Applicant.builder().build())
            .isJudicialSeparation(YES)
            .build();

        awaitingClarificationApplicationPrinter.sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1()
        );

        verify(awaitingAmendedOrClarificationApplicationCommonPrinter).sendLetters(
            eq(caseData),
            eq(TEST_CASE_ID),
            eq(caseData.getApplicant1()),
            missingDocumentsValidationCaptor.capture(),
            eq(awaitingClarificationApplicationPrinter.AWAITING_CLARIFICATION_APPLICATION_LETTER_TYPE)
        );

        final MissingDocumentsValidation missingDocumentsValidation = missingDocumentsValidationCaptor.getValue();
        assertThat(missingDocumentsValidation.message)
            .isEqualTo(awaitingClarificationApplicationPrinter.MISSING_DOCUMENTS_JUDICIAL_SEPARATION_MESSAGE);
        assertThat(missingDocumentsValidation.documentTypeList)
            .isEqualTo(awaitingClarificationApplicationPrinter.MISSING_DOCUMENTS_JUDICIAL_SEPARATION_REPRESENTED_TYPE_LIST);
        assertThat(missingDocumentsValidation.expectedDocumentsSize)
            .isEqualTo(awaitingClarificationApplicationPrinter.MISSING_DOCUMENTS_JUDICIAL_SEPARATION_EXPECTED_DOCUMENTS_SIZE);
    }
}
