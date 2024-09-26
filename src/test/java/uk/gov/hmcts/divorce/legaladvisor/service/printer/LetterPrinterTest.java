package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetterDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.print.GeneralLetterDocumentPack.LETTER_TYPE_GENERAL_LETTER;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class LetterPrinterTest {

    private static final String TEST_LETTER_NAME = "test-letter-name";

    @Mock
    private DocumentGenerator documentGenerator;

    @Mock
    private BulkPrintService bulkPrintService;

    @InjectMocks
    private LetterPrinter letterPrinter;

    @Captor
    private ArgumentCaptor<Print> printArgumentCaptor;

    @Test
    public void shouldPrintLettersWhenSizeOfListReturnedMatchesDocumentPackSize() {
        CaseData caseData = validApplicant1CaseData();
        long caseId = TEST_CASE_ID;
        Applicant applicant = caseData.getApplicant1();
        DocumentPackInfo documentPackInfo = getDocumentPackInfo();

        List<Letter> expectedLetters = documentPackInfo.documentPack().keySet().stream().map(this::getLetterFromDocumentType).toList();
        when(documentGenerator.generateDocuments(caseData, caseId, applicant, documentPackInfo)).thenReturn(expectedLetters);

        letterPrinter.sendLetters(caseData, caseId, applicant, documentPackInfo, TEST_LETTER_NAME);

        verify(bulkPrintService).print(printArgumentCaptor.capture());

        Print print = printArgumentCaptor.getValue();
        assertThat(print.getLetters()).containsAll(expectedLetters);
        assertThat(print.getLetterType()).isEqualTo(TEST_LETTER_NAME);
    }

    @Test
    public void shouldPrintLettersWhenSizeOfListReturnedMatchesDocumentPackSizeForGeneralLetter() {
        CaseData caseData = validApplicant1CaseData();

        Document generalLetter = Document.builder()
            .filename("GeneralLetter.pdf")
            .build();

        List<ListValue<GeneralLetterDetails>> generalLetters = new ArrayList<>();
        generalLetters.add(
            ListValue.<GeneralLetterDetails>builder()
                .value(
                    GeneralLetterDetails.builder()
                        .generalLetterParties(GeneralParties.APPLICANT)
                        .generalLetterLink(generalLetter)
                        .build()
                ).build()
        );
        caseData.setGeneralLetters(generalLetters);

        long caseId = TEST_CASE_ID;
        Applicant applicant = caseData.getApplicant1();
        DocumentPackInfo documentPackInfo = new DocumentPackInfo(
            ImmutableMap.of(DocumentType.GENERAL_LETTER, Optional.empty()),
            ImmutableMap.of()
        );

        List<Letter> expectedLetters = documentPackInfo.documentPack().keySet().stream().map(this::getLetterFromDocumentType)
                .collect(Collectors.toList());

        when(documentGenerator.generateDocuments(caseData, caseId, applicant, documentPackInfo)).thenReturn(expectedLetters);

        letterPrinter.sendLetters(caseData, caseId, applicant, documentPackInfo, LETTER_TYPE_GENERAL_LETTER);

        verify(bulkPrintService).print(printArgumentCaptor.capture());

        Print print = printArgumentCaptor.getValue();
        assertThat(print.getLetters()).containsAll(expectedLetters);
        assertThat(print.getIsInternational()).isEqualTo(false);
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_GENERAL_LETTER);
    }

    @Test
    public void shouldPrintLettersWithInternationalFlagSetWhenApplicantAddressOverseas() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setAddressOverseas(YES);

        Document generalLetter = Document.builder()
            .filename("GeneralLetter.pdf")
            .build();

        List<ListValue<GeneralLetterDetails>> generalLetters = new ArrayList<>();
        generalLetters.add(
            ListValue.<GeneralLetterDetails>builder()
                .value(
                    GeneralLetterDetails.builder()
                        .generalLetterParties(GeneralParties.APPLICANT)
                        .generalLetterLink(generalLetter)
                        .build()
                ).build()
        );
        caseData.setGeneralLetters(generalLetters);

        long caseId = TEST_CASE_ID;
        Applicant applicant = caseData.getApplicant1();
        DocumentPackInfo documentPackInfo = new DocumentPackInfo(
            ImmutableMap.of(DocumentType.GENERAL_LETTER, Optional.empty()),
            ImmutableMap.of()
        );

        List<Letter> expectedLetters = documentPackInfo.documentPack().keySet().stream().map(this::getLetterFromDocumentType)
            .collect(Collectors.toList());

        when(documentGenerator.generateDocuments(caseData, caseId, applicant, documentPackInfo)).thenReturn(expectedLetters);

        letterPrinter.sendLetters(caseData, caseId, applicant, documentPackInfo, LETTER_TYPE_GENERAL_LETTER);

        verify(bulkPrintService).print(printArgumentCaptor.capture());

        Print print = printArgumentCaptor.getValue();
        assertThat(print.getLetters()).containsAll(expectedLetters);
        assertThat(print.getIsInternational()).isEqualTo(true);
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_GENERAL_LETTER);
    }

    @Test
    public void shouldPrintAttachmentsWithGeneralLetter() {
        CaseData caseData = validApplicant1CaseData();

        Document generalLetter = Document.builder()
            .filename("GeneralLetter.pdf")
            .build();

        ListValue<Document> attachment = ListValue.<Document>builder()
            .value(Document.builder()
                .filename("some-attachment.pdf")
                .build())
            .build();

        List<ListValue<GeneralLetterDetails>> generalLetters = new ArrayList<>();
        generalLetters.add(
            ListValue.<GeneralLetterDetails>builder()
                .value(
                    GeneralLetterDetails.builder()
                        .generalLetterParties(GeneralParties.APPLICANT)
                        .generalLetterLink(generalLetter)
                        .generalLetterAttachmentLinks(Lists.newArrayList(attachment))
                        .build()
                ).build()
        );
        caseData.setGeneralLetters(generalLetters);
        long caseId = TEST_CASE_ID;

        Applicant applicant = caseData.getApplicant1();

        DocumentPackInfo documentPackInfo = new DocumentPackInfo(
            ImmutableMap.of(DocumentType.GENERAL_LETTER, Optional.empty()),
            ImmutableMap.of()
        );

        List<Letter> expectedLetters = documentPackInfo.documentPack().keySet().stream().map(this::getLetterFromDocumentType)
                .collect(Collectors.toList());

        when(documentGenerator.generateDocuments(caseData, caseId, applicant, documentPackInfo)).thenReturn(expectedLetters);

        letterPrinter.sendLetters(caseData, caseId, applicant, documentPackInfo, LETTER_TYPE_GENERAL_LETTER);

        verify(bulkPrintService).print(printArgumentCaptor.capture());

        Print print = printArgumentCaptor.getValue();
        assertThat(print.getLetters()).containsAll(expectedLetters);
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_GENERAL_LETTER);
    }

    @Test
    public void shouldThrowExceptionWhenSizeOfListReturnedIsNotEqualToDocumentPackSize() {
        CaseData caseData = validApplicant1CaseData();
        long caseId = TEST_CASE_ID;
        Applicant applicant = caseData.getApplicant1();
        DocumentPackInfo documentPackInfo = getDocumentPackInfo();

        Letter letter = new Letter(Document.builder().filename("coversheet").build(), 1);

        when(documentGenerator.generateDocuments(caseData, caseId, applicant, documentPackInfo)).thenReturn(List.of(letter));

        assertThrows(
            IllegalArgumentException.class,
            () -> letterPrinter.sendLetters(caseData, caseId, applicant, documentPackInfo, TEST_LETTER_NAME)
        );

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    public void shouldThrowExceptionWhenListReturnedIsEmpty() {
        CaseData caseData = validApplicant1CaseData();
        long caseId = TEST_CASE_ID;
        Applicant applicant = caseData.getApplicant1();
        DocumentPackInfo documentPackInfo = getDocumentPackInfo();

        when(documentGenerator.generateDocuments(caseData, caseId, applicant, documentPackInfo)).thenReturn(emptyList());

        assertThrows(
            IllegalArgumentException.class,
            () -> letterPrinter.sendLetters(caseData, caseId, applicant, documentPackInfo, TEST_LETTER_NAME)
        );

        verifyNoInteractions(bulkPrintService);
    }


    @Test
    public void shouldThrowExceptionWhenGeneralLetterIsNull() {
        CaseData caseData = validApplicant1CaseData();

        caseData.setGeneralLetters(null);

        Applicant applicant = caseData.getApplicant1();

        long caseId = TEST_CASE_ID;

        DocumentPackInfo documentPackInfo = new DocumentPackInfo(
            ImmutableMap.of(GENERAL_LETTER, Optional.empty()),
            ImmutableMap.of()
        );

        List<Letter> expectedLetters = documentPackInfo.documentPack().keySet().stream().map(this::getLetterFromDocumentType)
            .collect(Collectors.toList());

        when(documentGenerator.generateDocuments(caseData, caseId, applicant, documentPackInfo)).thenReturn(expectedLetters);

        assertThrows(
            IllegalArgumentException.class,
            () -> letterPrinter.sendLetters(caseData, caseId, applicant, documentPackInfo, LETTER_TYPE_GENERAL_LETTER)
        );

        verifyNoInteractions(bulkPrintService);
    }

    private DocumentPackInfo getDocumentPackInfo() {
        return new DocumentPackInfo(
            ImmutableMap.of(
                DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
                DocumentType.APPLICATION, Optional.empty()
            ),
            ImmutableMap.of(
                COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME
            )
        );
    }

    private Letter getLetterFromDocumentType(DocumentType documentType) {
        return new Letter(Document.builder().filename(documentType.toString()).build(), 1);
    }
}
