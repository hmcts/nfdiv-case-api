package uk.gov.hmcts.divorce.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_TO_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DISSOLUTION_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RELATIONSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RELATIONSHIP_COURT_HEADER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_URL_NOTIFY_KEY;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;

@ExtendWith(MockitoExtension.class)
class CommonContentTest {

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @InjectMocks
    private CommonContent commonContent;

    @Test
    void shouldSetCommonTemplateVariablesForApplicantForDivorce() {

        final String courtEmail = "court@email.com";
        final Map<String, String> configTemplateVars = new HashMap<>();
        configTemplateVars.put(DIVORCE_COURT_EMAIL, courtEmail);
        configTemplateVars.put(SIGN_IN_DIVORCE_URL, SIGN_IN_DIVORCE_URL);

        final String firstName = "Sarah";
        final String lastName = "Smith";
        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(getApplicant())
            .applicant2(getApplicant2(Gender.MALE))
            .build();

        caseData.getApplicant1().setFirstName(firstName);
        caseData.getApplicant1().setLastName(lastName);

        when(emailTemplatesConfig.getTemplateVars()).thenReturn(configTemplateVars);

        final Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, caseData.getApplicant1(), caseData.getApplicant2());

        assertThat(templateVars).isNotEmpty().hasSize(7)
            .contains(
                entry(FIRST_NAME, firstName),
                entry(LAST_NAME, lastName),
                entry(PARTNER, "husband"),
                entry(RELATIONSHIP, DIVORCE_APPLICATION),
                entry(RELATIONSHIP_COURT_HEADER, "Divorce service"),
                entry(COURT_EMAIL, courtEmail),
                entry(SIGN_IN_URL_NOTIFY_KEY, SIGN_IN_DIVORCE_URL));
    }

    @Test
    void shouldSetCommonTemplateVariablesForApplicantForDissolution() {

        final String courtEmail = "court@email.com";
        final Map<String, String> configTemplateVars = new HashMap<>();
        configTemplateVars.put(DISSOLUTION_COURT_EMAIL, courtEmail);
        configTemplateVars.put(SIGN_IN_DISSOLUTION_URL, SIGN_IN_DISSOLUTION_URL);

        final String firstName = "Sarah";
        final String lastName = "Smith";
        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DISSOLUTION)
            .applicant1(getApplicant())
            .applicant2(getApplicant2(Gender.MALE))
            .build();

        caseData.getApplicant1().setFirstName(firstName);
        caseData.getApplicant1().setLastName(lastName);

        when(emailTemplatesConfig.getTemplateVars()).thenReturn(configTemplateVars);

        final Map<String, String> templateVars = commonContent.templateVarsForApplicant(
            caseData, caseData.getApplicant1(), caseData.getApplicant2());

        assertThat(templateVars).isNotEmpty().hasSize(7)
            .contains(
                entry(FIRST_NAME, firstName),
                entry(LAST_NAME, lastName),
                entry(PARTNER, "civil partner"),
                entry(RELATIONSHIP, APPLICATION_TO_END_CIVIL_PARTNERSHIP),
                entry(RELATIONSHIP_COURT_HEADER, "End a civil partnership service"),
                entry(COURT_EMAIL, courtEmail),
                entry(SIGN_IN_URL_NOTIFY_KEY, SIGN_IN_DISSOLUTION_URL));
    }

    @Test
    void shouldSetCommonTemplateVarsForNotifications() {

        final CaseData caseData = caseData();
        caseData.setApplicant2(respondent());

        final Map<String, String> templateVars = commonContent.commonNotificationTemplateVars(caseData, TEST_CASE_ID);

        assertThat(templateVars).isNotEmpty().hasSize(3)
            .contains(
                entry(APPLICANT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME)),
                entry(RESPONDENT_NAME, join(" ", APPLICANT_2_FIRST_NAME, TEST_LAST_NAME)),
                entry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)));
    }
}
