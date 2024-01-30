package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.document.print.documentpack.ConditionalOrderPronouncedDocumentPack.LETTER_TYPE_CO_PRONOUNCED;


class ConditionalOrderPronouncedDocumentPackTest {

    private static final DocumentPackInfo APPLICANT_1_DIV_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            JUDICIAL_SEPARATION_ORDER_GRANTED_SOLICITOR_COVERSHEET_DOCUMENT_NAME
        )
    );
    private static final DocumentPackInfo APPLICANT_2_DIV_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, Optional.of(JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID),
            CONDITIONAL_ORDER_GRANTED, Optional.empty()
        ),
        ImmutableMap.of(
            JUDICIAL_SEPARATION_ORDER_GRANTED_COVER_LETTER_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME
        )
    );

    private final ConditionalOrderPronouncedDocumentPack finalOrderGrantedDocumentPack = new ConditionalOrderPronouncedDocumentPack();

    @Test
    void getCorrectLetterId() {
        assertThat(finalOrderGrantedDocumentPack.getLetterId()).isEqualTo(LETTER_TYPE_CO_PRONOUNCED);
    }

    @Test
    public void testGetDocumentPackForDivorceWithJudicialSeparationAndRepresentedApplicant1() {
        // Mocking necessary objects and setting up the scenario
        CaseData caseData = Mockito.mock(CaseData.class);
        Applicant applicant1 = Mockito.mock(Applicant.class);

        Mockito.when(caseData.isJudicialSeparationCase()).thenReturn(true);
        Mockito.when(caseData.isDivorce()).thenReturn(true);
        Mockito.when(caseData.getApplicant1()).thenReturn(applicant1);
        Mockito.when(caseData.getApplicationType()).thenReturn(ApplicationType.SOLE_APPLICATION);
        Mockito.when(applicant1.isRepresented()).thenReturn(true);

        ConditionalOrderPronouncedDocumentPack documentPack = new ConditionalOrderPronouncedDocumentPack();

        // Test the scenario
        DocumentPackInfo result = documentPack.getDocumentPack(caseData, applicant1);
        assertNotNull(result);
        assertEquals(APPLICANT_1_DIV_SOL_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK, result);
    }

    @Test
    public void testGetDocumentPackForDivorceWithJudicialSeparationAndNotRepresentedApplicant2() {
        // Mocking necessary objects and setting up the scenario
        CaseData caseData = Mockito.mock(CaseData.class);
        Applicant applicant2 = Mockito.mock(Applicant.class);

        Mockito.when(caseData.isJudicialSeparationCase()).thenReturn(true);
        Mockito.when(caseData.isDivorce()).thenReturn(true);
        Mockito.when(caseData.getApplicant1()).thenReturn(Mockito.mock(Applicant.class));
        Mockito.when(caseData.getApplicationType()).thenReturn(ApplicationType.SOLE_APPLICATION);
        Mockito.when(applicant2.isRepresented()).thenReturn(false);

        ConditionalOrderPronouncedDocumentPack documentPack = new ConditionalOrderPronouncedDocumentPack();

        // Test the scenario
        DocumentPackInfo result = documentPack.getDocumentPack(caseData, applicant2);
        assertNotNull(result);
        assertEquals(APPLICANT_2_DIV_JUDICIAL_SEPARATION_CONDITIONAL_ORDER_PACK, result);
    }

}
