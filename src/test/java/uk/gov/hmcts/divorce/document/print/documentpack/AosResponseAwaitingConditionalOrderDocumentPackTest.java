package uk.gov.hmcts.divorce.document.print.documentpack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication;
import uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.document.print.documentpack.AosResponseAwaitingConditionalOrderDocumentPack.DISPUTED_AOS_AWAITING_CO_RESPONSE_PACK;
import static uk.gov.hmcts.divorce.document.print.documentpack.AosResponseAwaitingConditionalOrderDocumentPack.UNDISPUTED_AOS_AWAITING_CO_RESPONSE_PACK;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class AosResponseAwaitingConditionalOrderDocumentPackTest {

    @InjectMocks
    private AosResponseAwaitingConditionalOrderDocumentPack aosResponseAwaitingConditionalOrderDocumentPack;


    @Test
    void shouldReturnCorrectPackWhenDisputedDivorce() {
        CaseData data = validApplicant1CaseData();
        data.getAcknowledgementOfService().setHowToRespondApplication(HowToRespondApplication.DISPUTE_DIVORCE);
        data.setSupplementaryCaseType(SupplementaryCaseType.NA);
        data.getApplicant2().setOffline(YesOrNo.NO);

        var documentPack = aosResponseAwaitingConditionalOrderDocumentPack.getDocumentPack(data, data.getApplicant1());
        assertThat(documentPack).isEqualTo(DISPUTED_AOS_AWAITING_CO_RESPONSE_PACK);
    }

    @Test
    void shouldReturnCorrectPackWhenUnDisputedDivorce() {
        CaseData data = validApplicant1CaseData();
        data.getAcknowledgementOfService().setHowToRespondApplication(HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE);
        data.setSupplementaryCaseType(SupplementaryCaseType.NA);
        data.getApplicant2().setOffline(YesOrNo.YES);

        var documentPack = aosResponseAwaitingConditionalOrderDocumentPack.getDocumentPack(data, data.getApplicant1());
        assertThat(documentPack).isEqualTo(UNDISPUTED_AOS_AWAITING_CO_RESPONSE_PACK);
    }
}
