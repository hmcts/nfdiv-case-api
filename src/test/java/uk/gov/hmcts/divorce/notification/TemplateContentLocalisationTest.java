package uk.gov.hmcts.divorce.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class TemplateContentLocalisationTest {

    private final Long caseRef = 7201000100010001L;
    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private TemplateContentLocalisation templateContentLocalisation;

    @Test
    void shouldGetPartnerEnglishContent() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setGender(FEMALE);
        assertThat(templateContentLocalisation.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).isEqualTo("wife");

        caseData = caseData();
        caseData.getApplicant2().setGender(Gender.MALE);
        assertThat(templateContentLocalisation.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).isEqualTo("husband");

        caseData = caseData();
        caseData.getApplicant2().setGender(null);
        assertThat(templateContentLocalisation.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).isEqualTo("spouse");

        caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(templateContentLocalisation.getPartner(caseData, caseData.getApplicant2(), ENGLISH)).isEqualTo("civil partner");
    }

    @Test
    void shouldGetPartnerWelshContent() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setGender(FEMALE);
        assertThat(templateContentLocalisation.getPartner(caseData, caseData.getApplicant2(), WELSH)).isEqualTo("gwraig");

        caseData = caseData();
        caseData.getApplicant2().setGender(Gender.MALE);
        assertThat(templateContentLocalisation.getPartner(caseData, caseData.getApplicant2(), WELSH)).isEqualTo("gŵr");

        caseData = caseData();
        caseData.getApplicant2().setGender(null);
        assertThat(templateContentLocalisation.getPartner(caseData, caseData.getApplicant2(), WELSH)).isEqualTo("priod");

        caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(templateContentLocalisation.getPartner(caseData, caseData.getApplicant2(), WELSH)).isEqualTo("partner sifil");
    }

    @Test
    void shouldGetUnionType() {
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        assertThat(templateContentLocalisation.getUnionType(caseData)).isEqualTo("divorce");

        caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(templateContentLocalisation.getUnionType(caseData)).isEqualTo("dissolution");
    }

    @Test
    void shouldGetEnglishUnionType() {
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        assertThat(templateContentLocalisation.getUnionType(caseData, ENGLISH)).isEqualTo("divorce");

        caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(templateContentLocalisation.getUnionType(caseData, ENGLISH)).isEqualTo("dissolution");
    }

    @Test
    void shouldGetWelshUnionType() {
        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        assertThat(templateContentLocalisation.getUnionType(caseData, LanguagePreference.WELSH)).isEqualTo("ysgariad");

        caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(templateContentLocalisation.getUnionType(caseData, LanguagePreference.WELSH)).isEqualTo("diddymiad");
    }
}

