package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.notification.NoticeOfProceedingsNotification;
import uk.gov.hmcts.divorce.caseworker.service.notification.PersonalServiceNotification;
import uk.gov.hmcts.divorce.caseworker.service.print.AosPackPrinter;
import uk.gov.hmcts.divorce.common.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueAos.CASEWORKER_ISSUE_AOS;
import static uk.gov.hmcts.divorce.common.model.SolServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.common.model.SolServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.common.model.State.Issued;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondentWithDigitalSolicitor;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueAosTest {

    private static final Instant NOW = Instant.now();
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private static final long DUE_DATE_OFFSET_DAYS = 30L;

    @Mock
    private PersonalServiceNotification personalServiceNotification;

    @Mock
    private NoticeOfProceedingsNotification noticeOfProceedingsNotification;

    @Mock
    private AosPackPrinter aosPackPrinter;

    @Mock
    private Clock clock;

    @InjectMocks
    private CaseworkerIssueAos caseworkerIssueAos;

    @BeforeEach
    void setPageSize() {
        setField(caseworkerIssueAos, "dueDateOffsetDays", DUE_DATE_OFFSET_DAYS);
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        caseworkerIssueAos.configure(configBuilder);

        assertThat(configBuilder.getEvents())
            .extracting(Event::getId)
            .contains(CASEWORKER_ISSUE_AOS);
    }

    @Test
    void shouldDoNothingIfApplicationIsPersonalServiceMethodWhenAboutToSubmit() {

        final var caseData = caseData();
        caseData.getApplication().setSolServiceMethod(PERSONAL_SERVICE);
        final var caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .state(AwaitingAos)
            .data(caseData)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerIssueAos.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getDueDate()).isNull();
        assertThat(response.getData().getAcknowledgementOfService())
            .extracting(
                AcknowledgementOfService::getDigitalNoticeOfProceedings,
                AcknowledgementOfService::getNoticeOfProceedingsEmail,
                AcknowledgementOfService::getNoticeOfProceedingsSolicitorFirm)
            .contains(null, null, null);
        verifyNoInteractions(aosPackPrinter, clock);
    }

    @Test
    void shouldPrintAosAndSetDueDateIfNotPersonalServiceAndRespondentIsNotRepresented() {

        final var caseData = caseData();
        caseData.getApplication().setSolServiceMethod(COURT_SERVICE);
        caseData.setApplicant2(respondent());
        final var caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .state(AwaitingAos)
            .data(caseData)
            .build();

        setClock();
        final LocalDate expectedDueDate = LocalDate.ofInstant(NOW, ZONE_ID).plusDays(DUE_DATE_OFFSET_DAYS);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerIssueAos.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getDueDate()).isEqualTo(expectedDueDate);
        assertThat(response.getData().getAcknowledgementOfService())
            .extracting(
                AcknowledgementOfService::getDigitalNoticeOfProceedings,
                AcknowledgementOfService::getNoticeOfProceedingsEmail,
                AcknowledgementOfService::getNoticeOfProceedingsSolicitorFirm)
            .contains(null, null, null);
        verify(aosPackPrinter).print(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldPrintAosAndUpdateCaseDataIfNotPersonalServiceAndRespondentIsRepresented() {

        final var caseData = caseData();
        caseData.getApplication().setSolServiceMethod(COURT_SERVICE);
        caseData.setApplicant2(respondentWithDigitalSolicitor());
        final var caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .state(AwaitingAos)
            .data(caseData)
            .build();

        setClock();
        final LocalDate expectedDueDate = LocalDate.ofInstant(NOW, ZONE_ID).plusDays(DUE_DATE_OFFSET_DAYS);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerIssueAos.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getDueDate()).isEqualTo(expectedDueDate);
        assertThat(response.getData().getAcknowledgementOfService())
            .extracting(
                AcknowledgementOfService::getDigitalNoticeOfProceedings,
                AcknowledgementOfService::getNoticeOfProceedingsEmail,
                AcknowledgementOfService::getNoticeOfProceedingsSolicitorFirm)
            .contains(YES, TEST_SOLICITOR_EMAIL, TEST_ORG_NAME);
        verify(aosPackPrinter).print(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendNoticeOfProceedingsWhenSubmitted() {

        final var caseData = caseData();
        final var caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .state(Issued)
            .data(caseData)
            .build();

        final SubmittedCallbackResponse submitted = caseworkerIssueAos.submitted(caseDetails, null);

        assertThat(submitted).isNotNull();
        verify(noticeOfProceedingsNotification).send(caseData, TEST_CASE_ID);
        verifyNoInteractions(personalServiceNotification);
    }

    @Test
    void shouldSendPersonalServiceNotificationWhenSubmitted() {

        final var caseData = caseData();
        caseData.getApplication().setSolServiceMethod(PERSONAL_SERVICE);

        final var caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .state(Issued)
            .data(caseData)
            .build();

        final SubmittedCallbackResponse submitted = caseworkerIssueAos.submitted(caseDetails, null);

        assertThat(submitted).isNotNull();
        verify(personalServiceNotification).send(caseData, TEST_CASE_ID);
        verifyNoInteractions(noticeOfProceedingsNotification);
    }

    private void setClock() {
        when(clock.instant()).thenReturn(NOW);
        when(clock.getZone()).thenReturn(ZONE_ID);
    }
}
