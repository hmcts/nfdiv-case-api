package uk.gov.hmcts.divorce.caseworker.service.print;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class NoticeOfProceedingsPrinterTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private BulkPrintService bulkPrintService;

    @InjectMocks
    private NoticeOfProceedingsPrinter noticeOfProceedingsPrinter;

    @Captor
    ArgumentCaptor<Print> printCaptor;

    @Test
    void shouldPrintApplicant1NoticeOfProceedingIfDocumentsArePresent() {
        final ListValue<DivorceDocument> applicant1NopDocument = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NOTICE_OF_PROCEEDINGS_APP_1)
                .build())
            .build();

        final ListValue<DivorceDocument> applicant1ApplicationDocument = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().build())
            .applicant2(Applicant.builder().build())
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(asList(applicant1NopDocument, applicant1ApplicationDocument))
                    .build()
            )
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        noticeOfProceedingsPrinter.sendLetterToApplicant1(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("applicant1-notice-of-proceedings");
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(applicant1NopDocument.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(applicant1ApplicationDocument.getValue());
    }

    @Test
    void shouldNotPrintApplicant1NoticeOfProceedingIfNotPresent() {

        final CaseData caseData = CaseData.builder().build();

        noticeOfProceedingsPrinter.sendLetterToApplicant1(caseData, TEST_CASE_ID);

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldPrintApplicant2NoticeOfProceedingIfDocumentsArePresent() {
        final ListValue<DivorceDocument> applicant2NopDocument = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NOTICE_OF_PROCEEDINGS_APP_2)
                .build())
            .build();

        final ListValue<DivorceDocument> applicant2ApplicationDocument = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().build())
            .applicant2(Applicant.builder().build())
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(asList(applicant2NopDocument, applicant2ApplicationDocument))
                    .build()
            )
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        noticeOfProceedingsPrinter.sendLetterToApplicant2(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("applicant2-notice-of-proceedings");
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(applicant2NopDocument.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(applicant2ApplicationDocument.getValue());
    }

    @Test
    void shouldNotPrintApplicant2NoticeOfProceedingIfNotPresent() {

        final CaseData caseData = CaseData.builder().build();

        noticeOfProceedingsPrinter.sendLetterToApplicant2(caseData, TEST_CASE_ID);

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldPrintApplicant2SolicitorNoticeOfProceedingWithD10IfDocumentsArePresent() {
        final ListValue<DivorceDocument> applicant2NopDocument = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NOTICE_OF_PROCEEDINGS_APP_2)
                .build())
            .build();

        final ListValue<DivorceDocument> applicant2ApplicationDocument = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> coversheetDocument = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(COVERSHEET)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().build())
            .applicant2(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .solicitorRepresented(YES)
                .solicitor(
                    Solicitor.builder()
                        .name("app 2 sol")
                        .address("The avenue")
                        .build()
                )
                .build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(asList(applicant2NopDocument, applicant2ApplicationDocument, coversheetDocument))
                .build()
            )
            .build();

        when(bulkPrintService.printAosRespondentPack(printCaptor.capture(), eq(true))).thenReturn(UUID.randomUUID());

        noticeOfProceedingsPrinter.sendLetterToApplicant2Solicitor(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("applicant2-solicitor-notice-of-proceedings-with-d10");
        assertThat(print.getLetters().size()).isEqualTo(3);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(coversheetDocument.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(applicant2NopDocument.getValue());
        assertThat(print.getLetters().get(2).getDivorceDocument()).isSameAs(applicant2ApplicationDocument.getValue());
    }

    @Test
    void shouldNotPrintApplicant2SolicitorNoticeOfProceedingWithD10IfDocumentsAreNotPresent() {
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().build())
            .applicant2(
                Applicant.builder()
                    .solicitor(Solicitor.builder().build())
                    .build()
            ).build();

        noticeOfProceedingsPrinter.sendLetterToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldPrintApplicant2SolicitorNoticeOfProceedingWithoutD10IfDocumentsArePresent() {
        final ListValue<DivorceDocument> applicant2NopDocument = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NOTICE_OF_PROCEEDINGS_APP_2)
                .build())
            .build();

        final ListValue<DivorceDocument> applicant2ApplicationDocument = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().build())
            .applicant2(Applicant.builder()
                .solicitorRepresented(YES)
                .solicitor(
                    Solicitor.builder()
                        .name("app 2 sol")
                        .address("The avenue")
                        .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                            .build())
                        .build()
                )
                .build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(asList(applicant2NopDocument, applicant2ApplicationDocument))
                .build()
            )
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        noticeOfProceedingsPrinter.sendLetterToApplicant2Solicitor(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("applicant2-solicitor-notice-of-proceedings");
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(applicant2NopDocument.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(applicant2ApplicationDocument.getValue());
    }

    @Test
    void shouldNotPrintApplicant2SolicitorNoticeOfProceedingIfNotPresent() {
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().build())
            .applicant2(Applicant.builder()
                .solicitor(
                    Solicitor.builder()
                        .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                            .build())
                        .build()
                )
                .build())
            .build();

        noticeOfProceedingsPrinter.sendLetterToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldPrintApplicant1NoticeOfProceedingWhenApplicantContactIsPrivateAndConfidentialDocumentsArePresent() {
        final ListValue<ConfidentialDivorceDocument> applicant1NopDocument = ListValue.<ConfidentialDivorceDocument>builder()
            .value(ConfidentialDivorceDocument.builder()
                .confidentialDocumentsReceived(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1)
                .build())
            .build();

        final ListValue<DivorceDocument> applicant1ApplicationDocument = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().contactDetailsType(ContactDetailsType.PRIVATE).build())
            .applicant2(Applicant.builder().contactDetailsType(ContactDetailsType.PRIVATE).build())
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(List.of(applicant1ApplicationDocument))
                    .confidentialDocumentsGenerated(List.of(applicant1NopDocument))
                    .build()
            )
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        noticeOfProceedingsPrinter.sendLetterToApplicant1(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("applicant1-notice-of-proceedings");
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getConfidentialDivorceDocument()).isSameAs(applicant1NopDocument.getValue());
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(applicant1ApplicationDocument.getValue());
    }
}
