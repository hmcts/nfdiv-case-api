package uk.gov.hmcts.divorce.systemupdate.print;

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
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.systemupdate.service.print.ConditionalOrderPronouncedPrinter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.systemupdate.service.print.ConditionalOrderPronouncedPrinter.LETTER_TYPE_CO_PRONOUNCED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class ConditionalOrderPronouncedPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @InjectMocks
    private ConditionalOrderPronouncedPrinter conditionalOrderPronouncedPrinter;

    @Captor
    ArgumentCaptor<Print> printCaptor;

    private static final DivorceDocument coGrantedDocValue =
        DivorceDocument.builder()
            .documentType(CONDITIONAL_ORDER_GRANTED)
            .build();

    private static final DivorceDocument coGrantedCoversheetValue =
        DivorceDocument.builder()
            .documentType(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1)
            .build();

    @Test
    void shouldGenerateDivorceCoGrantedCoversheetAddressedToApplicant1IfNotRepresentedAndPrintDocs() {

        CaseData caseData = caseData();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        conditionalOrderPronouncedPrinter.sendLetter(caseData, TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo(LETTER_TYPE_CO_PRONOUNCED);
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(coGrantedCoversheetValue);
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(coGrantedDocValue);
    }

    @Test
    void shouldGenerateDivorceCoGrantedCoversheetAddressedToApplicant2IfNotRepresentedAndPrintDocs() {

        CaseData caseData = caseData();
        caseData.getDocuments().setDocumentsGenerated(new ArrayList<>());

        conditionalOrderPronouncedPrinter.sendLetter(caseData, TEST_CASE_ID, CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2);

        verifyNoInteractions(bulkPrintService);
    }

    private CaseData caseData() {
        final ListValue<DivorceDocument> coGrantedDoc = ListValue.<DivorceDocument>builder()
            .value(coGrantedDocValue)
            .build();

        final ListValue<DivorceDocument> coGrantedCoversheet = ListValue.<DivorceDocument>builder()
            .value(coGrantedCoversheetValue)
            .build();

        return CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .applicant1(
                Applicant.builder()
                    .firstName("Bob")
                    .lastName("Smith")
                    .offline(YES)
                    .address(APPLICANT_ADDRESS)
                    .solicitorRepresented(NO)
                    .solicitor(Solicitor.builder().build())
                    .languagePreferenceWelsh(NO)
                    .build())
            .applicant2(
                Applicant.builder()
                    .firstName("Julie")
                    .lastName("Smith")
                    .offline(YES)
                    .address(APPLICANT_ADDRESS)
                    .solicitorRepresented(NO)
                    .solicitor(Solicitor.builder().build())
                    .languagePreferenceWelsh(NO)
                    .build())
            .documents(
                CaseDocuments.builder()
                    .documentsGenerated(asList(coGrantedDoc, coGrantedCoversheet))
                    .build()
            )
            .conditionalOrder(ConditionalOrder.builder().grantedDate(LocalDate.of(2022, 4, 28)).build())
            .build();
    }
}
