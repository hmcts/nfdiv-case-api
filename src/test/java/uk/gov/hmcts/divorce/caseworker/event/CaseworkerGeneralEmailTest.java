package uk.gov.hmcts.divorce.caseworker.event;

import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.service.notification.GeneralEmailNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralEmail;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralEmailDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.testutil.ConfigTestUtil;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralEmail.CASEWORKER_CREATE_GENERAL_EMAIL;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralEmail.MAX_NUMBER_GENERAL_EMAIL_ATTACHMENTS;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.RESPONDENT;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class CaseworkerGeneralEmailTest {

    private static final String NO_VALID_EMAIL_ERROR
        = "You cannot send an email because no email address has been provided for this party.";

    @Mock
    private GeneralEmailNotification generalEmailNotification;

    @Mock
    private Clock clock;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private IdamService idamService;

    @Mock
    private DocumentIdProvider documentIdProvider;

    @InjectMocks
    private CaseworkerGeneralEmail generalEmail;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = ConfigTestUtil.createCaseDataConfigBuilder();

        generalEmail.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .containsExactly(CASEWORKER_CREATE_GENERAL_EMAIL);
    }

    @Test
    void shouldSetGeneralEmailToNullInAboutToStart() {
        final CaseData caseData = caseData();
        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(details);

        assertNull(response.getData().getGeneralEmail());
    }

    @Test
    void shouldSetGeneralEmailDetailsWhenExistingGeneralEmailsIsNull() throws Exception {
        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN))
            .thenReturn(new User(
                    TEST_AUTHORIZATION_TOKEN, UserInfo
                    .builder()
                    .givenName("forename")
                    .familyName("lastname")
                    .name("forename lastname")
                    .build()
                )
            );

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToSubmit(details, details);

        assertThat(response.getData().getGeneralEmails())
            .extracting("value")
            .extracting("generalEmailDateTime", "generalEmailParties", "generalEmailCreatedBy", "generalEmailBody")
            .contains(tuple(getExpectedLocalDateTime(), APPLICANT, "forename lastname", "some details"));
    }

    @Test
    void shouldAddToTopOfExistingGeneralEmailsWhenThereIsExistingGeneralEmail() throws Exception {
        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details 2")
                .build()
        );

        var generalEmailDetails = GeneralEmailDetails
            .builder()
            .generalEmailDateTime(LocalDateTime.now(clock))
            .generalEmailParties(RESPONDENT)
            .generalEmailCreatedBy("forename lastname")
            .generalEmailBody("some details 1")
            .build();

        ListValue<GeneralEmailDetails> generalEmailDetailsListValue =
            ListValue
                .<GeneralEmailDetails>builder()
                .id(UUID.randomUUID().toString())
                .value(generalEmailDetails)
                .build();

        caseData.setGeneralEmails(Lists.newArrayList(generalEmailDetailsListValue));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN))
            .thenReturn(new User(
                    TEST_AUTHORIZATION_TOKEN, UserInfo
                    .builder()
                    .givenName("forename")
                    .familyName("lastname")
                    .name("forename lastname")
                    .build()
                )
            );

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToSubmit(details, details);

        assertThat(response.getData().getGeneralEmails())
            .extracting("value")
            .extracting("generalEmailDateTime", "generalEmailParties", "generalEmailCreatedBy", "generalEmailBody")
            .contains(
                tuple(getExpectedLocalDateTime(), APPLICANT, "forename lastname", "some details 2"),
                tuple(getExpectedLocalDateTime(), RESPONDENT, "forename lastname", "some details 1")
            );
    }


    @Test
    void shouldSetConfidentialGeneralEmailDetails() throws Exception {
        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.getApplicant1().setContactDetailsType(ContactDetailsType.PRIVATE);
        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN))
            .thenReturn(new User(
                    TEST_AUTHORIZATION_TOKEN, UserInfo
                    .builder()
                    .givenName("forename")
                    .familyName("lastname")
                    .name("forename lastname")
                    .build()
                )
            );

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToSubmit(details, details);

        assertThat(response.getData().getConfidentialGeneralEmails())
            .extracting("value")
            .extracting("generalEmailDateTime", "generalEmailParties", "generalEmailCreatedBy", "generalEmailBody")
            .contains(tuple(getExpectedLocalDateTime(), APPLICANT, "forename lastname", "some details"));

        assertNull(response.getData().getGeneralEmails());
    }

    @Test
    void shouldReturnAnErrorWhenNoValidEmailIsFoundDuringMidEventValidationWhenApplicant() {

        final CaseData caseData = caseData();
        caseData.getApplicant1().setEmail(null);

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.midEvent(details, null);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo(NO_VALID_EMAIL_ERROR);
    }

    @Test
    void shouldReturnAnErrorWhenNoValidEmailIsFoundDuringMidEventValidationWhenApplicantSolicitor() {

        final CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(Solicitor.builder().email(null).build());

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.midEvent(details, null);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo(NO_VALID_EMAIL_ERROR);
    }

    @Test
    void shouldReturnNoErrorsDuringMidEventValidationWhenValidEmailForApplicant() {

        final CaseData caseData = caseData();

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.midEvent(details, null);

        assertThat(response.getErrors()).isEqualTo(null);
    }

    @Test
    void shouldReturnNoErrorsDuringMidEventValidationWhenValidEmailForApplicantSolicitor() {

        final CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build());

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.midEvent(details, null);

        assertThat(response.getErrors()).isEqualTo(null);
    }

    @Test
    void shouldReturnAnErrorWhenNoValidEmailIsFoundDuringMidEventValidationWhenRespondent() {

        final CaseData caseData = caseData();
        caseData.getApplicant2().setEmail(null);

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(RESPONDENT)
                .generalEmailDetails("some details")
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.midEvent(details, null);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo(NO_VALID_EMAIL_ERROR);
    }

    @Test
    void shouldReturnAnErrorWhenNoValidEmailIsFoundDuringMidEventValidationWhenRespondentSolicitor() {

        final CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(Solicitor.builder().email(null).build());

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(RESPONDENT)
                .generalEmailDetails("some details")
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.midEvent(details, null);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo(NO_VALID_EMAIL_ERROR);
    }

    @Test
    void shouldReturnNoErrorsDuringMidEventValidationWhenValidEmailForRespondent() {

        final CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(RESPONDENT)
                .generalEmailDetails("some details")
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.midEvent(details, null);

        assertThat(response.getErrors()).isEqualTo(null);
    }

    @Test
    void shouldReturnNoErrorsDuringMidEventValidationWhenValidEmailForRespondentSolicitor() {

        final CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build());

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(RESPONDENT)
                .generalEmailDetails("some details")
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.midEvent(details, null);

        assertThat(response.getErrors()).isEqualTo(null);
    }

    @Test
    void shouldReturnAnErrorWhenNoValidEmailIsFoundDuringMidEventValidationWhenOther() {

        final CaseData caseData = caseData();

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(OTHER)
                .generalEmailDetails("some details")
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.midEvent(details, null);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo(NO_VALID_EMAIL_ERROR);
    }

    @Test
    void shouldReturnNoErrorsDuringMidEventValidationWhenOther() {

        final CaseData caseData = caseData();

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(OTHER)
                .generalEmailOtherRecipientEmail(TEST_USER_EMAIL)
                .generalEmailDetails("some details")
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.midEvent(details, null);

        assertThat(response.getErrors()).isEqualTo(null);
    }

    @Test
    void shouldReturnErrorIfDocumentLinkNotProvidedGeneralEmailAttachments() {
        ListValue<DivorceDocument> generalEmailAttachment = new ListValue<>(
            "1",
            DivorceDocument
                .builder()
                .build()
        );
        final CaseData caseData = caseData();

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .generalEmailAttachments(singletonList(generalEmailAttachment))
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.midEvent(details, details);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Please ensure all General Email attachments have been uploaded before continuing");
    }

    @Test
    void shouldReturnErrorIfAttachmentsExceedMaxAllowed() {

        final CaseData caseData = caseData();

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .generalEmailAttachments(getListofDocument(11))
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.midEvent(details, details);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(String.format(
            "Number of attachments on General Email cannot exceed %s",MAX_NUMBER_GENERAL_EMAIL_ATTACHMENTS));
    }

    @Test
    void shouldSendEmailWhenEventSubmitted() throws Exception {

        final CaseData caseData = caseData();

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .generalEmailAttachments(getListofDocument(2))
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        SubmittedCallbackResponse response = generalEmail.submitted(details, details);

        verify(generalEmailNotification).send(caseData,TEST_CASE_ID);
    }

    @Test
    void shouldReturnWarningAboutAttachmentSizeWhenDocumentsAttached() {
        setMockClock(clock);

        final CaseData caseData = caseData();

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .generalEmailAttachments(getListofDocument(2))
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);

        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(getCaseworkerUser());

        when(documentIdProvider.documentId()).thenReturn("1");

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToSubmit(details, details);

        assertThat(response.getWarnings()).isNotEmpty();
        assertThat(response.getWarnings()).hasSize(1);
        assertThat(response.getWarnings()).contains("Please ensure all individual attachments are smaller than 2MB. "
            + "If you are sure that all attachments are smaller than 2MB, submit the event again to proceed.");
    }

    @Test
    void shouldReturnNoWarningAboutAttachmentSizeWhenDocumentsNotAttached() {
        setMockClock(clock);

        final CaseData caseData = caseData();

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);

        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(getCaseworkerUser());

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToSubmit(details, details);

        assertThat(response.getWarnings()).isEmpty();
    }

    List<ListValue<DivorceDocument>> getListofDocument(int size) {
        List<ListValue<DivorceDocument>> docList = new ArrayList<>();
        while (size > 0) {
            ListValue<DivorceDocument> generalEmailAttachment = new ListValue<>(
                String.valueOf(size),
                DivorceDocument
                    .builder()
                    .documentLink(Document.builder().build())
                    .build()
            );
            docList.add(generalEmailAttachment);
            size--;
        }
        return docList;
    }

    private User getCaseworkerUser() {
        var userDetails = UserInfo
            .builder()
            .givenName("testFname")
            .familyName("testSname")
            .name("testFname testSname")
            .build();

        return new User(TEST_AUTHORIZATION_TOKEN, userDetails);
    }
}
