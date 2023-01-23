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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER;
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
            eq(true)
        );

        final MissingDocumentsValidation missingDocumentsValidation = missingDocumentsValidationCaptor.getValue();
        assertThat(missingDocumentsValidation.message).isEqualTo(
            "Awaiting clarification Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}"
        );
        assertThat(missingDocumentsValidation.documentTypeList).isEqualTo(
            List.of(COVERSHEET, CONDITIONAL_ORDER_REFUSAL_COVER_LETTER, CONDITIONAL_ORDER_REFUSAL)
        );
        assertThat(missingDocumentsValidation.expectedDocumentsSize).isEqualTo(3);
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
            eq(true)
        );

        final MissingDocumentsValidation missingDocumentsValidation = missingDocumentsValidationCaptor.getValue();
        assertThat(missingDocumentsValidation.message).isEqualTo(
            "Awaiting clarification JS Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}"
        );
        assertThat(missingDocumentsValidation.documentTypeList).isEqualTo(
            List.of(
                COVERSHEET,
                CONDITIONAL_ORDER_REFUSAL_COVER_LETTER,
                CONDITIONAL_ORDER_REFUSAL,
                APPLICATION
            )
        );
        assertThat(missingDocumentsValidation.expectedDocumentsSize).isEqualTo(4);
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
            eq(true)
        );

        final MissingDocumentsValidation missingDocumentsValidation = missingDocumentsValidationCaptor.getValue();
        assertThat(missingDocumentsValidation.message).isEqualTo(
            "Awaiting clarification JS Application Letter pack has missing documents. Expected documents with type {} , for Case ID: {}"
        );
        assertThat(missingDocumentsValidation.documentTypeList).isEqualTo(
            List.of(
                COVERSHEET,
                JUDICIAL_SEPARATION_CONDITIONAL_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER,
                CONDITIONAL_ORDER_REFUSAL,
                APPLICATION
            )
        );
        assertThat(missingDocumentsValidation.expectedDocumentsSize).isEqualTo(4);
    }
}
