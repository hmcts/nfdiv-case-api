package uk.gov.hmcts.divorce.common.notification;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.CertificateOfEntitlementDocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class RegenerateCourtOrdersNotificationTest {

    @Mock
    LetterPrinter letterPrinter;

    @Mock
    private CertificateOfEntitlementDocumentPack certificateOfEntitlementDocumentPack;

    @InjectMocks
    private RegenerateCourtOrdersNotification notification;

    @Test
    void shouldPrintRegeneratedCourtOrdersIfApplicant1Offline() {
        final CaseData caseData = new CaseData();
        var documentPackInfo = new DocumentPackInfo(
                ImmutableMap.of(
                    CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1, Optional.of(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID),
                    CERTIFICATE_OF_ENTITLEMENT, Optional.empty()
                ),
                ImmutableMap.of(
                    CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID, CERTIFICATE_OF_ENTITLEMENT_NAME)
        );

        when(certificateOfEntitlementDocumentPack.getDocumentPack(caseData, caseData.getApplicant1())).thenReturn(documentPackInfo);

        notification.sendToApplicant1Offline(caseData, TEST_CASE_ID);
        verify(letterPrinter).sendLetters(caseData, TEST_CASE_ID, caseData.getApplicant1(),
                documentPackInfo,
                null);
    }

    @Test
    void shouldPrintRegeneratedCourtOrdersIfApplicant2Offline() {
        final CaseData caseData = new CaseData();
        var documentPackInfo = new DocumentPackInfo(
                ImmutableMap.of(
                    CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
                    Optional.of(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID),
                    CERTIFICATE_OF_ENTITLEMENT, Optional.empty()
                ),
                ImmutableMap.of(
                    CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID, CERTIFICATE_OF_ENTITLEMENT_NAME)
        );

        when(certificateOfEntitlementDocumentPack.getDocumentPack(caseData, caseData.getApplicant2())).thenReturn(documentPackInfo);

        notification.sendToApplicant1Offline(caseData, TEST_CASE_ID);
        verify(letterPrinter).sendLetters(caseData, TEST_CASE_ID, caseData.getApplicant2(),
                documentPackInfo,
                null);
    }
}
