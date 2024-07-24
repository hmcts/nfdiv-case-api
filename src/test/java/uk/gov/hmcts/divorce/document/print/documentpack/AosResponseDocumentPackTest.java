package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateD10Form;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication;
import uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_RESPONSE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT2_SOLICITOR;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE_DISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_SOL_JS_SOLE_UNDISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_RESPONDED_UNDEFENDED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.print.documentpack.AosResponseDocumentPack.APP_2_OFFLINE_CITIZEN_JS_UNDISPUTED_AOS_RESPONSE_PACK;
import static uk.gov.hmcts.divorce.document.print.documentpack.AosResponseDocumentPack.CITIZEN_JS_UNDISPUTED_AOS_RESPONSE_PACK;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class AosResponseDocumentPackTest {

    private static final DocumentPackInfo DISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID),
            DocumentType.RESPONDENT_ANSWERS, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID, AOS_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo CITIZEN_JS_DISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(NFD_NOP_APP1_JS_SOLE_DISPUTED),
            DocumentType.RESPONDENT_ANSWERS, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            NFD_NOP_APP1_JS_SOLE_DISPUTED, AOS_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo APP_2_OFFLINE_UNDISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(RESPONDENT_RESPONDED_UNDEFENDED_TEMPLATE_ID),
            DocumentType.RESPONDENT_ANSWERS, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            RESPONDENT_RESPONDED_UNDEFENDED_TEMPLATE_ID, AOS_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    private static final DocumentPackInfo SOLICITOR_JS_UNDISPUTED_AOS_RESPONSE_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT2_SOLICITOR),
            DocumentType.AOS_RESPONSE_LETTER, Optional.of(NFD_NOP_APP1_SOL_JS_SOLE_UNDISPUTED),
            DocumentType.RESPONDENT_ANSWERS, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT2_SOLICITOR, COVERSHEET_DOCUMENT_NAME,
            NFD_NOP_APP1_SOL_JS_SOLE_UNDISPUTED, AOS_RESPONSE_LETTER_DOCUMENT_NAME
        )
    );

    @Mock
    private GenerateD10Form generateD10Form;

    @Mock
    private GenerateD84Form generateD84Form;

    @InjectMocks
    private AosResponseDocumentPack aosResponseDocumentPack;


    @Test
    public void shouldReturnCorrectPackWhenDisputedDivorce() {
        CaseData data = validApplicant1CaseData();
        data.getAcknowledgementOfService().setHowToRespondApplication(HowToRespondApplication.DISPUTE_DIVORCE);
        data.setSupplementaryCaseType(SupplementaryCaseType.NA);
        data.getApplicant2().setOffline(YesOrNo.NO);

        var documentPack = aosResponseDocumentPack.getDocumentPack(data, data.getApplicant1());
        assertThat(documentPack).isEqualTo(DISPUTED_AOS_RESPONSE_PACK);
    }

    @Test
    public void shouldReturnCorrectPackWhenUnDisputedDivorce() {
        CaseData data = validApplicant1CaseData();
        data.getAcknowledgementOfService().setHowToRespondApplication(HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE);
        data.setSupplementaryCaseType(SupplementaryCaseType.NA);
        data.getApplicant2().setOffline(YesOrNo.YES);

        var documentPack = aosResponseDocumentPack.getDocumentPack(data, data.getApplicant1());
        assertThat(documentPack).isEqualTo(APP_2_OFFLINE_UNDISPUTED_AOS_RESPONSE_PACK);
    }

    @Test
    public void shouldReturnCorrectPackWhenJSDisputedDivorce() {
        CaseData data = validApplicant1CaseData();
        data.getAcknowledgementOfService().setHowToRespondApplication(HowToRespondApplication.DISPUTE_DIVORCE);
        data.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);
        data.getApplicant2().setOffline(YesOrNo.NO);

        var documentPack = aosResponseDocumentPack.getDocumentPack(data, data.getApplicant1());
        verify(generateD84Form).generateD84(data);
        assertThat(documentPack).isEqualTo(CITIZEN_JS_DISPUTED_AOS_RESPONSE_PACK);
    }

    @Test
    public void shouldReturnCorrectPackWhenJSUnDisputedApp1Represented() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.getAcknowledgementOfService().setHowToRespondApplication(HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE);
        data.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);
        data.getApplicant2().setOffline(YesOrNo.NO);

        var documentPack = aosResponseDocumentPack.getDocumentPack(data, data.getApplicant1());
        verify(generateD84Form).generateD84(data);
        verify(generateD10Form).apply(data);
        assertThat(documentPack).isEqualTo(SOLICITOR_JS_UNDISPUTED_AOS_RESPONSE_PACK);
    }

    @Test
    public void shouldReturnCorrectPackWhenJSUnDisputedUnRepresentedOffline() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.NO);
        data.getApplicant1().setOffline(YesOrNo.YES);
        data.getAcknowledgementOfService().setHowToRespondApplication(HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE);
        data.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);
        data.getApplicant2().setSolicitorRepresented(YesOrNo.NO);
        data.getApplicant2().setOffline(YesOrNo.YES);

        var documentPack = aosResponseDocumentPack.getDocumentPack(data, data.getApplicant1());
        verify(generateD84Form).generateD84(data);
        assertThat(documentPack).isEqualTo(APP_2_OFFLINE_CITIZEN_JS_UNDISPUTED_AOS_RESPONSE_PACK);
    }

    @Test
    public void shouldReturnCorrectPackWhenJSUnDisputedUnRepresentedApp1Offline() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.NO);
        data.getApplicant1().setOffline(YesOrNo.YES);
        data.getAcknowledgementOfService().setHowToRespondApplication(HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE);
        data.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);
        data.getApplicant2().setSolicitorRepresented(YesOrNo.NO);
        data.getApplicant2().setOffline(YesOrNo.NO);

        var documentPack = aosResponseDocumentPack.getDocumentPack(data, data.getApplicant1());
        assertThat(documentPack).isEqualTo(CITIZEN_JS_UNDISPUTED_AOS_RESPONSE_PACK);
    }

}
