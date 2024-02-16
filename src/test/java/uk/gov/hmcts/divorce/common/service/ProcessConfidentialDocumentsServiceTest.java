package uk.gov.hmcts.divorce.common.service;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetterDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.BAILIFF_SERVICE_REFUSED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class ProcessConfidentialDocumentsServiceTest {

    private final ListValue<DivorceDocument> applicant1Nop = ListValue.<DivorceDocument>builder()
        .id("1111")
        .value(buildDivorceDocument(NOTICE_OF_PROCEEDINGS_APP_1))
        .build();

    private final ListValue<DivorceDocument> applicant2Nop = ListValue.<DivorceDocument>builder()
        .id("2222")
        .value(buildDivorceDocument(NOTICE_OF_PROCEEDINGS_APP_2))
        .build();

    private final ListValue<DivorceDocument> applicationDoc = ListValue.<DivorceDocument>builder()
        .id("3333")
        .value(buildDivorceDocument(DocumentType.APPLICATION))
        .build();

    private final ListValue<DivorceDocument> bailiffRefusedDoc = ListValue.<DivorceDocument>builder()
        .id("3333")
        .value(buildDivorceDocument(DocumentType.BAILIFF_SERVICE_REFUSED))
        .build();

    private final ListValue<DivorceDocument> coCanApply = ListValue.<DivorceDocument>builder()
        .id("3333")
        .value(buildDivorceDocument(DocumentType.CONDITIONAL_ORDER_CAN_APPLY))
        .build();

    private final ListValue<DivorceDocument> foCanApply = ListValue.<DivorceDocument>builder()
        .id("3333")
        .value(buildDivorceDocument(DocumentType.FINAL_ORDER_CAN_APPLY))
        .build();

    private final ListValue<ConfidentialDivorceDocument> applicant1NopConfidential
        = ListValue.<ConfidentialDivorceDocument>builder()
        .id("1111")
        .value(buildConfidentialDivorceDocument(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1))
        .build();

    private final ListValue<ConfidentialDivorceDocument> applicant2NopConfidential
        = ListValue.<ConfidentialDivorceDocument>builder()
        .id("2222")
        .value(buildConfidentialDivorceDocument(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2))
        .build();

    private final ListValue<DivorceDocument> applicant1GeneralLetter = ListValue.<DivorceDocument>builder()
        .id("4444")
        .value(DivorceDocument.builder()
            .documentType(GENERAL_LETTER)
            .documentLink(Document.builder()
                .url("generalLetter1Url")
                .build())
            .build())
        .build();

    private final ListValue<DivorceDocument> applicant2GeneralLetter = ListValue.<DivorceDocument>builder()
        .id("5555")
        .value(DivorceDocument.builder()
            .documentType(GENERAL_LETTER)
            .documentLink(Document.builder()
                .url("generalLetter2Url")
                .build())
            .build())
        .build();

    private final ListValue<GeneralLetterDetails> applicant1GeneralLetterDetail = ListValue.<GeneralLetterDetails>builder()
        .id("6666")
        .value(GeneralLetterDetails.builder()
            .generalLetterParties(GeneralParties.APPLICANT)
            .generalLetterLink(Document.builder()
                .url("generalLetter1Url")
                .build())
            .build())
        .build();

    private final ListValue<GeneralLetterDetails> applicant2GeneralLetterDetail = ListValue.<GeneralLetterDetails>builder()
        .id("7777")
        .value(GeneralLetterDetails.builder()
            .generalLetterParties(GeneralParties.RESPONDENT)
            .generalLetterLink(Document.builder()
                .url("generalLetter2Url")
                .build())
            .build())
        .build();

    @InjectMocks
    private ProcessConfidentialDocumentsService documentsService;


    @Test
    public void processDocumentsShouldMoveNOPDocumentToConfidentialDocumentsGeneratedWhenContactIsPrivateForApplicant1() {

        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().contactDetailsType(ContactDetailsType.PRIVATE).build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(Lists.newArrayList(applicant1Nop, applicant2Nop, applicationDoc))
                .build())
            .build();

        documentsService.processDocuments(caseData, TEST_CASE_ID);

        List<ListValue<DivorceDocument>> nonConfidentialDocuments = caseData.getDocuments().getDocumentsGenerated();
        List<ListValue<ConfidentialDivorceDocument>> confidentialDocuments = caseData.getDocuments().getConfidentialDocumentsGenerated();

        assertEquals(2, nonConfidentialDocuments.size());
        assertEquals(1, confidentialDocuments.size());

        assertThat(confidentialDocuments.stream()
            .map(doc -> doc.getValue().getConfidentialDocumentsReceived())
            .collect(Collectors.toList()),  contains(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1)
        );
    }

    @Test
    public void processDocumentsShouldNotMoveGeneratedDocumentsToConfidentialIfTheyDoNotHaveAMapping() {
        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().contactDetailsType(ContactDetailsType.PRIVATE).build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(Lists.newArrayList(applicant1Nop, bailiffRefusedDoc, coCanApply))
                .build())
            .build();

        documentsService.processDocuments(caseData, TEST_CASE_ID);

        List<ListValue<DivorceDocument>> nonConfidentialDocuments = caseData.getDocuments().getDocumentsGenerated();
        List<ListValue<ConfidentialDivorceDocument>> confidentialDocuments = caseData.getDocuments().getConfidentialDocumentsGenerated();

        assertEquals(1, nonConfidentialDocuments.size());
        assertEquals(2, confidentialDocuments.size());

        assertThat(nonConfidentialDocuments.stream()
            .map(doc -> doc.getValue().getDocumentType())
            .collect(Collectors.toList()),  containsInAnyOrder(
            DocumentType.BAILIFF_SERVICE_REFUSED)
        );
    }

    @Test
    public void processDocumentsShouldNotMoveConfidentialDocumentsToDocumentsGeneratedWhenContactIsNotPrivateForApplicant1() {

        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().contactDetailsType(ContactDetailsType.PUBLIC).build())
            .documents(CaseDocuments.builder()
                .confidentialDocumentsGenerated(Lists.newArrayList(applicant1NopConfidential, applicant2NopConfidential))
                .documentsGenerated(Lists.newArrayList(applicationDoc))
                .build())
            .build();

        documentsService.processDocuments(caseData, TEST_CASE_ID);

        List<ListValue<DivorceDocument>> nonConfidentialDocuments = caseData.getDocuments().getDocumentsGenerated();
        List<ListValue<ConfidentialDivorceDocument>> confidentialDocuments = caseData.getDocuments().getConfidentialDocumentsGenerated();

        assertEquals(1, nonConfidentialDocuments.size());
        assertEquals(2, confidentialDocuments.size());

        assertThat(
            confidentialDocuments.stream()
                .map(doc -> doc.getValue().getConfidentialDocumentsReceived())
                .collect(Collectors.toList()),
            containsInAnyOrder(
                ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1,
                ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2)
        );

        assertEquals(DocumentType.APPLICATION,
            nonConfidentialDocuments.get(0).getValue().getDocumentType());
    }

    @Test
    public void processDocumentsShouldNotMoveConfidentialDocumentsToDocumentsGeneratedWhenContactIsNotPrivateForApplicant2() {

        CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder().contactDetailsType(ContactDetailsType.PUBLIC).build())
            .documents(CaseDocuments.builder()
                .confidentialDocumentsGenerated(Lists.newArrayList(applicant1NopConfidential, applicant2NopConfidential))
                .documentsGenerated(Lists.newArrayList(applicationDoc))
                .build())
            .build();

        documentsService.processDocuments(caseData, TEST_CASE_ID);

        List<ListValue<DivorceDocument>> nonConfidentialDocuments = caseData.getDocuments().getDocumentsGenerated();
        List<ListValue<ConfidentialDivorceDocument>> confidentialDocuments = caseData.getDocuments().getConfidentialDocumentsGenerated();

        assertEquals(1, nonConfidentialDocuments.size());
        assertEquals(2, confidentialDocuments.size());

        assertThat(
            confidentialDocuments.stream()
                .map(doc -> doc.getValue().getConfidentialDocumentsReceived())
                .collect(Collectors.toList()),
            containsInAnyOrder(
                ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1,
                ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2)
        );

        assertEquals(DocumentType.APPLICATION,
            nonConfidentialDocuments.get(0).getValue().getDocumentType());
    }

    @Test
    public void processDocumentShouldMoveBothApplicantsGeneralLettersToConfidentialDocumentsWhenContactIsPrivateForBothApplicant() {

        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().contactDetailsType(ContactDetailsType.PRIVATE).build())
            .applicant2(Applicant.builder().contactDetailsType(ContactDetailsType.PRIVATE).build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(Lists.newArrayList(
                    applicant1Nop, applicant2Nop, applicant1GeneralLetter, applicant2GeneralLetter, applicationDoc, bailiffRefusedDoc))
                .build())
            .generalLetters(Lists.newArrayList(applicant1GeneralLetterDetail, applicant2GeneralLetterDetail))
            .build();

        documentsService.processDocuments(caseData, TEST_CASE_ID);

        List<ListValue<DivorceDocument>> nonConfidentialDocuments = caseData.getDocuments().getDocumentsGenerated();
        List<ListValue<ConfidentialDivorceDocument>> confidentialDocuments = caseData.getDocuments().getConfidentialDocumentsGenerated();

        assertEquals(2, nonConfidentialDocuments.size());
        assertEquals(4, confidentialDocuments.size());

        assertThat(nonConfidentialDocuments.stream()
            .map(doc -> doc.getValue().getDocumentType())
            .collect(Collectors.toList()),
            containsInAnyOrder(BAILIFF_SERVICE_REFUSED, APPLICATION));

        assertThat(
            confidentialDocuments.stream()
                .map(doc -> doc.getValue().getConfidentialDocumentsReceived())
                .collect(Collectors.toList()),
            containsInAnyOrder(
                ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1,
                ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2,
                ConfidentialDocumentsReceived.GENERAL_LETTER,
                ConfidentialDocumentsReceived.GENERAL_LETTER)
        );

        assertThat(
            confidentialDocuments.stream()
                .filter(doc -> doc.getValue().getConfidentialDocumentsReceived().equals(ConfidentialDocumentsReceived.GENERAL_LETTER))
                .map(doc -> doc.getValue().getDocumentLink().getUrl())
                .collect(Collectors.toList()),
            containsInAnyOrder(
                "generalLetter1Url",
                "generalLetter2Url")
        );
    }

    @Test
    public void processDocumentShouldMoveFOCanApplyToConfidentialDocumentsWhenContactIsPrivateForEitherApplicant() {
        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().contactDetailsType(ContactDetailsType.PRIVATE).build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(Lists.newArrayList(
                    foCanApply))
                .build())
            .generalLetters(Lists.newArrayList(applicant1GeneralLetterDetail, applicant2GeneralLetterDetail))
            .build();

        documentsService.processDocuments(caseData, TEST_CASE_ID);

        List<ListValue<ConfidentialDivorceDocument>> confidentialDocuments = caseData.getDocuments().getConfidentialDocumentsGenerated();

        assertEquals(1, confidentialDocuments.size());

        assertEquals(ConfidentialDocumentsReceived.FINAL_ORDER_CAN_APPLY, confidentialDocuments.get(0).getValue()
            .getConfidentialDocumentsReceived());
    }

    @Test
    public void processDocumentsShouldMoveOfflineRespondentNopToConfidentialDocumentsTab() {
        CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder().contactDetailsType(ContactDetailsType.PRIVATE).build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(Lists.newArrayList(
                    applicant2Nop, applicant2GeneralLetter))
                .build())
            .generalLetters(Lists.newArrayList(applicant2GeneralLetterDetail))
            .build();

        documentsService.processDocuments(caseData, TEST_CASE_ID);

        List<ListValue<ConfidentialDivorceDocument>> confidentialDocuments = caseData.getDocuments().getConfidentialDocumentsGenerated();

        assertEquals(2, confidentialDocuments.size());

        assertThat(
            confidentialDocuments.stream()
                .map(doc -> doc.getValue().getConfidentialDocumentsReceived())
                .collect(Collectors.toList()),
            containsInAnyOrder(
                ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2,
                ConfidentialDocumentsReceived.GENERAL_LETTER)
        );
    }

    private DivorceDocument buildDivorceDocument(final DocumentType documentType) {
        return DivorceDocument.builder()
            .documentType(documentType)
            .documentLink(Document.builder().build())
            .build();
    }

    private ConfidentialDivorceDocument buildConfidentialDivorceDocument(final ConfidentialDocumentsReceived documentType) {
        return ConfidentialDivorceDocument.builder()
            .confidentialDocumentsReceived(documentType)
            .documentLink(Document.builder().build())
            .build();
    }
}
