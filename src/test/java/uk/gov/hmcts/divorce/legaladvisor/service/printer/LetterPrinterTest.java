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
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetterDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.document.GeneralLetterRecipient;
import uk.gov.hmcts.divorce.document.GeneralLetterRecipientResolver;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.print.GeneralLetterDocumentPack.LETTER_TYPE_GENERAL_LETTER;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class LetterPrinterTest {

    private static final String TEST_LETTER_NAME = "test-letter-name";

    @Mock
    private DocumentGenerator documentGenerator;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private GeneralLetterRecipientResolver generalLetterRecipientResolver;

    @InjectMocks
    private LetterPrinter letterPrinter;

    @Captor
    private ArgumentCaptor<Print> printArgumentCaptor;

    @Test
    void shouldPrintLettersWhenSizeOfListReturnedMatchesDocumentPackSize() {
        CaseData caseData = validApplicant1CaseData();
        setApplicantAddress(caseData);
        long caseId = TEST_CASE_ID;
        Applicant applicant = caseData.getApplicant1();
        DocumentPackInfo documentPackInfo = getDocumentPackInfo();

        List<Letter> expectedLetters = new ArrayList<>(
            documentPackInfo.documentPack().keySet().stream().map(this::getLetterFromDocumentType).toList()
        );
        when(documentGenerator.generateDocuments(caseData, caseId, applicant, documentPackInfo)).thenReturn(expectedLetters);

        letterPrinter.sendLetters(caseData, caseId, applicant, documentPackInfo, TEST_LETTER_NAME);

        verify(bulkPrintService).print(printArgumentCaptor.capture());

        Print print = printArgumentCaptor.getValue();
        assertThat(print.getLetters()).containsAll(expectedLetters);
        assertThat(print.getLetterType()).isEqualTo(TEST_LETTER_NAME);
    }

    @Test
    void shouldPrintLettersWhenSizeOfListReturnedMatchesDocumentPackSizeForGeneralLetter() {
        CaseData caseData = validApplicant1CaseData();
        setApplicantAddress(caseData);

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

        List<Letter> expectedLetters = new ArrayList<>(
            documentPackInfo.documentPack().keySet().stream().map(this::getLetterFromDocumentType)
                .toList()
        );

        when(documentGenerator.generateDocuments(caseData, caseId, applicant, documentPackInfo)).thenReturn(expectedLetters);
        when(generalLetterRecipientResolver.resolve(any(), any())).thenReturn(applicantRecipient());

        letterPrinter.sendLetters(caseData, caseId, applicant, documentPackInfo, LETTER_TYPE_GENERAL_LETTER);

        verify(bulkPrintService).print(printArgumentCaptor.capture());

        Print print = printArgumentCaptor.getValue();
        assertThat(print.getLetters()).containsAll(expectedLetters);
        assertThat(print.getIsInternational()).isEqualTo(false);
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_GENERAL_LETTER);
    }

    @Test
    void shouldPrintLettersWithInternationalFlagSetWhenApplicantAddressOverseas() {
        CaseData caseData = validApplicant1CaseData();
        setApplicantAddress(caseData);
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

        List<Letter> expectedLetters = new ArrayList<>(
            documentPackInfo.documentPack().keySet().stream().map(this::getLetterFromDocumentType)
                .toList());

        when(documentGenerator.generateDocuments(caseData, caseId, applicant, documentPackInfo)).thenReturn(expectedLetters);
        when(generalLetterRecipientResolver.resolve(any(), any())).thenReturn(new GeneralLetterRecipient(
            GeneralParties.APPLICANT,
            applicant.getFullName(),
            applicant.getCorrespondenceAddressWithoutConfidentialCheck(),
            YES,
            "wife"
        ));

        letterPrinter.sendLetters(caseData, caseId, applicant, documentPackInfo, LETTER_TYPE_GENERAL_LETTER);

        verify(bulkPrintService).print(printArgumentCaptor.capture());

        Print print = printArgumentCaptor.getValue();
        assertThat(print.getLetters()).containsAll(expectedLetters);
        assertThat(print.getIsInternational()).isEqualTo(true);
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_GENERAL_LETTER);
    }

    @Test
    void shouldPrintAttachmentsWithGeneralLetter() {
        CaseData caseData = validApplicant1CaseData();
        setApplicantAddress(caseData);

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

        List<Letter> expectedLetters = new ArrayList<>(
            documentPackInfo.documentPack().keySet().stream().map(this::getLetterFromDocumentType)
                .toList());

        when(documentGenerator.generateDocuments(caseData, caseId, applicant, documentPackInfo)).thenReturn(expectedLetters);
        when(generalLetterRecipientResolver.resolve(any(), any())).thenReturn(applicantRecipient());

        letterPrinter.sendLetters(caseData, caseId, applicant, documentPackInfo, LETTER_TYPE_GENERAL_LETTER);

        verify(bulkPrintService).print(printArgumentCaptor.capture());

        Print print = printArgumentCaptor.getValue();
        assertThat(print.getLetters()).containsAll(expectedLetters);
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_GENERAL_LETTER);
    }

    @Test
    void shouldThrowExceptionWhenSizeOfListReturnedIsNotEqualToDocumentPackSize() {
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
    void shouldThrowExceptionWhenListReturnedIsEmpty() {
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
    void shouldNotPrintWhenGeneralLetterDetailsAreMissing() {
        CaseData caseData = validApplicant1CaseData();

        caseData.setGeneralLetters(null);

        Applicant applicant = caseData.getApplicant1();

        long caseId = TEST_CASE_ID;

        DocumentPackInfo documentPackInfo = new DocumentPackInfo(
            ImmutableMap.of(GENERAL_LETTER, Optional.empty()),
            ImmutableMap.of()
        );

        List<Letter> expectedLetters = new ArrayList<>(
            documentPackInfo.documentPack().keySet().stream().map(this::getLetterFromDocumentType)
                .toList());

        when(documentGenerator.generateDocuments(caseData, caseId, applicant, documentPackInfo)).thenReturn(expectedLetters);
        when(generalLetterRecipientResolver.resolve(any(), any())).thenReturn(blankOtherRecipient());

        letterPrinter.sendLetters(caseData, caseId, applicant, documentPackInfo, LETTER_TYPE_GENERAL_LETTER);

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotPrintWhenApplicantAddressIsBlankButStillStoreLetterPack() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setAddress(null);
        long caseId = TEST_CASE_ID;
        Applicant applicant = caseData.getApplicant1();
        DocumentPackInfo documentPackInfo = getDocumentPackInfo();

        List<Letter> expectedLetters = new ArrayList<>(
            documentPackInfo.documentPack().keySet().stream().map(this::getLetterFromDocumentType).toList()
        );
        when(documentGenerator.generateDocuments(caseData, caseId, applicant, documentPackInfo)).thenReturn(expectedLetters);

        letterPrinter.sendLetters(caseData, caseId, applicant, documentPackInfo, TEST_LETTER_NAME);

        assertThat(caseData.getDocuments().getLetterPacks()).hasSize(1);
        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotPrintGeneralLetterWhenOtherRecipientAddressIsBlank() {
        CaseData caseData = validApplicant1CaseData();
        caseData.setGeneralLetter(GeneralLetter.builder()
            .generalLetterParties(GeneralParties.OTHER)
            .otherRecipientName("Recipient")
            .build());

        long caseId = TEST_CASE_ID;
        Applicant applicant = caseData.getApplicant1();
        DocumentPackInfo documentPackInfo = new DocumentPackInfo(
            ImmutableMap.of(DocumentType.GENERAL_LETTER, Optional.empty()),
            ImmutableMap.of()
        );

        List<Letter> expectedLetters = new ArrayList<>(
            documentPackInfo.documentPack().keySet().stream().map(this::getLetterFromDocumentType).toList()
        );

        when(documentGenerator.generateDocuments(caseData, caseId, applicant, documentPackInfo)).thenReturn(expectedLetters);
        when(generalLetterRecipientResolver.resolve(any(), any())).thenReturn(blankOtherRecipient());

        letterPrinter.sendLetters(caseData, caseId, applicant, documentPackInfo, LETTER_TYPE_GENERAL_LETTER);

        verifyNoInteractions(bulkPrintService);
        verifyNoMoreInteractions(documentGenerator);
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

    private void setApplicantAddress(CaseData caseData) {
        caseData.getApplicant1().setAddress(AddressGlobalUK.builder()
            .addressLine1("line 1")
            .postTown("town")
            .postCode("postcode")
            .country("UK")
            .build());
    }

    private GeneralLetterRecipient applicantRecipient() {
        return new GeneralLetterRecipient(
            GeneralParties.APPLICANT,
            "Applicant",
            "line 1\ntown\nUK\npostcode",
            NO,
            "wife"
        );
    }

    private GeneralLetterRecipient blankOtherRecipient() {
        return new GeneralLetterRecipient(
            GeneralParties.OTHER,
            "Other",
            null,
            NO,
            "civil partner"
        );
    }
}
