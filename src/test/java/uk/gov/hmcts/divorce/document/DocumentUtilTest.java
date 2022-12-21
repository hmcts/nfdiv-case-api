package uk.gov.hmcts.divorce.document;


import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.model.Letter;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.RESPONDENT;
import static uk.gov.hmcts.divorce.document.DocumentUtil.divorceDocumentFrom;
import static uk.gov.hmcts.divorce.document.DocumentUtil.documentFrom;
import static uk.gov.hmcts.divorce.document.DocumentUtil.documentsWithDocumentType;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getLettersBasedOnContactPrivacy;
import static uk.gov.hmcts.divorce.document.DocumentUtil.isConfidential;
import static uk.gov.hmcts.divorce.document.DocumentUtil.isDocumentApplicableForConfidentiality;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.DocumentUtil.mapToLetters;
import static uk.gov.hmcts.divorce.document.DocumentUtil.removeDocumentsBasedOnContactPrivacy;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D10;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.OTHER;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class DocumentUtilTest {

    private static final String DOC_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003";
    private static final String DOC_BINARY_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003/binary";
    private static final String PDF_FILENAME = "draft-divorce-application-1616591401473378.pdf";
    private static final String URL = "url";
    private static final String FILENAME = "filename";
    private static final String BINARY_URL = "binaryUrl";

    @Test
    void shouldConvertFromDocumentInfoToDocument() {

        final Document document = documentFrom(documentInfo());

        assertThat(document)
            .extracting(URL, FILENAME, BINARY_URL)
            .contains(
                DOC_URL,
                PDF_FILENAME,
                DOC_BINARY_URL);
    }

    @Test
    void shouldCreateDivorceDocumentFromDocumentInfoAndDocumentType() {

        final DivorceDocument divorceDocument = divorceDocumentFrom(documentInfo(), OTHER);

        assertThat(divorceDocument.getDocumentType()).isEqualTo(OTHER);
        assertThat(divorceDocument.getDocumentFileName()).isEqualTo(PDF_FILENAME);
        assertThat(divorceDocument
            .getDocumentLink())
            .extracting(URL, FILENAME, BINARY_URL)
            .contains(
                DOC_URL,
                PDF_FILENAME,
                DOC_BINARY_URL);
    }

    @Test
    void shouldReturnListOfLetterOfGivenDocumentTypeIfPresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(MARRIAGE_CERTIFICATE)
                .build())
            .build();

        final List<Letter> letters = lettersWithDocumentType(
            asList(doc1, doc2),
            MARRIAGE_CERTIFICATE);

        assertThat(letters.size()).isEqualTo(1);
        assertThat(letters.get(0).getDivorceDocument()).isSameAs(doc2.getValue());
    }

    @Test
    void shouldNotFindDocumentOfGivenDocumentTypeIfNotPresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(MARRIAGE_CERTIFICATE)
                .build())
            .build();

        final List<Letter> letters = lettersWithDocumentType(
            asList(doc1, doc2),
            NAME_CHANGE_EVIDENCE);

        assertThat(letters.size()).isZero();
    }

    @Test
    void shouldReturnEmptyListIfNullDocumentList() {
        final List<Letter> letters = lettersWithDocumentType(null, NAME_CHANGE_EVIDENCE);
        assertThat(letters.size()).isZero();
    }

    @Test
    void shouldReturnEmptyListIfEmptyDocumentList() {
        final List<Letter> letters = lettersWithDocumentType(emptyList(), NAME_CHANGE_EVIDENCE);
        assertThat(letters.size()).isZero();
    }

    @Test
    void shouldReturnListOfLettersOfGivenDocumentTypesIfPresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(MARRIAGE_CERTIFICATE)
                .build())
            .build();

        final ListValue<DivorceDocument> doc3 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NAME_CHANGE_EVIDENCE)
                .build())
            .build();

        final List<Letter> letters = lettersWithDocumentType(
            asList(doc1, doc2, doc3),
            NAME_CHANGE_EVIDENCE);

        assertThat(letters.size()).isEqualTo(1);
        assertThat(letters.get(0).getDivorceDocument()).isSameAs(doc3.getValue());
    }

    @Test
    void shouldReturnTrueIfDocumentWithTypeD10IsPresent() {

        final ListValue<DivorceDocument> d10Document = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(D10)
                .build())
            .build();

        final boolean d10IsPresent = documentsWithDocumentType(
            singletonList(d10Document),
            D10);

        assertThat(d10IsPresent).isTrue();
    }

    @Test
    void shouldReturnFalseIfDocumentWithTypeD10IsNotPresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final boolean d10IsPresent = documentsWithDocumentType(
            singletonList(doc1),
            D10);

        assertThat(d10IsPresent).isFalse();
    }

    @Test
    void shouldReturnFalseIfNullDocumentList() {
        final boolean d10IsPresent = documentsWithDocumentType(null, D10);
        assertThat(d10IsPresent).isFalse();
    }

    @Test
    void shouldReturnFalseIfEmptyDocumentList() {
        final boolean d10IsPresent = documentsWithDocumentType(emptyList(), D10);
        assertThat(d10IsPresent).isFalse();
    }

    @Test
    void mapToLettersShouldReturnListOfLettersOfGivenDocumentType() {

        final ListValue<Document> doc1 = ListValue.<Document>builder()
            .value(Document.builder().filename("doc1.pdf").build())
            .build();

        final ListValue<Document> doc2 = ListValue.<Document>builder()
            .value(Document.builder().filename("doc2.pdf").build())
            .build();

        final List<Letter> letters = mapToLetters(asList(doc1, doc2), NOTICE_OF_PROCEEDINGS_APP_1);

        assertThat(letters.size()).isEqualTo(2);
        assertThat(
            letters.stream().map(letter -> letter.getDivorceDocument().getDocumentFileName()).collect(Collectors.toList()))
            .containsExactlyInAnyOrder("doc1.pdf", "doc2.pdf");
        assertThat(
            letters.stream().map(letter -> letter.getDivorceDocument().getDocumentType()).collect(Collectors.toList()))
            .containsExactlyInAnyOrder(NOTICE_OF_PROCEEDINGS_APP_1, NOTICE_OF_PROCEEDINGS_APP_1);
    }

    @Test
    public void isConfidentialShouldReturnTrueWhenDocumentTypeIsGeneralLetterAndGeneralLetterPartyIsApplicant() {
        var caseData = CaseData.builder().build();
        caseData.getGeneralLetter().setGeneralLetterParties(APPLICANT);
        caseData.getApplicant1().setContactDetailsType(PRIVATE);

        assertTrue(isConfidential(caseData, GENERAL_LETTER));
    }

    @Test
    public void isConfidentialShouldReturnTrueWhenDocumentTypeIsGeneralLetterAndGeneralLetterPartyIsRespondent() {
        var caseData = CaseData.builder().build();
        caseData.getGeneralLetter().setGeneralLetterParties(RESPONDENT);
        caseData.getApplicant2().setContactDetailsType(PRIVATE);

        assertTrue(isConfidential(caseData, GENERAL_LETTER));
    }


    @Test
    public void isConfidentialShouldReturnFalseWhenDocumentTypeIsGeneralLetterAndGeneralLetterPartyIsOther() {
        var caseData = CaseData.builder().build();
        caseData.getGeneralLetter().setGeneralLetterParties(GeneralParties.OTHER);
        caseData.getApplicant2().setContactDetailsType(PRIVATE);

        assertFalse(isConfidential(caseData, GENERAL_LETTER));
    }

    @Test
    public void shouldReturnConfidentialLettersWhenDocumentIsApplicableForConfidentialityAndApplicantContactIsPrivate() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NOTICE_OF_PROCEEDINGS_APP_1)
                .build())
            .build();

        final ListValue<ConfidentialDivorceDocument> doc3 = ListValue.<ConfidentialDivorceDocument>builder()
            .value(ConfidentialDivorceDocument.builder()
                .confidentialDocumentsReceived(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2)
                .build())
            .build();

        final ListValue<ConfidentialDivorceDocument> doc4 = ListValue.<ConfidentialDivorceDocument>builder()
            .value(ConfidentialDivorceDocument.builder()
                .confidentialDocumentsReceived(ConfidentialDocumentsReceived.GENERAL_LETTER)
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .contactDetailsType(PUBLIC)
                .build())
            .applicant2(Applicant.builder()
                .contactDetailsType(PRIVATE)
                .build())
            .generalLetter(GeneralLetter.builder()
                .generalLetterParties(RESPONDENT)
                .build())
            .documents(CaseDocuments.builder()
                .confidentialDocumentsGenerated(Lists.newArrayList(doc3, doc4))
                .documentsGenerated(Lists.newArrayList(doc1, doc2))
                .build())
            .build();

        List<Letter> nonConfidentialNop1 = getLettersBasedOnContactPrivacy(caseData, NOTICE_OF_PROCEEDINGS_APP_1);
        List<Letter> confidentialNop2 = getLettersBasedOnContactPrivacy(caseData, NOTICE_OF_PROCEEDINGS_APP_2);
        List<Letter> confidentialGeneralLetter = getLettersBasedOnContactPrivacy(caseData, GENERAL_LETTER);

        assertThat(nonConfidentialNop1.size()).isEqualTo(1);
        assertThat(nonConfidentialNop1.get(0).getConfidentialDivorceDocument()).isNull();
        assertThat(nonConfidentialNop1.get(0).getDivorceDocument().getDocumentType()).isEqualTo(NOTICE_OF_PROCEEDINGS_APP_1);

        assertThat(confidentialNop2.size()).isEqualTo(1);
        assertThat(confidentialNop2.get(0).getDivorceDocument()).isNull();
        assertThat(confidentialNop2.get(0).getConfidentialDivorceDocument().getConfidentialDocumentsReceived())
            .isEqualTo(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2);

        assertThat(confidentialGeneralLetter.size()).isEqualTo(1);
        assertThat(confidentialGeneralLetter.get(0).getDivorceDocument()).isNull();
        assertThat(confidentialGeneralLetter.get(0).getConfidentialDivorceDocument().getConfidentialDocumentsReceived())
            .isEqualTo(ConfidentialDocumentsReceived.GENERAL_LETTER);
    }

    @Test
    public void shouldReturnTrueForApplicant1WhenGivenDocumentTypeIsApplicableForConfidentiality() {
        assertTrue(isDocumentApplicableForConfidentiality(NOTICE_OF_PROCEEDINGS_APP_1, true));
    }

    @Test
    public void shouldReturnTrueForApplicant2WhenGivenDocumentTypeIsApplicableForConfidentiality() {
        assertTrue(isDocumentApplicableForConfidentiality(NOTICE_OF_PROCEEDINGS_APP_2, false));
    }

    @Test
    public void shouldReturnTrueWhenGivenDocumentTypeIsApplicableForConfidentiality() {
        assertTrue(isDocumentApplicableForConfidentiality(GENERAL_LETTER, true));
    }

    @Test
    public void shouldReturnFalseWhenGivenDocumentTypeIsNotApplicableForConfidentiality() {
        assertFalse(isDocumentApplicableForConfidentiality(APPLICATION, true));
    }

    @Test
    public void shouldReturnTrueForApplicant1WhenFOCoverLetterTypeIsApplicableForConfidentiality() {
        assertTrue(isDocumentApplicableForConfidentiality(FINAL_ORDER_GRANTED_COVER_LETTER_APP_1, true));
    }

    @Test
    public void shouldReturnTrueForApplicant2WhenFOCoverLetterTypeIsApplicableForConfidentiality() {
        assertTrue(isDocumentApplicableForConfidentiality(FINAL_ORDER_GRANTED_COVER_LETTER_APP_2, false));
    }

    @Test
    public void shouldReturnConfidentialDocumentType() {
        List<DocumentType> documentTypes = Lists.newArrayList(
            NOTICE_OF_PROCEEDINGS_APP_1,
            NOTICE_OF_PROCEEDINGS_APP_2,
            GENERAL_LETTER,
            FINAL_ORDER_GRANTED_COVER_LETTER_APP_1,
            FINAL_ORDER_GRANTED_COVER_LETTER_APP_2
        );

        assertThat(documentTypes.stream().map(DocumentUtil::getConfidentialDocumentType)
                .collect(Collectors.toList()))
            .containsExactly(
                ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1,
                ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2,
                ConfidentialDocumentsReceived.GENERAL_LETTER,
                ConfidentialDocumentsReceived.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1,
                ConfidentialDocumentsReceived.FINAL_ORDER_GRANTED_COVER_LETTER_APP_2
            );
    }

    @Test
    public void shouldRemoveDocumentsFromConfidentialDocsWhenContactIsPrivate() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setContactDetailsType(PRIVATE);

        caseData.setDocuments(CaseDocuments.builder()
            .documentsGenerated(Lists.newArrayList(
                ListValue.<DivorceDocument>builder()
                    .value(
                        DivorceDocument.builder()
                            .documentType(NOTICE_OF_PROCEEDINGS_APP_1)
                            .build())
                    .build()))
            .confidentialDocumentsGenerated(Lists.newArrayList(
                ListValue.<ConfidentialDivorceDocument>builder()
                .value(
                    ConfidentialDivorceDocument.builder()
                        .confidentialDocumentsReceived(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2)
                        .build())
                .build()))
            .build());

        removeDocumentsBasedOnContactPrivacy(caseData, NOTICE_OF_PROCEEDINGS_APP_2);

        assertThat(caseData.getDocuments().getConfidentialDocumentsGenerated().size()).isEqualTo(0);
        assertThat(caseData.getDocuments().getDocumentsGenerated().size()).isEqualTo(1);
    }

    @Test
    public void shouldRemoveDocumentsFromDocumentsGeneratedWhenContactIsPublic() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setContactDetailsType(PUBLIC);

        caseData.setDocuments(CaseDocuments.builder()
            .documentsGenerated(Lists.newArrayList(
                ListValue.<DivorceDocument>builder()
                    .value(
                        DivorceDocument.builder()
                            .documentType(NOTICE_OF_PROCEEDINGS_APP_1)
                            .build())
                    .build()))
            .confidentialDocumentsGenerated(Lists.newArrayList(
                ListValue.<ConfidentialDivorceDocument>builder()
                    .value(
                        ConfidentialDivorceDocument.builder()
                            .confidentialDocumentsReceived(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2)
                            .build())
                    .build()))
            .build());

        removeDocumentsBasedOnContactPrivacy(caseData, NOTICE_OF_PROCEEDINGS_APP_1);

        assertThat(caseData.getDocuments().getConfidentialDocumentsGenerated().size()).isEqualTo(1);
        assertThat(caseData.getDocuments().getDocumentsGenerated().size()).isEqualTo(0);
    }

    private DocumentInfo documentInfo() {
        return new DocumentInfo(
            DOC_URL,
            PDF_FILENAME,
            DOC_BINARY_URL
        );
    }
}
