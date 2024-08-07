package uk.gov.hmcts.divorce.document.print.documentpack;

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
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.document.print.documentpack.AosResponseDocumentPack.APP_2_OFFLINE_CITIZEN_JS_UNDISPUTED_AOS_RESPONSE_PACK;
import static uk.gov.hmcts.divorce.document.print.documentpack.AosResponseDocumentPack.CITIZEN_JS_DISPUTED_AOS_RESPONSE_PACK;
import static uk.gov.hmcts.divorce.document.print.documentpack.AosResponseDocumentPack.CITIZEN_JS_UNDISPUTED_AOS_RESPONSE_PACK;
import static uk.gov.hmcts.divorce.document.print.documentpack.AosResponseDocumentPack.DISPUTED_AOS_RESPONSE_PACK;
import static uk.gov.hmcts.divorce.document.print.documentpack.AosResponseDocumentPack.SOLICITOR_JS_UNDISPUTED_AOS_RESPONSE_PACK;
import static uk.gov.hmcts.divorce.document.print.documentpack.AosResponseDocumentPack.UNDISPUTED_AOS_RESPONSE_PACK;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class AosResponseDocumentPackTest {

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
        assertThat(documentPack).isEqualTo(UNDISPUTED_AOS_RESPONSE_PACK);
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
    public void shouldReturnCorrectPackWhenJSUnDisputedUnRepresentedApp2Offline() {
        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.NO);
        data.getApplicant1().setOffline(YesOrNo.YES);
        data.getAcknowledgementOfService().setHowToRespondApplication(HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE);
        data.setSupplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION);
        data.getApplicant2().setSolicitorRepresented(YesOrNo.NO);
        data.getApplicant2().setOffline(YesOrNo.YES);

        var documentPack = aosResponseDocumentPack.getDocumentPack(data, data.getApplicant2());
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
