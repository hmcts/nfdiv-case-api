package uk.gov.hmcts.divorce.caseworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.divorce.caseworker.service.notification.GeneralEmailNotification;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralEmail;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralEmail.CASEWORKER_CREATE_GENERAL_EMAIL;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.RESPONDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_OTHER_PARTY;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_PETITIONER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_PETITIONER_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_RESPONDENT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CCD_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.GENERAL_OTHER_RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
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
public class CaseworkerGeneralEmailTest {

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

    @Test
    void shouldSendEmailNotificationToPetitionerWhenGeneralEmailPartyIsPetitionerAndIsNotSolicitorRepresented() throws Exception {
        final var caseData = caseData();

        final var applicant1 = getApplicant();
        caseData.setApplicant1(applicant1);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setGeneralEmail(GeneralEmail
            .builder()
            .generalEmailDetails("some details")
            .generalEmailParties(APPLICANT)
            .build()
        );

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_CREATE_GENERAL_EMAIL)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(GENERAL_EMAIL_DETAILS, "some details");
        templateVars.put(GENERAL_OTHER_RECIPIENT_NAME, null);
        templateVars.put(APPLICANT_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        templateVars.put(CCD_REFERENCE, String.valueOf(TEST_CASE_ID));
        templateVars.put(RESPONDENT_NAME, null);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            GENERAL_EMAIL_PETITIONER,
            templateVars,
            ENGLISH
        );
    }

    @Test
    void shouldSendEmailNotificationToPetitionerSolicitorWhenGeneralEmailPartyIsPetitionerAndIsSolicitorRepresented() throws Exception {
        final var caseData = caseData();

        final var applicant1 = getApplicant();
        applicant1.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        caseData.setApplicant1(applicant1);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setGeneralEmail(GeneralEmail
            .builder()
            .generalEmailDetails("some details")
            .generalEmailParties(APPLICANT)
            .build()
        );

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_CREATE_GENERAL_EMAIL)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(GENERAL_EMAIL_DETAILS, "some details");
        templateVars.put(SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        templateVars.put(GENERAL_OTHER_RECIPIENT_NAME, null);
        templateVars.put(APPLICANT_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        templateVars.put(CCD_REFERENCE, String.valueOf(TEST_CASE_ID));
        templateVars.put(RESPONDENT_NAME, null);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            GENERAL_EMAIL_PETITIONER_SOLICITOR,
            templateVars,
            ENGLISH
        );
    }

    @Test
    void shouldSendEmailNotificationToRespondentWhenGeneralEmailPartyIsRespondentAndIsNotSolicitorRepresented() throws Exception {
        final var caseData = caseData();

        final var applicant2 = getApplicant();
        caseData.setApplicant2(applicant2);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant2Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setGeneralEmail(GeneralEmail
            .builder()
            .generalEmailDetails("some details")
            .generalEmailParties(RESPONDENT)
            .build()
        );

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_CREATE_GENERAL_EMAIL)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(GENERAL_EMAIL_DETAILS, "some details");
        templateVars.put(GENERAL_OTHER_RECIPIENT_NAME, null);
        templateVars.put(APPLICANT_NAME, null);
        templateVars.put(CCD_REFERENCE, String.valueOf(TEST_CASE_ID));
        templateVars.put(RESPONDENT_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            GENERAL_EMAIL_RESPONDENT,
            templateVars,
            ENGLISH
        );
    }

    @Test
    void shouldSendEmailNotificationToRespondentSolicitorWhenGeneralEmailPartyIsRespondentAndIsSolicitorRepresented() throws Exception {
        final var caseData = caseData();

        final var applicant2 = getApplicant();
        applicant2.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        caseData.setApplicant2(applicant2);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant2Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setGeneralEmail(GeneralEmail
            .builder()
            .generalEmailDetails("some details")
            .generalEmailParties(RESPONDENT)
            .build()
        );

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_CREATE_GENERAL_EMAIL)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(GENERAL_EMAIL_DETAILS, "some details");
        templateVars.put(SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        templateVars.put(GENERAL_OTHER_RECIPIENT_NAME, null);
        templateVars.put(APPLICANT_NAME, null);
        templateVars.put(CCD_REFERENCE, String.valueOf(TEST_CASE_ID));
        templateVars.put(RESPONDENT_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME);

        verify(notificationService).sendEmail(
            TEST_SOLICITOR_EMAIL,
            GENERAL_EMAIL_RESPONDENT_SOLICITOR,
            templateVars,
            ENGLISH
        );
    }

    @Test
    void shouldSendEmailNotificationToOtherPartyWhenGeneralEmailPartyIsOther() throws Exception {
        final var caseData = caseData();

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setApplicant2Name(APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setGeneralEmail(GeneralEmail
            .builder()
            .generalEmailDetails("some details")
            .generalEmailOtherRecipientEmail(TEST_USER_EMAIL)
            .generalEmailOtherRecipientName("otherparty")
            .generalEmailParties(OTHER)
            .build()
        );

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_CREATE_GENERAL_EMAIL)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(GENERAL_EMAIL_DETAILS, "some details");
        templateVars.put(GENERAL_OTHER_RECIPIENT_NAME, "otherparty");
        templateVars.put(APPLICANT_NAME, TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        templateVars.put(CCD_REFERENCE, String.valueOf(TEST_CASE_ID));
        templateVars.put(RESPONDENT_NAME, APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            GENERAL_EMAIL_OTHER_PARTY,
            templateVars,
            ENGLISH
        );
    }
}
