package uk.gov.hmcts.divorce.caseworker.event;

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
import uk.gov.hmcts.divorce.caseworker.model.CaseNote;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAddNote.CASEWORKER_ADD_NOTE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerAddNoteTest {

    @Mock
    private IdamService idamService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Clock clock;

    @InjectMocks
    private CaseworkerAddNote caseworkerAddNote;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        caseworkerAddNote.configure(configBuilder);

        assertThat(configBuilder.getEvents())
            .extracting(Event::getId)
            .contains(CASEWORKER_ADD_NOTE);
    }

    @Test
    public void shouldSuccessfullyAddCaseNoteToCaseDataWhenThereAreNoExistingCaseNotes() {
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note");

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final var instant = Instant.now();
        final var zoneId = ZoneId.systemDefault();
        final var expectedDate = LocalDate.ofInstant(instant, zoneId);

        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(zoneId);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);

        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(getCaseworkerUser());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerAddNote.aboutToSubmit(updatedCaseDetails, CaseDetails.<CaseData, State>builder().build());

        assertThat(response.getData().getNotes())
            .extracting("id", "value.author", "value.note")
            .contains(tuple("1", "testFname testSname", "This is a test note"));

        assertThat(response.getData().getNotes())
            .extracting("value.date", LocalDate.class)
            .allMatch(localDate -> localDate.isEqual(expectedDate));

        assertThat(response.getData().getNote()).isNull();

        verify(httpServletRequest).getHeader(AUTHORIZATION);
        verify(idamService).retrieveUser(TEST_AUTHORIZATION_TOKEN);
        verifyNoMoreInteractions(httpServletRequest, idamService);
    }

    @Test
    public void shouldSuccessfullyAddCaseNoteToStartOfCaseNotesListWhenThereIsExistingCaseNote() {
        final CaseData caseData = caseData();
        caseData.setNote("This is a test note 2");

        var caseNoteAddedDate = LocalDate.of(2021, 1, 1);

        var notes = new ArrayList<ListValue<CaseNote>>();
        notes.add(ListValue
            .<CaseNote>builder()
            .id("1")
            .value(new CaseNote("TestFirstName TestSurname", caseNoteAddedDate, "This is a test note 1"))
            .build());

        caseData.setNotes(notes);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final var instant = Instant.now();
        final var zoneId = ZoneId.systemDefault();
        final var expectedDate = LocalDate.ofInstant(instant, zoneId);

        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(zoneId);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);

        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(getCaseworkerUser());

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerAddNote.aboutToSubmit(updatedCaseDetails, CaseDetails.<CaseData, State>builder().build());

        assertThat(response.getData().getNotes())
            .extracting("id", "value.author", "value.note")
            .containsExactly(
                tuple("1", "testFname testSname", "This is a test note 2"),
                tuple("2", "TestFirstName TestSurname", "This is a test note 1")

            );

        assertThat(response.getData().getNotes())
            .extracting("value.date", LocalDate.class)
            .containsExactlyInAnyOrder(expectedDate, caseNoteAddedDate);

        assertThat(response.getData().getNote()).isNull();

        verify(httpServletRequest).getHeader(AUTHORIZATION);
        verify(idamService).retrieveUser(TEST_AUTHORIZATION_TOKEN);
        verifyNoMoreInteractions(httpServletRequest, idamService);
    }

    private User getCaseworkerUser() {
        UserDetails userDetails = UserDetails
            .builder()
            .forename("testFname")
            .surname("testSname")
            .build();

        return new User(TEST_AUTHORIZATION_TOKEN, userDetails);
    }
}
