package uk.gov.hmcts.divorce.document.print.documentpack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.print.documentpack.ConditionalOrderPronouncedDocumentPack.LETTER_TYPE_CO_PRONOUNCED;


class ConditionalOrderPronouncedDocumentPackTest {

    private final ConditionalOrderPronouncedDocumentPack conditionalOrderPronouncedDocumentPack =
        new ConditionalOrderPronouncedDocumentPack();

    @Test
    void getCorrectLetterId() {
        assertThat(conditionalOrderPronouncedDocumentPack.getLetterId()).isEqualTo(LETTER_TYPE_CO_PRONOUNCED);
    }

    private static Stream<Object[]> provideTestCases() {
        return Stream.of(
            // Parameters: isApplicant1, isJudicialSeparation, isDivorce, applicationType, isRepresented, documentPackField
            new Object[] {true, true, false, ApplicationType.JOINT_APPLICATION, true,
                "APPLICANT_1_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK"},
            new Object[] {true, false, false, ApplicationType.JOINT_APPLICATION, false,
                "APPLICANT_1_JOINT_SEPARATION_CONDITIONAL_ORDER_PACK"},
            new Object[] {true, false, false, ApplicationType.SOLE_APPLICATION, false, "APPLICANT_1_SOLE_CONDITIONAL_ORDER_PACK"},
            new Object[] {true, true, true, ApplicationType.SOLE_APPLICATION, false,
                "APPLICANT_1_DIV_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK"},
            new Object[] {true, true, true, ApplicationType.SOLE_APPLICATION, true,
                "APPLICANT_1_DIV_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK"},
            new Object[] {true, true, true, ApplicationType.SOLE_APPLICATION, false,
                "APPLICANT_1_DIV_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK"},
            new Object[] {false, true, false, ApplicationType.JOINT_APPLICATION, true,
                "APPLICANT_2_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK"},
            new Object[] {false, false, false, ApplicationType.JOINT_APPLICATION, false,
                "APPLICANT_2_JOINT_SEPARATION_CONDITIONAL_ORDER_PACK"},
            new Object[] {false, false, false, ApplicationType.SOLE_APPLICATION, false, "APPLICANT_2_SOLE_CONDITIONAL_ORDER_PACK"},
            new Object[] {false, true, true, ApplicationType.SOLE_APPLICATION, false,
                "APPLICANT_2_DIV_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK"},
            new Object[] {false, true, true, ApplicationType.SOLE_APPLICATION, true,
                "APPLICANT_2_DIV_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK"},
            new Object[] {false, true, true, ApplicationType.SOLE_APPLICATION, false,
                "APPLICANT_2_DIV_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK"},
            new Object[] {true, false, true, ApplicationType.SOLE_APPLICATION, true,
                "APPLICANT_1_SOL_SOLE_CONDITIONAL_ORDER_PACK"},
            new Object[] {false, false, true, ApplicationType.SOLE_APPLICATION, true,
                "APPLICANT_2_SOL_SOLE_CONDITIONAL_ORDER_PACK"},
            new Object[] {true, false, true, ApplicationType.JOINT_APPLICATION, true,
                "APPLICANT_1_SOL_JOINT_SEPARATION_CONDITIONAL_ORDER_PACK"},
            new Object[] {false, false, true, ApplicationType.JOINT_APPLICATION, true,
                "APPLICANT_2_SOL_JOINT_SEPARATION_CONDITIONAL_ORDER_PACK"}
            );
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void testGetDocumentPackApplicant1(boolean isApplicant1, boolean isJudicialSeparation, boolean isDivorce,
                                              ApplicationType applicationType, boolean isRepresented, String documentPackField)
        throws NoSuchFieldException, IllegalAccessException {

        CaseData caseData = mock(CaseData.class);
        Applicant applicant = mock(Applicant.class);
        Applicant otherApplicant = mock(Applicant.class);

        when(caseData.isJudicialSeparationCase()).thenReturn(isJudicialSeparation);
        when(caseData.isDivorce()).thenReturn(isDivorce);
        if (isApplicant1) {
            when(caseData.getApplicant1()).thenReturn(applicant);
        } else {
            when(caseData.getApplicant1()).thenReturn(otherApplicant);
        }
        when(applicant.isRepresented()).thenReturn(isRepresented);
        when(caseData.getApplicationType()).thenReturn(applicationType);

        ConditionalOrderPronouncedDocumentPack documentPack = new ConditionalOrderPronouncedDocumentPack();
        Field field = ConditionalOrderPronouncedDocumentPack.class.getDeclaredField(documentPackField);
        field.setAccessible(true);
        DocumentPackInfo expectedPackInfo = (DocumentPackInfo) field.get(documentPack);
        DocumentPackInfo result = documentPack.getDocumentPack(caseData, applicant);

        assertNotNull(result);
        assertEquals(expectedPackInfo, result);
    }
}
