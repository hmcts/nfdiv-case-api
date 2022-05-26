package uk.gov.hmcts.divorce.caseworker.event;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.service.notification.GeneralEmailNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralEmail;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralEmailDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.testutil.ConfigTestUtil;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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
    void shouldSetGeneralEmailDetailsAndSendEmailNotificationWhenExistingGeneralEmailsIsNull() {
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
                    TEST_AUTHORIZATION_TOKEN, UserDetails
                    .builder()
                    .forename("forename")
                    .surname("lastname")
                    .build()
                )
            );

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToSubmit(details, details);

        assertThat(response.getData().getGeneralEmails())
            .extracting("value")
            .extracting("generalEmailDateTime", "generalEmailParties", "generalEmailCreatedBy", "generalEmailBody")
            .contains(tuple(getExpectedLocalDateTime(), APPLICANT, "forename lastname", "some details"));

        verify(generalEmailNotification).send(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldAddToTopOfExistingGeneralEmailsAndSendEmailNotificationWhenThereIsExistingGeneralEmail() {
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
                    TEST_AUTHORIZATION_TOKEN, UserDetails
                    .builder()
                    .forename("forename")
                    .surname("lastname")
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

        verify(generalEmailNotification).send(caseData, TEST_CASE_ID);
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
}
