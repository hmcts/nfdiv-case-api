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
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_ANSWERS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class AosPackPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @InjectMocks
    private AosPackPrinter aosPackPrinter;

    @Captor
    ArgumentCaptor<Print> printCaptor;

    @Test
    void shouldPrintAosPackWithD10ForRespondentIfRequiredDocumentsArePresent() {

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NOTICE_OF_PROCEEDINGS_APP_2)
                .build())
            .build();

        final ListValue<DivorceDocument> doc3 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant2(
                Applicant.builder()
                    .email("testresp@test.com")
                    .solicitorRepresented(YES)
                    .solicitor(Solicitor.builder().build())
                    .build())
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc2, doc3)).build())
            .build();

        when(bulkPrintService.printAosRespondentPack(printCaptor.capture(), eq(true))).thenReturn(randomUUID());

        aosPackPrinter.sendAosLetterToRespondent(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("respondent-aos-pack");
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(doc2.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(doc3.getValue());
    }

    @Test
    void shouldPrintAosPackWithD10ForRespondentIfEmailIsBlank() {

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NOTICE_OF_PROCEEDINGS_APP_2)
                .build())
            .build();

        final ListValue<DivorceDocument> doc3 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant2(
                Applicant.builder()
                    .email("")
                    .solicitorRepresented(NO)
                    .solicitor(Solicitor.builder().build())
                    .build())
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc2, doc3)).build())
            .build();

        when(bulkPrintService.printAosRespondentPack(printCaptor.capture(), eq(true))).thenReturn(randomUUID());

        aosPackPrinter.sendAosLetterToRespondent(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("respondent-aos-pack");
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(doc2.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(doc3.getValue());
    }

    @Test
    void shouldPrintAosPackWithoutD10ForRespondentIfEmailIsPresent() {

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NOTICE_OF_PROCEEDINGS_APP_2)
                .build())
            .build();

        final ListValue<DivorceDocument> doc3 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant2(
                Applicant.builder()
                    .email("testresp@test.com")
                    .solicitorRepresented(NO)
                    .solicitor(Solicitor.builder().build())
                    .build())
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc2, doc3)).build())
            .build();

        when(bulkPrintService.printAosRespondentPack(printCaptor.capture(), eq(false))).thenReturn(randomUUID());

        aosPackPrinter.sendAosLetterToRespondent(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("respondent-aos-pack");
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(doc2.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(doc3.getValue());
    }

    @Test
    void shouldPrintAosPackWithoutD10ForRespondentIfAddressIsOverseas() {

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NOTICE_OF_PROCEEDINGS_APP_2)
                .build())
            .build();

        final ListValue<DivorceDocument> doc3 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant2(
                Applicant.builder()
                    .email("testresp@test.com")
                    .addressOverseas(YES)
                    .solicitorRepresented(NO)
                    .solicitor(Solicitor.builder().build())
                    .build())
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc2, doc3)).build())
            .build();

        when(bulkPrintService.printAosRespondentPack(printCaptor.capture(), eq(true))).thenReturn(randomUUID());

        aosPackPrinter.sendAosLetterToRespondent(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("respondent-aos-pack");
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(doc2.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(doc3.getValue());
    }

    @Test
    void shouldPrintAosPackWithConfidentialNopDocumentForApplicant2IfContactIsPrivate() {

        final ListValue<ConfidentialDivorceDocument> doc1 = ListValue.<ConfidentialDivorceDocument>builder()
            .value(ConfidentialDivorceDocument.builder()
                .confidentialDocumentsReceived(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant2(
                Applicant.builder()
                    .email("testresp@test.com")
                    .solicitorRepresented(NO)
                    .solicitor(Solicitor.builder().build())
                    .contactDetailsType(ContactDetailsType.PRIVATE)
                    .build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(List.of(doc2))
                .confidentialDocumentsGenerated(List.of(doc1))
                .build())
            .build();

        when(bulkPrintService.printAosRespondentPack(printCaptor.capture(), eq(false))).thenReturn(randomUUID());

        aosPackPrinter.sendAosLetterToRespondent(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("respondent-aos-pack");
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getConfidentialDivorceDocument()).isSameAs(doc1.getValue());
        assertThat(print.getLetters().get(0).getDivorceDocument()).isNull();
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(doc2.getValue());
        assertThat(print.getLetters().get(1).getConfidentialDivorceDocument()).isNull();
    }

    @Test
    void shouldPrintAosPackForApplicantIfRequiredDocumentsArePresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NOTICE_OF_PROCEEDINGS_APP_1)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();


        final ListValue<DivorceDocument> doc3 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NAME_CHANGE_EVIDENCE)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc1, doc2, doc3)).build())
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(randomUUID());

        aosPackPrinter.sendAosLetterToApplicant(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("applicant-aos-pack");
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(doc1.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(doc2.getValue());
    }

    @Test
    void shouldPrintPersonalServiceAosPackForApplicantIfRequiredDocumentsArePresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NOTICE_OF_PROCEEDINGS_APP_1)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> doc3 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(COVERSHEET)
                .build())
            .build();

        final ListValue<DivorceDocument> doc4 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NOTICE_OF_PROCEEDINGS_APP_2)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc1, doc2, doc3, doc4)).build())
            .build();

        when(bulkPrintService.printWithD10Form(printCaptor.capture())).thenReturn(randomUUID());

        aosPackPrinter.sendAosLetterAndRespondentAosPackToApplicant(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("applicant-aos-pack");
        assertThat(print.getLetters().size()).isEqualTo(5);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(doc1.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(doc2.getValue());
        assertThat(print.getLetters().get(2).getDivorceDocument()).isSameAs(doc3.getValue());
        assertThat(print.getLetters().get(3).getDivorceDocument()).isSameAs(doc4.getValue());
        assertThat(print.getLetters().get(4).getDivorceDocument()).isSameAs(doc2.getValue());
    }

    @Test
    void shouldPrintPersonalServiceAosPackWithConfidentialNopDocsForApplicantIfRequiredDocumentsArePresent() {

        final ListValue<ConfidentialDivorceDocument> nopAppOne = ListValue.<ConfidentialDivorceDocument>builder()
            .value(ConfidentialDivorceDocument.builder()
                .confidentialDocumentsReceived(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1)
                .build())
            .build();

        final ListValue<DivorceDocument> application = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final ListValue<ConfidentialDivorceDocument> coversheet = ListValue.<ConfidentialDivorceDocument>builder()
            .value(ConfidentialDivorceDocument.builder()
                .confidentialDocumentsReceived(ConfidentialDocumentsReceived.COVERSHEET)
                .build())
            .build();

        final ListValue<ConfidentialDivorceDocument> nopAppTwo = ListValue.<ConfidentialDivorceDocument>builder()
            .value(ConfidentialDivorceDocument.builder()
                .confidentialDocumentsReceived(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .contactDetailsType(ContactDetailsType.PRIVATE)
                .build())
            .applicant2(Applicant.builder()
                .contactDetailsType(ContactDetailsType.PRIVATE)
                .build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(singletonList(application))
                .confidentialDocumentsGenerated(List.of(nopAppOne, coversheet, nopAppTwo))
                .build())
            .build();

        when(bulkPrintService.printWithD10Form(printCaptor.capture())).thenReturn(randomUUID());

        aosPackPrinter.sendAosLetterAndRespondentAosPackToApplicant(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("applicant-aos-pack");
        assertThat(print.getLetters().get(0).getConfidentialDivorceDocument()).isSameAs(nopAppOne.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(application.getValue());
        assertThat(print.getLetters().get(2).getConfidentialDivorceDocument()).isSameAs(coversheet.getValue());
        assertThat(print.getLetters().get(3).getConfidentialDivorceDocument()).isSameAs(nopAppTwo.getValue());
        assertThat(print.getLetters().get(4).getDivorceDocument()).isSameAs(application.getValue());
        assertThat(print.getLetters().size()).isEqualTo(5);
    }

    @Test
    void shouldPrintPersonalServiceAosPackWithApplicant2ConfidentialNopForApplicantIfRequiredDocumentsArePresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NOTICE_OF_PROCEEDINGS_APP_1)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> doc3 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(COVERSHEET)
                .build())
            .build();

        final ListValue<DivorceDocument> doc4 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NOTICE_OF_PROCEEDINGS_APP_2)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc1, doc2, doc3, doc4)).build())
            .build();

        when(bulkPrintService.printWithD10Form(printCaptor.capture())).thenReturn(randomUUID());

        aosPackPrinter.sendAosLetterAndRespondentAosPackToApplicant(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("applicant-aos-pack");
        assertThat(print.getLetters().size()).isEqualTo(5);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(doc1.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(doc2.getValue());
        assertThat(print.getLetters().get(2).getDivorceDocument()).isSameAs(doc3.getValue());
        assertThat(print.getLetters().get(3).getDivorceDocument()).isSameAs(doc4.getValue());
        assertThat(print.getLetters().get(4).getDivorceDocument()).isSameAs(doc2.getValue());
    }

    @Test
    void shouldNotPrintAosPackIfRequiredDocumentsAreNotPresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(RESPONDENT_ANSWERS)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NAME_CHANGE_EVIDENCE)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc1, doc2)).build())
            .build();

        aosPackPrinter.sendAosLetterToRespondent(caseData, TEST_CASE_ID);

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotPrintPersonalServiceAosPackIfRequiredDocumentsAreNotPresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(RESPONDENT_ANSWERS)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NAME_CHANGE_EVIDENCE)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc1, doc2)).build())
            .build();

        aosPackPrinter.sendAosLetterAndRespondentAosPackToApplicant(caseData, TEST_CASE_ID);

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldNotPrintAosPackForApplicantIfRequiredDocumentsAreNotPresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(RESPONDENT_ANSWERS)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NAME_CHANGE_EVIDENCE)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().documentsGenerated(asList(doc1, doc2)).build())
            .build();

        aosPackPrinter.sendAosLetterToApplicant(caseData, TEST_CASE_ID);

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldPrintAosPackForApplicantIfRequiredDocumentsArePresentAndApplicantContactIsPrivate() {

        final ListValue<ConfidentialDivorceDocument> doc1 = ListValue.<ConfidentialDivorceDocument>builder()
            .value(ConfidentialDivorceDocument.builder()
                .confidentialDocumentsReceived(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> doc3 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NAME_CHANGE_EVIDENCE)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().contactDetailsType(ContactDetailsType.PRIVATE).build())
            .documents(CaseDocuments.builder()
                .confidentialDocumentsGenerated(List.of(doc1))
                .documentsGenerated(asList(doc2, doc3))
                .build())
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(randomUUID());

        aosPackPrinter.sendAosLetterToApplicant(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("applicant-aos-pack");
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getConfidentialDivorceDocument()).isSameAs(doc1.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(doc2.getValue());
    }
}
