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
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.caseworker.service.notification.GeneralEmailNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralEmail;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralEmailDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.testutil.ConfigTestUtil;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralEmail.CASEWORKER_CREATE_GENERAL_EMAIL;
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
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceGeneralOrderListValue;

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
    void shouldRemoveStaleEmailTextAndRecipientDataInAboutToStart() throws Exception {
        final CaseData caseData = caseData();

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .generalEmailOtherRecipientName("name")
                .generalEmailOtherRecipientEmail("email")
                .build()
        );
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(details);

        GeneralEmail updatedGeneralEmail = response.getData().getGeneralEmail();
        assertThat(updatedGeneralEmail.getGeneralEmailParties()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailDetails()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailOtherRecipientEmail()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailOtherRecipientName()).isNull();
    }

    @Test
    void shouldRemoveEmailAttachmentsInAboutToStartIfTheyHaveAlreadyBeenDelivered() throws Exception {
        final CaseData caseData = caseData();

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocument(2);
        caseData.getDocuments().setDocumentsGenerated(docs);

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("a general email")
                .generalEmailAttachments(buildGeneralEmailAttachmentsWithDocLinks("123-456", "000-123"))
                .build()
        );

        caseData.setGeneralEmails(
            List.of(
                ListValue.<GeneralEmailDetails>builder().value(
                    GeneralEmailDetails.builder()
                        .generalEmailParties(GeneralParties.APPLICANT)
                        .generalEmailBody("a general email")
                        .generalEmailAttachmentLinks(buildGeneralEmailDetailsAttachmentsWithDocLinks("000-123", "123-456"))
                        .build()
                ).build()
            )
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(details);

        GeneralEmail updatedGeneralEmail = response.getData().getGeneralEmail();
        assertThat(updatedGeneralEmail.getGeneralEmailParties()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailDetails()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailOtherRecipientEmail()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailOtherRecipientName()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailAttachments()).isNull();
    }

    @Test
    void shouldRemoveEmailAttachmentsInAboutToStartIfTheyHaveAlreadyBeenDeliveredConfidential() throws Exception {
        final CaseData caseData = caseData();

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocument(2);
        caseData.getDocuments().setDocumentsGenerated(docs);

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("a general email")
                .generalEmailAttachments(buildGeneralEmailAttachmentsWithDocLinks("123-456", "000-123"))
                .build()
        );

        caseData.setConfidentialGeneralEmails(
            List.of(
                ListValue.<GeneralEmailDetails>builder().value(
                    GeneralEmailDetails.builder()
                        .generalEmailParties(GeneralParties.APPLICANT)
                        .generalEmailBody("a general email")
                        .generalEmailAttachmentLinks(buildGeneralEmailDetailsAttachmentsWithDocLinks("000-123", "123-456"))
                        .build()
                ).build()
            )
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(details);

        GeneralEmail updatedGeneralEmail = response.getData().getGeneralEmail();
        assertThat(updatedGeneralEmail.getGeneralEmailParties()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailDetails()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailOtherRecipientEmail()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailOtherRecipientName()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailAttachments()).isNull();
    }

    @Test
    void shouldNotRemoveGeneralEmailAttachmentsInAboutToStartIfPreviousEmailsHadDifferentAttachments() throws Exception {
        final CaseData caseData = caseData();

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocument(2);
        caseData.getDocuments().setDocumentsGenerated(docs);

        List<ListValue<DivorceDocument>> generalEmailAttachments = buildGeneralEmailAttachmentsWithDocLinks("123-456", "000-124");
        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("a general email")
                .generalEmailAttachments(generalEmailAttachments)
                .build()
        );

        caseData.setGeneralEmails(
            List.of(
                ListValue.<GeneralEmailDetails>builder().value(
                    GeneralEmailDetails.builder()
                        .generalEmailParties(GeneralParties.APPLICANT)
                        .generalEmailBody("a general email")
                        .generalEmailAttachmentLinks(buildGeneralEmailDetailsAttachmentsWithDocLinks("000-123", "123-456"))
                        .build()
                ).build(),
                ListValue.<GeneralEmailDetails>builder().value(
                    GeneralEmailDetails.builder()
                        .generalEmailParties(GeneralParties.APPLICANT)
                        .generalEmailBody("a general email")
                        .build()
                ).build()
            )
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(details);

        GeneralEmail updatedGeneralEmail = response.getData().getGeneralEmail();
        assertThat(updatedGeneralEmail.getGeneralEmailParties()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailDetails()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailOtherRecipientEmail()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailOtherRecipientName()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailAttachments()).isEqualTo(generalEmailAttachments);
    }

    @Test
    void shouldNotRemoveGeneralEmailAttachmentsInAboutToStartIfPreviousEmailsHadDifferentContent() throws Exception {
        final CaseData caseData = caseData();

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocument(2);
        caseData.getDocuments().setDocumentsGenerated(docs);

        List<ListValue<DivorceDocument>> generalEmailAttachments = buildGeneralEmailAttachmentsWithDocLinks("123-456", "000-123");
        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("a general email")
                .generalEmailAttachments(generalEmailAttachments)
                .build()
        );

        caseData.setGeneralEmails(
            List.of(
                ListValue.<GeneralEmailDetails>builder().value(
                    GeneralEmailDetails.builder()
                        .generalEmailParties(GeneralParties.APPLICANT)
                        .generalEmailBody("a different general email")
                        .generalEmailAttachmentLinks(buildGeneralEmailDetailsAttachmentsWithDocLinks("000-123", "123-456"))
                        .build()
                ).build(),
                ListValue.<GeneralEmailDetails>builder().value(
                    GeneralEmailDetails.builder()
                        .generalEmailParties(GeneralParties.APPLICANT)
                        .generalEmailBody("a different general email 2")
                        .build()
                ).build()
            )
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(details);

        GeneralEmail updatedGeneralEmail = response.getData().getGeneralEmail();
        assertThat(updatedGeneralEmail.getGeneralEmailParties()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailDetails()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailOtherRecipientEmail()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailOtherRecipientName()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailAttachments()).isEqualTo(generalEmailAttachments);
    }


    @Test
    void shouldNotRemoveGeneralEmailAttachmentsInAboutToStartIfNoEmailsHaveBeenDelivered() throws Exception {
        final CaseData caseData = caseData();

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocument(2);
        caseData.getDocuments().setDocumentsGenerated(docs);

        List<ListValue<DivorceDocument>> generalEmailAttachments = buildGeneralEmailAttachmentsWithDocLinks("123-456", "000-124");
        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("a general email")
                .generalEmailAttachments(generalEmailAttachments)
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(details);

        GeneralEmail updatedGeneralEmail = response.getData().getGeneralEmail();
        assertThat(updatedGeneralEmail.getGeneralEmailParties()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailDetails()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailOtherRecipientEmail()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailOtherRecipientName()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailAttachments()).isEqualTo(generalEmailAttachments);
    }

    @Test
    void shouldNotRemoveGeneralEmailAttachmentsIfDeliveredEmailsWentToDifferentParties() throws Exception {
        final CaseData caseData = caseData();

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocument(2);
        caseData.getDocuments().setDocumentsGenerated(docs);

        List<ListValue<DivorceDocument>> generalEmailAttachments = buildGeneralEmailAttachmentsWithDocLinks("123-456", "000-123");
        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(RESPONDENT)
                .generalEmailDetails("a general email")
                .generalEmailAttachments(generalEmailAttachments)
                .build()
        );

        caseData.setGeneralEmails(
            List.of(
                ListValue.<GeneralEmailDetails>builder().value(
                    GeneralEmailDetails.builder()
                        .generalEmailParties(GeneralParties.APPLICANT)
                        .generalEmailBody("a general email")
                        .generalEmailAttachmentLinks(buildGeneralEmailDetailsAttachmentsWithDocLinks("000-123", "123-456"))
                        .build()
                ).build()
            )
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(details);

        GeneralEmail updatedGeneralEmail = response.getData().getGeneralEmail();
        assertThat(updatedGeneralEmail.getGeneralEmailParties()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailDetails()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailOtherRecipientEmail()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailOtherRecipientName()).isNull();
        assertThat(updatedGeneralEmail.getGeneralEmailAttachments()).isEqualTo(generalEmailAttachments);
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
    void shouldAddSelectedUploadedDocsToAttachmentsListInAboutToSubmit() throws Exception {
        setMockClock(clock);

        final CaseData caseData = caseData();

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocument(1);
        caseData.getDocuments().setDocumentsUploaded(docs);

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .generalEmailAttachments(getListOfDivorceDocument(2))
                .geApplicant1DocumentNames(null)
                .geApplicant2DocumentNames(null)
                .geGeneratedDocumentNames(null)
                .geUploadedDocumentNames(getDummySelectionList(docs.get(0).getId()))
                .geScannedDocumentNames(null)
                .geGeneralOrderDocumentNames(null)
                .geGeneralOrderDocumentNames(null)
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

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

        assertThat(response.getData().getGeneralEmails().get(0).getValue()
            .getGeneralEmailAttachmentLinks().size()).isEqualTo(3);
    }

    @Test
    void shouldAddSelectedGeneratedDocsToAttachmentsListInAboutToSubmit() throws Exception {
        setMockClock(clock);

        final CaseData caseData = caseData();

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocument(1);
        caseData.getDocuments().setDocumentsGenerated(docs);

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .generalEmailAttachments(getListOfDivorceDocument(2))
                .geApplicant1DocumentNames(null)
                .geApplicant2DocumentNames(null)
                .geGeneratedDocumentNames(getDummySelectionList(docs.get(0).getId()))
                .geUploadedDocumentNames(null)
                .geScannedDocumentNames(null)
                .geGeneralOrderDocumentNames(null)
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

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

        assertThat(response.getData().getGeneralEmails().get(0).getValue()
            .getGeneralEmailAttachmentLinks().size()).isEqualTo(3);
    }

    @Test
    void shouldAddSelectedApp1DocsToAttachmentsListInAboutToSubmit() throws Exception {
        setMockClock(clock);

        final CaseData caseData = caseData();

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocument(1);
        caseData.getDocuments().setApplicant1DocumentsUploaded(docs);

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .generalEmailAttachments(getListOfDivorceDocument(2))
                .geApplicant1DocumentNames(getDummySelectionList(docs.get(0).getId()))
                .geApplicant2DocumentNames(null)
                .geGeneratedDocumentNames(null)
                .geUploadedDocumentNames(null)
                .geScannedDocumentNames(null)
                .geGeneralOrderDocumentNames(null)
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

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

        assertThat(response.getData().getGeneralEmails().get(0).getValue()
            .getGeneralEmailAttachmentLinks().size()).isEqualTo(3);
    }

    @Test
    void shouldAddSelectedApp2DocsToAttachmentsListInAboutToSubmit() throws Exception {
        setMockClock(clock);

        final CaseData caseData = caseData();

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocument(1);
        caseData.getDocuments().setApplicant2DocumentsUploaded(docs);

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .generalEmailAttachments(getListOfDivorceDocument(2))
                .geApplicant1DocumentNames(null)
                .geApplicant2DocumentNames(getDummySelectionList(docs.get(0).getId()))
                .geGeneratedDocumentNames(null)
                .geUploadedDocumentNames(null)
                .geScannedDocumentNames(null)
                .geGeneralOrderDocumentNames(null)
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

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

        assertThat(response.getData().getGeneralEmails().get(0).getValue()
            .getGeneralEmailAttachmentLinks().size()).isEqualTo(3);
    }

    @Test
    void shouldAddSelectedScannedDocsToAttachmentsListInAboutToSubmit() throws Exception {
        setMockClock(clock);

        final CaseData caseData = caseData();

        List<ListValue<ScannedDocument>> docs = getListOfScannedDocument(1);
        caseData.getDocuments().setScannedDocuments(docs);

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .generalEmailAttachments(getListOfDivorceDocument(2))
                .geApplicant1DocumentNames(null)
                .geApplicant2DocumentNames(null)
                .geGeneratedDocumentNames(null)
                .geUploadedDocumentNames(null)
                .geScannedDocumentNames(getDummySelectionList(docs.get(0).getId()))
                .geGeneralOrderDocumentNames(null)
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

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

        assertThat(response.getData().getGeneralEmails().get(0).getValue()
            .getGeneralEmailAttachmentLinks().size()).isEqualTo(3);
    }

    @Test
    void shouldAddSelectedGenOrderDocsToAttachmentsListInAboutToSubmit() throws Exception {
        setMockClock(clock);

        final CaseData caseData = caseData();

        String documentUrl = "http://localhost:8080/4567";

        Document generalOrderDoc1 = new Document(
            documentUrl,
            "generalOrder2020-07-16 11:10:34.pdf",
            documentUrl + "/binary"
        );

        final List<ListValue<DivorceGeneralOrder>> generalOrders = new ArrayList<>();
        generalOrders.add(getDivorceGeneralOrderListValue(generalOrderDoc1, UUID.randomUUID().toString()));
        caseData.setGeneralOrders(generalOrders);

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .generalEmailAttachments(getListOfDivorceDocument(2))
                .geApplicant1DocumentNames(null)
                .geApplicant2DocumentNames(null)
                .geGeneratedDocumentNames(null)
                .geUploadedDocumentNames(null)
                .geScannedDocumentNames(null)
                .geGeneralOrderDocumentNames(getDummySelectionList(generalOrders.get(0).getId()))
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

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

        assertThat(response.getData().getGeneralEmails().get(0).getValue()
            .getGeneralEmailAttachmentLinks().size()).isEqualTo(3);
    }

    @Test
    void shouldSendEmailInAboutToSubmit() throws Exception {
        setMockClock(clock);

        final CaseData caseData = caseData();

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .generalEmailAttachments(getListOfDivorceDocument(2))
                .geApplicant1DocumentNames(null)
                .geApplicant2DocumentNames(null)
                .geGeneratedDocumentNames(null)
                .geUploadedDocumentNames(null)
                .geScannedDocumentNames(null)
                .geGeneralOrderDocumentNames(null)
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

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

        var caseDataCopy = caseData.toBuilder().build();

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToSubmit(details, details);

        verify(generalEmailNotification).send(caseDataCopy,TEST_CASE_ID);
    }

    private List<ListValue<DivorceDocument>> getListOfDivorceDocument(int size) {
        List<ListValue<DivorceDocument>> docList = new ArrayList<>();
        while (size > 0) {
            ListValue<DivorceDocument> generalEmailAttachment = new ListValue<>(
                UUID.randomUUID().toString(),
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

    private List<ListValue<ScannedDocument>> getListOfScannedDocument(int size) {
        List<ListValue<ScannedDocument>> docList = new ArrayList<>();
        while (size > 0) {
            ListValue<ScannedDocument> generalEmailAttachment = new ListValue<>(
                UUID.randomUUID().toString(),
                ScannedDocument
                    .builder()
                    .url(Document.builder().build())
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

    DynamicMultiSelectList getDummySelectionList(String code) {
        return DynamicMultiSelectList.builder()
            .listItems(List.of(DynamicListElement.builder()
                .label("Test")
                .code(UUID.fromString(code))
                .build()))
            .value(List.of(DynamicListElement.builder()
                .label("Test")
                .code(UUID.fromString(code))
                .build()))
            .build();
    }

    List<ListValue<DivorceDocument>> buildGeneralEmailAttachmentsWithDocLinks(String... docLinks) {
        return Arrays.stream(docLinks).map(link -> {
            var divorceDoc = DivorceDocument.builder().documentLink(Document.builder().url(link).build()).build();
            return ListValue.<DivorceDocument>builder().value(divorceDoc).build();
        }).toList();
    }

    List<ListValue<Document>> buildGeneralEmailDetailsAttachmentsWithDocLinks(String... docLinks) {
        return Arrays.stream(docLinks).map(link -> {
            var document = Document.builder().url(link).build();
            return ListValue.<Document>builder().value(document).build();
        }).toList();
    }
}
