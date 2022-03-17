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
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.time.LocalDate;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class NoticeOfProceedingsPrinterTest {

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
                .documentType(NOTICE_OF_PROCEEDINGS)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().build())
            .applicant2(Applicant.builder().build())
            .documentsGenerated(singletonList(applicant1NopDocument))
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        noticeOfProceedingsPrinter.sendLetterToApplicant1(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("applicant1-notice-of-proceedings");
        assertThat(print.getLetters().size()).isEqualTo(1);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(applicant1NopDocument.getValue());
    }

    @Test
    void shouldPrintCorrectNoticeOfProceedingDocumentsIfOneExistsForEachApplicant() {
        final ListValue<DivorceDocument> applicant1NopDocument = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NOTICE_OF_PROCEEDINGS)
                .documentDateAdded(LocalDate.now())
                .build())
            .build();

        final ListValue<DivorceDocument> applicant2NopDocument = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NOTICE_OF_PROCEEDINGS)
                .documentDateAdded(LocalDate.now())
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .offline(YES)
                .build()
            )
            .applicant2(Applicant.builder()
                .offline(YES)
                .build()
            )
            .documentsGenerated(asList(applicant2NopDocument, applicant1NopDocument))
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        noticeOfProceedingsPrinter.sendLetterToApplicant1(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("applicant1-notice-of-proceedings");
        assertThat(print.getLetters().size()).isEqualTo(1);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(applicant1NopDocument.getValue());
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
                .documentType(NOTICE_OF_PROCEEDINGS)
                .build())
            .build();

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().build())
            .applicant2(Applicant.builder().build())
            .documentsGenerated(singletonList(applicant2NopDocument))
            .build();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        noticeOfProceedingsPrinter.sendLetterToApplicant2(caseData, TEST_CASE_ID);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("applicant2-notice-of-proceedings");
        assertThat(print.getLetters().size()).isEqualTo(1);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(applicant2NopDocument.getValue());
    }

    @Test
    void shouldNotPrintApplicant2NoticeOfProceedingIfNotPresent() {

        final CaseData caseData = CaseData.builder().build();

        noticeOfProceedingsPrinter.sendLetterToApplicant2(caseData, TEST_CASE_ID);

        verifyNoInteractions(bulkPrintService);
    }
}
