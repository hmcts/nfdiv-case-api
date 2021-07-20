package uk.gov.hmcts.divorce.caseworker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralEmail;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.testutil.FunctionalTestSuite;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralEmail.CASEWORKER_CREATE_GENERAL_EMAIL;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.RESPONDENT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@SpringBootTest
public class CaseworkerGeneralEmailTest extends FunctionalTestSuite {

    private static final String GOV_UK_TEST_EMAIL = "simulate-delivered@notifications.service.gov.uk";

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldSendEmailNotificationToApplicantWhenGeneralEmailPartyIsPetitionerAndIsNotSolicitorRepresented() throws IOException {
        final var caseData = caseData();

        final var applicant1 = getApplicant();
        applicant1.setEmail(GOV_UK_TEST_EMAIL);
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

        Map<String, Object> caseDataMap = objectMapper.convertValue(caseData, new TypeReference<Map<String, Object>>() {
        });

        Response response = triggerCallback(caseDataMap, CASEWORKER_CREATE_GENERAL_EMAIL, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendEmailNotificationToApplicantSolicitorWhenGeneralEmailPartyIsPetitionerAndIsSolicitorRepresented()
        throws IOException {
        final var caseData = caseData();

        final var applicant1 = getApplicant();
        applicant1.setSolicitor(Solicitor.builder().email(GOV_UK_TEST_EMAIL).name(TEST_SOLICITOR_NAME).build());
        caseData.setApplicant1(applicant1);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setApplicant2Name(APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setGeneralEmail(GeneralEmail
            .builder()
            .generalEmailDetails("some details")
            .generalEmailParties(APPLICANT)
            .build()
        );

        Map<String, Object> caseDataMap = objectMapper.convertValue(caseData, new TypeReference<Map<String, Object>>() {
        });

        Response response = triggerCallback(caseDataMap, CASEWORKER_CREATE_GENERAL_EMAIL, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendEmailNotificationToRespondentWhenGeneralEmailPartyIsRespondentAndIsNotSolicitorRepresented() throws IOException {
        final var caseData = caseData();

        final var applicant2 = getApplicant();
        applicant2.setEmail(GOV_UK_TEST_EMAIL);
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

        Map<String, Object> caseDataMap = objectMapper.convertValue(caseData, new TypeReference<Map<String, Object>>() {
        });

        Response response = triggerCallback(caseDataMap, CASEWORKER_CREATE_GENERAL_EMAIL, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendEmailNotificationToRespondentSolicitorWhenGeneralEmailPartyIsRespondentAndIsSolicitorRepresented()
        throws IOException {
        final var caseData = caseData();

        final var applicant2 = getApplicant();
        applicant2.setSolicitor(Solicitor.builder().email(GOV_UK_TEST_EMAIL).name(TEST_SOLICITOR_NAME).build());
        caseData.setApplicant2(applicant2);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setApplicant2Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setGeneralEmail(GeneralEmail
            .builder()
            .generalEmailDetails("some details")
            .generalEmailParties(RESPONDENT)
            .build()
        );

        Map<String, Object> caseDataMap = objectMapper.convertValue(caseData, new TypeReference<Map<String, Object>>() {
        });

        Response response = triggerCallback(caseDataMap, CASEWORKER_CREATE_GENERAL_EMAIL, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }

    @Test
    public void shouldSendEmailNotificationToOtherPartyWhenGeneralEmailPartyIsOther() throws IOException {
        final var caseData = caseData();

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setApplicant2Name(APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setGeneralEmail(GeneralEmail
            .builder()
            .generalEmailDetails("some details")
            .generalEmailOtherRecipientEmail(GOV_UK_TEST_EMAIL)
            .generalEmailOtherRecipientName("otherparty")
            .generalEmailParties(OTHER)
            .build()
        );

        Map<String, Object> caseDataMap = objectMapper.convertValue(caseData, new TypeReference<Map<String, Object>>() {
        });

        Response response = triggerCallback(caseDataMap, CASEWORKER_CREATE_GENERAL_EMAIL, ABOUT_TO_SUBMIT_URL);

        assertThat(response.getStatusCode()).isEqualTo(OK.value());
    }
}
