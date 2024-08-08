package uk.gov.hmcts.divorce.common.notification;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.AosResponseDocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class ResendJudicialSeparationCitizenAosResponseNotificationTest {

    @Mock
    private LetterPrinter letterPrinter;

    @Mock
    private AosResponseDocumentPack aosResponseDocumentPack;

    @InjectMocks
    private ResendJudicialSeparationCitizenAosResponseNotification resendJudicialSeparationCitizenAosResponseNotification;

    @Test
    void shouldResendLetterToOfflineApplicant1() {
        final var caseId = TEST_CASE_ID;
        final var caseData = validJointApplicant1CaseData();
        caseData.getApplicant1().setOffline(YES);

        final var documentPack = new DocumentPackInfo(
                ImmutableMap.of(DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT)),
                ImmutableMap.of(COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME)
        );

        final String letterTypeAosResponsePack = "aos-response-pack";

        when(aosResponseDocumentPack.getDocumentPack(caseData, caseData.getApplicant1())).thenReturn(documentPack);
        when(aosResponseDocumentPack.getLetterId()).thenReturn(letterTypeAosResponsePack);

        resendJudicialSeparationCitizenAosResponseNotification.sendToApplicant1Offline(caseData, caseId);

        verify(letterPrinter).sendLetters(
                caseData,
                caseId,
                caseData.getApplicant1(),
                documentPack,
                letterTypeAosResponsePack
        );
    }
}
