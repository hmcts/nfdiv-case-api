package uk.gov.hmcts.divorce.caseworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.divorce.caseworker.service.notification.GeneralEmailNotification;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralEmail;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralEmail.CASEWORKER_CREATE_GENERAL_EMAIL;
import static uk.gov.hmcts.divorce.caseworker.service.notification.GeneralEmailNotification.GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.divorce.caseworker.service.notification.GeneralEmailNotification.GENERAL_OTHER_RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.RESPONDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_OTHER_PARTY;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_PETITIONER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_PETITIONER_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_RESPONDENT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.CASEWORKER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@ContextConfiguration(initializers = {
    IdamWireMock.PropertiesInitializer.class
})
public class CaseworkerGeneralEmailIT {

    @Autowired
    private GeneralEmailNotification generalEmailNotification;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private EmailTemplatesConfig emailTemplatesConfig;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void shouldSendEmailNotificationToApplicantWhenGeneralEmailPartyIsPetitionerAndIsNotSolicitorRepresented() throws Exception {
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(DIVORCE_COURT_EMAIL, "divorce.court@email.com"));
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);

        final var caseData = caseData();
        final var applicant1 = getApplicant();
        caseData.setApplicant1(applicant1);

        caseData.setGeneralEmail(getGeneralEmailObject(APPLICANT));

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_CREATE_GENERAL_EMAIL)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(GENERAL_EMAIL_DETAILS, "Test Body");
        templateVars.put(GENERAL_OTHER_RECIPIENT_NAME, null);
        templateVars.put(APPLICANT_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        templateVars.put(APPLICATION_REFERENCE, formatId(TEST_CASE_ID));
        templateVars.put(RESPONDENT_NAME, "null null");
        templateVars.put(COURT_EMAIL, "divorce.court@email.com");

        Map<String, Object> templateVarsObj = populateAttachmentVars(templateVars);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            GENERAL_EMAIL_PETITIONER,
            templateVarsObj,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendEmailNotificationToApplicantSolicitorWhenGeneralEmailPartyIsPetitionerAndIsSolicitorRepresented() throws Exception {
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(DIVORCE_COURT_EMAIL, "divorce.court@email.com"));
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);

        final var caseData = caseData();

        final var applicant1 = getApplicant();
        applicant1.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant1.setSolicitorRepresented(YES);
        caseData.setApplicant1(applicant1);
        caseData.setApplicant2(Applicant.builder().firstName(APPLICANT_2_FIRST_NAME).lastName(APPLICANT_2_LAST_NAME).build());

        caseData.setGeneralEmail(getGeneralEmailObject(APPLICANT));

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_CREATE_GENERAL_EMAIL)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(GENERAL_EMAIL_DETAILS, "Test Body");
        templateVars.put(SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        templateVars.put(GENERAL_OTHER_RECIPIENT_NAME, null);
        templateVars.put(APPLICANT_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        templateVars.put(APPLICATION_REFERENCE, formatId(TEST_CASE_ID));
        templateVars.put(RESPONDENT_NAME, APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME);
        templateVars.put(COURT_EMAIL, "divorce.court@email.com");

        Map<String, Object> templateVarsObj = populateAttachmentVars(templateVars);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            GENERAL_EMAIL_PETITIONER_SOLICITOR,
            templateVarsObj,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendEmailNotificationToRespondentWhenGeneralEmailPartyIsRespondentAndIsNotSolicitorRepresented() throws Exception {
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(DIVORCE_COURT_EMAIL, "divorce.court@email.com"));
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        final var caseData = caseData();
        final var applicant2 = getApplicant();
        caseData.setApplicant2(applicant2);

        caseData.setGeneralEmail(getGeneralEmailObject(RESPONDENT));

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_CREATE_GENERAL_EMAIL)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(GENERAL_EMAIL_DETAILS, "Test Body");
        templateVars.put(GENERAL_OTHER_RECIPIENT_NAME, null);
        templateVars.put(APPLICANT_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        templateVars.put(APPLICATION_REFERENCE, formatId(TEST_CASE_ID));
        templateVars.put(RESPONDENT_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        templateVars.put(COURT_EMAIL, "divorce.court@email.com");

        Map<String, Object> templateVarsObj = populateAttachmentVars(templateVars);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            GENERAL_EMAIL_RESPONDENT,
            templateVarsObj,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendEmailNotificationToRespondentSolicitorWhenGeneralEmailPartyIsRespondentAndIsSolicitorRepresented() throws Exception {
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(DIVORCE_COURT_EMAIL, "divorce.court@email.com"));
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);

        final var caseData = caseData();
        final var applicant2 = getApplicant();
        applicant2.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant2.setSolicitorRepresented(YES);
        applicant2.setFirstName(APPLICANT_2_FIRST_NAME);
        applicant2.setLastName(APPLICANT_2_LAST_NAME);
        caseData.setApplicant2(applicant2);
        caseData.setApplicant1(Applicant.builder().firstName(TEST_FIRST_NAME).lastName(TEST_LAST_NAME).build());

        caseData.setGeneralEmail(getGeneralEmailObject(RESPONDENT));

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_CREATE_GENERAL_EMAIL)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(GENERAL_EMAIL_DETAILS, "Test Body");
        templateVars.put(SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        templateVars.put(GENERAL_OTHER_RECIPIENT_NAME, null);
        templateVars.put(APPLICANT_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        templateVars.put(APPLICATION_REFERENCE, formatId(TEST_CASE_ID));
        templateVars.put(RESPONDENT_NAME, APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME);
        templateVars.put(COURT_EMAIL, "divorce.court@email.com");

        Map<String, Object> templateVarsObj = populateAttachmentVars(templateVars);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            GENERAL_EMAIL_RESPONDENT_SOLICITOR,
            templateVarsObj,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendEmailNotificationToOtherPartyWhenGeneralEmailPartyIsOther() throws Exception {
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(DIVORCE_COURT_EMAIL, "divorce.court@email.com"));
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        final var caseData = caseData();
        caseData.setApplicant1(Applicant.builder().firstName(TEST_FIRST_NAME).lastName(TEST_LAST_NAME).build());
        caseData.setApplicant2(Applicant.builder().firstName(APPLICANT_2_FIRST_NAME).lastName(APPLICANT_2_LAST_NAME).build());

        caseData.setGeneralEmail(getGeneralEmailObject(OTHER));

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_CREATE_GENERAL_EMAIL)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(GENERAL_EMAIL_DETAILS, "Test Body");
        templateVars.put(GENERAL_OTHER_RECIPIENT_NAME, "otherparty");
        templateVars.put(APPLICANT_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        templateVars.put(APPLICATION_REFERENCE, formatId(TEST_CASE_ID));
        templateVars.put(RESPONDENT_NAME, APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME);
        templateVars.put(COURT_EMAIL, "divorce.court@email.com");

        Map<String, Object> templateVarsObj = populateAttachmentVars(templateVars);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            GENERAL_EMAIL_OTHER_PARTY,
            templateVarsObj,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    GeneralEmail getGeneralEmailObject(GeneralParties party) {
        GeneralEmail generalEmail;

        if (OTHER == party) {
            generalEmail = GeneralEmail
                .builder()
                .generalEmailDetails("Test Body")
                .generalEmailParties(party)
                .generalEmailOtherRecipientName("otherparty")
                .generalEmailOtherRecipientEmail(TEST_USER_EMAIL)
                .build();
        } else {
            generalEmail = GeneralEmail
                .builder()
                .generalEmailDetails("Test Body")
                .generalEmailParties(party)
                .build();
        }

        return generalEmail;
    }

    Map<String, Object> populateAttachmentVars(Map<String, String> templateVars) {
        templateVars.put("sot1", "");
        templateVars.put("sot2", "");
        templateVars.put("sot3", "");
        templateVars.put("sot4", "");
        templateVars.put("sot5", "");
        templateVars.put("sot6", "");
        templateVars.put("sot7", "");
        templateVars.put("sot8", "");
        templateVars.put("sot9", "");
        templateVars.put("sot10", "");
        templateVars.put("areDocuments","no");

        Map<String, Object> templateVarsObj = new HashMap<>(templateVars);

        return templateVarsObj;
    }
}
