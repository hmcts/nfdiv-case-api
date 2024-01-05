package uk.gov.hmcts.divorce.caseworker.service.task;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.AosResponseDocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_RESPONSE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE_DISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_ANSWERS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_ANSWERS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SendAosResponseLetterPackToApplicantTest {

    public static final String AOS_LETTER_ID = "aos-letter-id";

    private static final DocumentPackInfo DISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(NFD_NOP_APP1_JS_SOLE_DISPUTED),
            DocumentType.RESPONDENT_ANSWERS, Optional.of(RESPONDENT_ANSWERS_TEMPLATE_ID)
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            NFD_NOP_APP1_JS_SOLE_DISPUTED, AOS_RESPONSE_LETTER_DOCUMENT_NAME,
            RESPONDENT_ANSWERS_TEMPLATE_ID, RESPONDENT_ANSWERS_DOCUMENT_NAME
        )
    );

    @Mock
    private LetterPrinter letterPrinter;

    @Mock
    private AosResponseDocumentPack aosResponseDocumentPack;

    @InjectMocks
    private SendAosResponseLetterPackToApplicant sendAosResponseLetterPackToApplicant;

    @Test
    void shouldNotSendAosResponsePackToApplicantIfApplicantIsNotOffline() {
        final var caseData = caseData();
        caseData.getApplicant1().setOffline(NO);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        sendAosResponseLetterPackToApplicant.apply(caseDetails);

        verifyNoInteractions(letterPrinter);
    }

    @Test
    void shouldNotSendAosResponsePackToApplicantIfApplicantIsOfflineAndAosIsUndisputed() {
        final var caseData = caseData();
        caseData.getApplicant1().setOffline(NO);
        caseData.getAcknowledgementOfService().setHowToRespondApplication(WITHOUT_DISPUTE_DIVORCE);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        sendAosResponseLetterPackToApplicant.apply(caseDetails);

        verifyNoInteractions(letterPrinter);
    }

    @Test
    void shouldSendAosResponsePackToApplicantIfApplicantIsOfflineAndAosIsDisputed() {
        final var caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.getAcknowledgementOfService().setHowToRespondApplication(DISPUTE_DIVORCE);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        when(aosResponseDocumentPack.getDocumentPack(caseData, caseData.getApplicant1())).thenReturn(DISPUTED_AOS_RESPONSE_PACK);
        when(aosResponseDocumentPack.getLetterId()).thenReturn(AOS_LETTER_ID);

        sendAosResponseLetterPackToApplicant.apply(caseDetails);

        verify(letterPrinter).sendLetters(caseData, TEST_CASE_ID, caseData.getApplicant1(), DISPUTED_AOS_RESPONSE_PACK, AOS_LETTER_ID);

        verifyNoMoreInteractions(letterPrinter);
    }
}
