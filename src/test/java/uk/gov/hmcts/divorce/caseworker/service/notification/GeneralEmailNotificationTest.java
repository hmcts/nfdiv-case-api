package uk.gov.hmcts.divorce.caseworker.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralEmailDetails;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.CaseDocumentAccessManagement;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Stream.ofNullable;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.RESPONDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_OTHER_PARTY;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_PETITIONER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_PETITIONER_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_RESPONDENT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.GENERAL_EMAIL_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
public class GeneralEmailNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @Mock
    private Resource resource;

    @Mock
    private CaseDocumentAccessManagement documentManagementClient;

    @InjectMocks
    private GeneralEmailNotification generalEmailNotification;

    @Test
    public void shouldSendEmailNotificationToApplicantWhenGeneralEmailPartyIsPetitionerAndIsNotSolicitorRepresented() throws Exception {
        final var caseData = caseData();

        final var applicant1 = getApplicant();
        caseData.setApplicant1(applicant1);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setPartyToEmail(APPLICANT);

        var generalEmailDetails = GeneralEmailDetails
            .builder()
            .generalEmailDateTime(LOCAL_DATE_TIME)
            .generalEmailParties(APPLICANT)
            .generalEmailCreatedBy("Test User")
            .generalEmailBody("Test Body")
            .build();

        ListValue<GeneralEmailDetails> generalEmailDetailsListValue =
            ListValue
                .<GeneralEmailDetails>builder()
                .id(UUID.randomUUID().toString())
                .value(generalEmailDetails)
                .build();

        caseData.setGeneralEmails(List.of(generalEmailDetailsListValue));

        generalEmailNotification.send(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmailWithAttachment(
            eq(TEST_USER_EMAIL),
            eq(GENERAL_EMAIL_PETITIONER),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
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

        caseData.setPartyToEmail(RESPONDENT);

        var generalEmailDetails = GeneralEmailDetails
            .builder()
            .generalEmailDateTime(LOCAL_DATE_TIME)
            .generalEmailParties(RESPONDENT)
            .generalEmailCreatedBy("Test User")
            .generalEmailBody("Test Body")
            .build();

        ListValue<GeneralEmailDetails> generalEmailDetailsListValue =
            ListValue
                .<GeneralEmailDetails>builder()
                .id(UUID.randomUUID().toString())
                .value(generalEmailDetails)
                .build();

        caseData.setGeneralEmails(List.of(generalEmailDetailsListValue));

        generalEmailNotification.send(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmailWithAttachment(
            eq(TEST_USER_EMAIL),
            eq(GENERAL_EMAIL_RESPONDENT),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldNotSendEmailWhenPartyIsRespondentAndIsNotSolicitorRepresentedAndRespondentEmailIsNotPresent() throws Exception {
        final var caseData = caseData();

        final var applicant2 = getApplicant();
        applicant2.setEmail(null);
        caseData.setApplicant2(applicant2);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant2Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setPartyToEmail(RESPONDENT);

        var generalEmailDetails = GeneralEmailDetails
            .builder()
            .generalEmailDateTime(LOCAL_DATE_TIME)
            .generalEmailParties(RESPONDENT)
            .generalEmailCreatedBy("Test User")
            .generalEmailBody("Test Body")
            .build();

        ListValue<GeneralEmailDetails> generalEmailDetailsListValue =
            ListValue
                .<GeneralEmailDetails>builder()
                .id(UUID.randomUUID().toString())
                .value(generalEmailDetails)
                .build();

        caseData.setGeneralEmails(List.of(generalEmailDetailsListValue));

        generalEmailNotification.send(caseData, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendEmailNotificationToApplicantSolicitorWhenGeneralEmailPartyIsPetitionerAndIsSolicitorRepresented() throws Exception {
        final var caseData = caseData();

        final var applicant1 = getApplicant();
        applicant1.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant1.setSolicitorRepresented(YES);
        caseData.setApplicant1(applicant1);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setApplicant2Name(APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setPartyToEmail(APPLICANT);

        var generalEmailDetails = GeneralEmailDetails
            .builder()
            .generalEmailDateTime(LOCAL_DATE_TIME)
            .generalEmailParties(APPLICANT)
            .generalEmailCreatedBy("Test User")
            .generalEmailBody("Test Body")
            .build();

        ListValue<GeneralEmailDetails> generalEmailDetailsListValue =
            ListValue
                .<GeneralEmailDetails>builder()
                .id(UUID.randomUUID().toString())
                .value(generalEmailDetails)
                .build();

        caseData.setGeneralEmails(List.of(generalEmailDetailsListValue));

        generalEmailNotification.send(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmailWithAttachment(
            eq(TEST_SOLICITOR_EMAIL),
            eq(GENERAL_EMAIL_PETITIONER_SOLICITOR),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendEmailNotificationToRespondentSolicitorWhenGeneralEmailPartyIsRespondentAndIsSolicitorRepresented() throws Exception {
        final var caseData = caseData();

        final var applicant2 = getApplicant();
        applicant2.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());
        applicant2.setSolicitorRepresented(YES);
        caseData.setApplicant2(applicant2);

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setApplicant2Name(APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setPartyToEmail(RESPONDENT);

        var generalEmailDetails = GeneralEmailDetails
            .builder()
            .generalEmailDateTime(LOCAL_DATE_TIME)
            .generalEmailParties(RESPONDENT)
            .generalEmailCreatedBy("Test User")
            .generalEmailBody("Test Body")
            .build();

        ListValue<GeneralEmailDetails> generalEmailDetailsListValue =
            ListValue
                .<GeneralEmailDetails>builder()
                .id(UUID.randomUUID().toString())
                .value(generalEmailDetails)
                .build();

        caseData.setGeneralEmails(List.of(generalEmailDetailsListValue));

        generalEmailNotification.send(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmailWithAttachment(
            eq(TEST_SOLICITOR_EMAIL),
            eq(GENERAL_EMAIL_RESPONDENT_SOLICITOR),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendEmailNotificationToOtherPartyWhenGeneralEmailPartyIsOther() throws Exception {
        final var caseData = caseData();

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setApplicant2Name(APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME);

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setPartyToEmail(OTHER);

        var generalEmailDetails = GeneralEmailDetails
            .builder()
            .generalEmailDateTime(LOCAL_DATE_TIME)
            .generalEmailParties(OTHER)
            .generalEmailCreatedBy("Test User")
            .generalEmailBody("Test Body")
            .generalEmailToOtherEmail(TEST_USER_EMAIL)
            .generalEmailToOtherName("Other User")
            .build();

        ListValue<GeneralEmailDetails> generalEmailDetailsListValue =
            ListValue
                .<GeneralEmailDetails>builder()
                .id(UUID.randomUUID().toString())
                .value(generalEmailDetails)
                .build();

        caseData.setGeneralEmails(List.of(generalEmailDetailsListValue));

        generalEmailNotification.send(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmailWithAttachment(
            eq(TEST_USER_EMAIL),
            eq(GENERAL_EMAIL_OTHER_PARTY),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendEmailNotificationWithEmailAttached() throws Exception {
        final var caseData = caseData();

        final var marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setApplicant2Name(APPLICANT_2_FIRST_NAME + " " + APPLICANT_2_LAST_NAME);

        final String userId = UUID.randomUUID().toString();
        final User systemUpdateUser = caseWorkerUser();

        given(idamService.retrieveSystemUpdateUserDetails()).willReturn(systemUpdateUser);
        given(authTokenGenerator.generate()).willReturn(TEST_SERVICE_AUTH_TOKEN);

        final byte[] firstFile = "data from file 1".getBytes(StandardCharsets.UTF_8);

        given(resource.getInputStream())
            .willReturn(new ByteArrayInputStream(firstFile));

        caseData.setApplication(Application.builder().marriageDetails(marriageDetails).build());

        caseData.setPartyToEmail(APPLICANT);

        List<ListValue<Document>> attachments = ofNullable(documentWithType(DocumentType.APPLICATION))
            .map(divorceDocument -> ListValue.<Document>builder()
                .id(divorceDocument.getId())
                .value(divorceDocument.getValue().getDocumentLink()).build())
            .toList();

        given(documentManagementClient
            .downloadBinary(SYSTEM_UPDATE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, attachments.get(0).getValue()))
            .willReturn(ResponseEntity.ok(resource));

        var generalEmailDetails = GeneralEmailDetails
            .builder()
            .generalEmailDateTime(LOCAL_DATE_TIME)
            .generalEmailParties(APPLICANT)
            .generalEmailCreatedBy("Test User")
            .generalEmailBody("Test Body")
            .generalEmailAttachmentLinks(attachments)
            .build();

        ListValue<GeneralEmailDetails> generalEmailDetailsListValue =
            ListValue
                .<GeneralEmailDetails>builder()
                .id(UUID.randomUUID().toString())
                .value(generalEmailDetails)
                .build();

        caseData.setGeneralEmails(List.of(generalEmailDetailsListValue));

        generalEmailNotification.send(caseData, TEST_CASE_ID);

        verify(idamService).retrieveSystemUpdateUserDetails();

        verify(documentManagementClient).downloadBinary(
            SYSTEM_UPDATE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            attachments.get(0).getValue()
        );

        verify(authTokenGenerator).generate();
        verify(notificationService).sendEmailWithAttachment(
            eq(TEST_USER_EMAIL),
            eq(GENERAL_EMAIL_PETITIONER),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    private User caseWorkerUser() {
        return new User(
            SYSTEM_UPDATE_AUTH_TOKEN,
            UserInfo.builder()
                .uid(SYSTEM_USER_USER_ID)
                .build());
    }
}
