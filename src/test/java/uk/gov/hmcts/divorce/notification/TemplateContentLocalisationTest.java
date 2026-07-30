package uk.gov.hmcts.divorce.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT_CY;
import static uk.gov.hmcts.divorce.notification.CommonContent.CONTACT_TEXT;
import static uk.gov.hmcts.divorce.notification.CommonContent.CONTACT_TEXT_WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.DO_NOT_REPLY;
import static uk.gov.hmcts.divorce.notification.CommonContent.DO_NOT_REPLY_WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.IDAM_INACTIVITY_POLICY;
import static uk.gov.hmcts.divorce.notification.CommonContent.IDAM_INACTIVITY_POLICY_CY;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.CommonContent.USER;
import static uk.gov.hmcts.divorce.notification.CommonContent.USER_CY;
import static uk.gov.hmcts.divorce.notification.CommonContent.WEBFORM_CY_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.WEBFORM_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_IDAM_INACTIVITY_POLICY;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_IDAM_INACTIVITY_POLICY_CY;
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
    void shouldGetWebFormUrl() {
        Map<String, String> templateVars = Map.of(
            WEBFORM_URL, WEBFORM_URL,
            WEBFORM_CY_URL, WEBFORM_CY_URL
        );
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(templateVars);

        assertThat(templateContentLocalisation.getWebFormUrl(ENGLISH)).isEqualTo(WEBFORM_URL);

        assertThat(templateContentLocalisation.getWebFormUrl(WELSH)).isEqualTo(WEBFORM_CY_URL);
    }

    @Test
    void shouldGetIssueDate() {
        CaseData caseData = caseData();
        caseData.getApplication().setIssueDate(null);
        assertThat(templateContentLocalisation.getIssueDate(caseData.getApplication().getIssueDate(), ENGLISH)).isEqualTo("");

        caseData = caseData();
        caseData.getApplication().setIssueDate(null);
        assertThat(templateContentLocalisation.getIssueDate(caseData.getApplication().getIssueDate(), WELSH)).isEqualTo("");

        caseData = caseData();
        caseData.getApplication().setIssueDate(LocalDate.of(2020, 1, 1));
        assertThat(templateContentLocalisation.getIssueDate(caseData.getApplication().getIssueDate(), ENGLISH)).isEqualTo("1 January 2020");

        caseData = caseData();
        caseData.getApplication().setIssueDate(LocalDate.of(2020, 1, 1));
        assertThat(templateContentLocalisation.getIssueDate(caseData.getApplication().getIssueDate(), WELSH)).isEqualTo("1 Ionawr 2020");
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
        assertThat(templateContentLocalisation.getUnionType(caseData, WELSH)).isEqualTo("ysgariad");

        caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        assertThat(templateContentLocalisation.getUnionType(caseData, WELSH)).isEqualTo("diddymiad");
    }

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
    void shouldGetSmartSurvey() {
        Map<String, String> templateVars = Map.of(
            SMART_SURVEY, SMART_SURVEY
        );
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(templateVars);

        String expectedString = SMART_SURVEY + System.lineSeparator() + System.lineSeparator() + "##";

        assertThat(templateContentLocalisation.getSmartSurvey(ENGLISH)).isEqualTo(expectedString + DO_NOT_REPLY);

        assertThat(templateContentLocalisation.getSmartSurvey(WELSH)).isEqualTo(expectedString + DO_NOT_REPLY_WELSH);
    }

    @Test
    void shouldContactWebFormText() {
        Map<String, String> templateVars = Map.of(
            WEBFORM_URL, WEBFORM_URL,
            WEBFORM_CY_URL, WEBFORM_CY_URL
        );
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(templateVars);

        assertThat(templateContentLocalisation.getContactWebFormText(ENGLISH)).isEqualTo(CONTACT_TEXT + "(webformUrl)");

        assertThat(templateContentLocalisation.getContactWebFormText(WELSH)).isEqualTo(CONTACT_TEXT_WELSH + "(webformCyUrl)");
    }

    @Test
    void shouldGetPhoneAndOpeningTimes() {
        assertThat(templateContentLocalisation.getPhoneAndOpeningTimes(ENGLISH)).isEqualTo(PHONE_AND_OPENING_TIMES_TEXT);

        assertThat(templateContentLocalisation.getPhoneAndOpeningTimes(WELSH)).isEqualTo(PHONE_AND_OPENING_TIMES_TEXT_CY);
    }

    @Test
    void shouldGetUserString() {
        assertThat(templateContentLocalisation.getUserString(ENGLISH)).isEqualTo(USER);

        assertThat(templateContentLocalisation.getUserString(WELSH)).isEqualTo(USER_CY);
    }

    @Test
    void shouldGetIdamInactivityPolicy() {
        Map<String, String> templateVars = Map.of(
            IDAM_INACTIVITY_POLICY, TEST_IDAM_INACTIVITY_POLICY,
            IDAM_INACTIVITY_POLICY_CY, TEST_IDAM_INACTIVITY_POLICY_CY
        );
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(templateVars);

        assertThat(templateContentLocalisation.getIdamInactivityPolicy(ENGLISH)).isEqualTo(TEST_IDAM_INACTIVITY_POLICY);

        assertThat(templateContentLocalisation.getIdamInactivityPolicy(WELSH)).isEqualTo(TEST_IDAM_INACTIVITY_POLICY_CY);
    }
}

