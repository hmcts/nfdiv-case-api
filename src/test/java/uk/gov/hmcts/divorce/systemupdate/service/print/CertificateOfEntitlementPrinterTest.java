package uk.gov.hmcts.divorce.systemupdate.service.print;

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
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class CertificateOfEntitlementPrinterTest {

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private GenerateCertificateOfEntitlement generateCertificateOfEntitlement;

    @InjectMocks
    private CertificateOfEntitlementPrinter certificateOfEntitlementPrinter;

    @Captor
    private ArgumentCaptor<Print> printCaptor;

    private static final DivorceDocument certificateOfEntitlementDocValue =
        DivorceDocument.builder()
            .documentType(CERTIFICATE_OF_ENTITLEMENT)
            .build();

    private static final DivorceDocument certificateOfEntitlementCoverLetterValue =
        DivorceDocument.builder()
            .documentType(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1)
            .build();

    @Test
    void shouldPrintCertificateOfEntitlementLetterIfRequiredDocumentsArePresent() {

        final CaseData caseData = caseData();

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        certificateOfEntitlementPrinter.sendLetter(caseData, TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1, caseData.getApplicant1());

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("certificate-of-entitlement");
        assertThat(print.getLetters().size()).isEqualTo(2);
        assertThat(print.getLetters().get(0).getDivorceDocument()).isSameAs(certificateOfEntitlementCoverLetterValue);
        assertThat(print.getLetters().get(1).getDivorceDocument()).isSameAs(certificateOfEntitlementDocValue);

        final List recipient = List.of(TEST_CASE_ID, caseData.getApplicant1(), "certificate-of-entitlement");
        assertThat(print.getRecipients().equals(recipient));
    }

    @Test
    void shouldNotPrintCertificateOfEntitlementLetterIfRequiredDocumentsAreNotPresent() {

        final CaseData caseData = caseData();
        caseData.getDocuments().setDocumentsGenerated(new ArrayList<>());

        certificateOfEntitlementPrinter.sendLetter(caseData, TEST_CASE_ID, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1, caseData.getApplicant1());

        verifyNoInteractions(bulkPrintService);
    }

    private CaseData caseData() {
        final ListValue<DivorceDocument> certificateOfEntitlementDoc = ListValue.<DivorceDocument>builder()
            .value(certificateOfEntitlementDocValue)
            .build();

        final ListValue<DivorceDocument> certificateOfEntitlementCoverLetter = ListValue.<DivorceDocument>builder()
            .value(certificateOfEntitlementCoverLetterValue)
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
                    .documentsGenerated(singletonList(certificateOfEntitlementCoverLetter))
                    .build()
            )
            .conditionalOrder(
                ConditionalOrder.builder()
                    .dateAndTimeOfHearing(LocalDateTime.of(2022, 4, 28, 10, 0, 0))
                    .court(BURY_ST_EDMUNDS)
                    .certificateOfEntitlementDocument(certificateOfEntitlementDocValue)
                    .build()
            )
            .build();
    }

    private CtscContactDetails buildCtscContactDetails() {
        return CtscContactDetails
            .builder()
            .emailAddress("contactdivorce@justice.gov.uk")
            .phoneNumber("0300 303 0642")
            .build();
    }
}
