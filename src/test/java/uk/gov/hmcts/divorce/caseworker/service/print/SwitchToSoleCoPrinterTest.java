package uk.gov.hmcts.divorce.caseworker.service.print;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.UUID;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SWITCH_TO_SOLE_CO_LETTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_LAST_NAME;

@ExtendWith(MockitoExtension.class)
public class SwitchToSoleCoPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @InjectMocks
    private SwitchToSoleCoPrinter printer;

    @Captor
    private ArgumentCaptor<Print> printCaptor;

    @Test
    void shouldPrintSwitchToSoleCoLetterWithDivorceContent() {

        final ListValue<DivorceDocument> switchToSoleCoLetter =
            ListValue.<DivorceDocument>builder()
                .value(DivorceDocument.builder()
                    .documentType(SWITCH_TO_SOLE_CO_LETTER)
                    .build())
                .build();

        final CaseData caseData = CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(singletonList(switchToSoleCoLetter))
                    .build()
            )
            .applicant2(
                Applicant.builder()
                    .firstName(TEST_APP2_FIRST_NAME)
                    .lastName(TEST_APP2_LAST_NAME)
                    .build()
            )
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        printer.print(caseData, TEST_CASE_ID, caseData.getApplicant2());

        final Print print = printCaptor.getValue();
        var expectedRecipient = List.of(TEST_CASE_ID.toString(), caseData.getApplicant2().getFullName(), "switch-to-sole-co-letter");
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("switch-to-sole-co-letter");
        assertThat(print.getLetters().size()).isEqualTo(1);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(switchToSoleCoLetter.getValue());
        assertThat(print.getRecipients()).isEqualTo(expectedRecipient);

    }

    @Test
    void shouldGenerateAndPrintSwitchToSoleCoLetterWithCivilPartnershipContent() {

        final ListValue<DivorceDocument> switchToSoleCoLetter =
            ListValue.<DivorceDocument>builder()
                .value(DivorceDocument.builder()
                    .documentType(SWITCH_TO_SOLE_CO_LETTER)
                    .build())
                .build();

        final CaseData caseData = CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(singletonList(switchToSoleCoLetter))
                    .build()
            )
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        printer.print(caseData, TEST_CASE_ID, caseData.getApplicant2());

        final Print print = printCaptor.getValue();
        var recipient = List.of(TEST_CASE_ID.toString(), caseData.getApplicant2().getFullName(), "switch-to-sole-co-letter");
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("switch-to-sole-co-letter");
        assertThat(print.getLetters().size()).isEqualTo(1);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(switchToSoleCoLetter.getValue());
        assertThat(print.getRecipients()).isEqualTo(recipient);
    }

    @Test
    void shouldNotPrintSwitchToSoleCoLetterIfRequiredDocumentNotPresent() {

        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .documents(CaseDocuments.builder().build())
            .build();

        printer.print(caseData, TEST_CASE_ID, caseData.getApplicant2());

        verifyNoInteractions(bulkPrintService);
    }
}
