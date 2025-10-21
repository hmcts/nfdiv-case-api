package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.bulkaction.service.ScheduleCaseService;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction.FailedBulkCaseRemover;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.CASEWORKER_SCHEDULE_CASE;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_ALREADY_LINKED_TO_BULK_CASE;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_CASE_ID;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_CASE_IDS_DUPLICATED;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_HEARING_DATE_IN_PAST;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_INVALID_STATE;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_NO_CASES_FOUND;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_NO_NEW_CASES_ADDED;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_ONLY_AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_REMOVE_DUPLICATES;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemLinkWithBulkCase.SYSTEM_LINK_WITH_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2WithAddress;

@ExtendWith(MockitoExtension.class)
class CaseworkerScheduleCaseTest {

    private static final String BULK_CASE_REFERENCE = "1234123412341234";

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ScheduleCaseService scheduleCaseService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private FailedBulkCaseRemover failedBulkCaseRemover;

    @Mock
    private BulkTriggerService bulkTriggerService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @InjectMocks
    private CaseworkerScheduleCase scheduleCase;

    private User user;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        scheduleCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_SCHEDULE_CASE);
    }

    @Test
    void shouldSuccessfullyUpdateCasesInBulkWithCourtHearingDetails() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().build());
        details.setId(TEST_CASE_ID);

        setUpUser("Random role");

        doNothing().when(scheduleCaseService).updateCourtHearingDetailsForCasesInBulk(details);

        SubmittedCallbackResponse submittedCallbackResponse = scheduleCase.submitted(details, details);

        assertThat(submittedCallbackResponse).isNotNull();
        verify(scheduleCaseService).updateCourtHearingDetailsForCasesInBulk(details);
        verifyNoInteractions(bulkTriggerService);
    }

    @Test
    void shouldNotPopulateErrorMessageWhenHearingDateIsInFutureAndMidEventIsTriggered() {
        setUpSystemUser();

        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(LocalDateTime.now().minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            LocalDateTime.now().plusDays(5),
            List.of(bulkListCaseDetailsListValue())
        );

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = getModelCaseDetails();

        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> searchResults = new ArrayList<>();
        searchResults.add(getModelCaseDetails());

        when(ccdSearchService.searchForCases(List.of(TEST_CASE_ID.toString()), user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(getMappedCaseData());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldPopulateErrorMessageWhenHearingDateIsInPastAndMidEventIsTriggered() {
        setUpSystemUser();

        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(LocalDateTime.now().minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            LocalDateTime.now().minusHours(5),
            List.of(bulkListCaseDetailsListValue())
        );

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = getModelCaseDetails();

        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> searchResults = new ArrayList<>();
        searchResults.add(getModelCaseDetails());

        when(ccdSearchService.searchForCases(List.of(TEST_CASE_ID.toString()), user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(getMappedCaseData());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(ERROR_HEARING_DATE_IN_PAST);
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseAddedTwiceAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(LocalDateTime.now().minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            LocalDateTime.now().plusDays(5),
            List.of(bulkListCaseDetailsListValue(), bulkListCaseDetailsListValue())
        );

        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> searchResults = new ArrayList<>();
        searchResults.add(getModelCaseDetails());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(ERROR_CASE_IDS_DUPLICATED + TEST_CASE_ID, ERROR_REMOVE_DUPLICATES);
    }

    @Test
    void shouldPopulateErrorMessagesWhenHearingDateIsInPastAndCaseAddedTwiceAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(LocalDateTime.now().minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            LocalDateTime.now().minusHours(5),
            List.of(bulkListCaseDetailsListValue(), bulkListCaseDetailsListValue())
        );

        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> searchResults = new ArrayList<>();
        searchResults.add(getModelCaseDetails());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(
            ERROR_HEARING_DATE_IN_PAST,
            ERROR_CASE_IDS_DUPLICATED + TEST_CASE_ID,
            ERROR_REMOVE_DUPLICATES
        );
    }

    @Test
    void shouldPopulateErrorMessageWhenNoNewCasesAddedForListingAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(
            LocalDateTime.now().minusDays(5),
            List.of(bulkListCaseDetailsListValue())
        );
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            LocalDateTime.now().plusDays(5),
            List.of(bulkListCaseDetailsListValue())
        );

        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> searchResults = new ArrayList<>();
        searchResults.add(getModelCaseDetails());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(ERROR_NO_NEW_CASES_ADDED);
    }

    @Test
    void shouldPopulateErrorMessagesWhenHearingDateIsInPastAndNoNewCasesAddedForListingAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(
            LocalDateTime.now().minusDays(5),
            List.of(bulkListCaseDetailsListValue())
        );
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            LocalDateTime.now().minusHours(5),
            List.of(bulkListCaseDetailsListValue())
        );

        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> searchResults = new ArrayList<>();
        searchResults.add(getModelCaseDetails());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(
            ERROR_HEARING_DATE_IN_PAST,
            ERROR_NO_NEW_CASES_ADDED
        );
    }

    @Test
    void shouldPopulateErrorMessageWhenNewCaseAddedForListingAlreadyLinkedToBulkCaseAndMidEventIsTriggered() {
        setUpSystemUser();

        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(LocalDateTime.now().minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            LocalDateTime.now().plusDays(5),
            List.of(bulkListCaseDetailsListValue())
        );

        final CaseLink caseLink = CaseLink.builder().caseReference(BULK_CASE_REFERENCE).build();
        final Map<String, Object> caseData = getModelCaseData();
        caseData.put("bulkListCaseReferenceLink", caseLink);

        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> searchResults = new ArrayList<>();
        searchResults.add(getModelCaseDetails(caseData));

        when(ccdSearchService.searchForCases(List.of(TEST_CASE_ID.toString()), user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        final CaseData mappedCaseData = getMappedCaseData();
        mappedCaseData.setBulkListCaseReferenceLink(caseLink);

        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(mappedCaseData);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(
            ERROR_CASE_ID + TEST_CASE_ID + ERROR_ALREADY_LINKED_TO_BULK_CASE + BULK_CASE_REFERENCE
        );
    }

    @Test
    void shouldPopulateErrorMessageWhenNewCaseAddedForListingInWrongStateAndMidEventIsTriggered() {
        setUpSystemUser();

        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(LocalDateTime.now().minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            LocalDateTime.now().plusDays(5),
            List.of(bulkListCaseDetailsListValue())
        );

        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> searchResults = new ArrayList<>();
        searchResults.add(getModelCaseDetails(getModelCaseData(), State.Submitted));

        when(ccdSearchService.searchForCases(List.of(TEST_CASE_ID.toString()), user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        final CaseData mappedCaseData = getMappedCaseData();

        when(objectMapper.convertValue(getModelCaseData(), CaseData.class)).thenReturn(mappedCaseData);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(
            ERROR_CASE_ID + TEST_CASE_ID + ERROR_INVALID_STATE + State.Submitted + ERROR_ONLY_AWAITING_PRONOUNCEMENT
        );
    }

    @Test
    void shouldPopulateErrorMessagesWhenHearingDateInPastAndNewCaseAddedForListingLinkedToBulkCaseAndWrongStateAndMidEventIsTriggered() {
        setUpSystemUser();

        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(LocalDateTime.now().minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            LocalDateTime.now().minusHours(5),
            List.of(bulkListCaseDetailsListValue())
        );

        final CaseLink caseLink = CaseLink.builder().caseReference(BULK_CASE_REFERENCE).build();
        final Map<String, Object> caseData = getModelCaseData();
        caseData.put("bulkListCaseReferenceLink", caseLink);

        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> searchResults = new ArrayList<>();
        searchResults.add(getModelCaseDetails(caseData, State.Submitted));

        when(ccdSearchService.searchForCases(List.of(TEST_CASE_ID.toString()), user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        final CaseData mappedCaseData = getMappedCaseData();
        mappedCaseData.setBulkListCaseReferenceLink(caseLink);

        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(mappedCaseData);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(
            ERROR_HEARING_DATE_IN_PAST,
            ERROR_CASE_ID + TEST_CASE_ID + ERROR_INVALID_STATE + State.Submitted + ERROR_ONLY_AWAITING_PRONOUNCEMENT,
            ERROR_CASE_ID + TEST_CASE_ID + ERROR_ALREADY_LINKED_TO_BULK_CASE + BULK_CASE_REFERENCE
        );
    }

    @Test
    void shouldPopulateErrorMessageWhenSearchReturnsNoResultsAndMidEventIsTriggered() {
        setUpSystemUser();

        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(LocalDateTime.now().minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            LocalDateTime.now().plusDays(5),
            List.of(bulkListCaseDetailsListValue())
        );

        when(ccdSearchService.searchForCases(List.of(TEST_CASE_ID.toString()), user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(new ArrayList<>());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(ERROR_NO_CASES_FOUND + TEST_CASE_ID);
    }

    @Test
    void shouldPopulateErrorMessagesWhenHearingDateIsInPastAndSearchReturnsNoResultsAndMidEventIsTriggered() {
        setUpSystemUser();

        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(LocalDateTime.now().minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            LocalDateTime.now().minusHours(5),
            List.of(bulkListCaseDetailsListValue())
        );

        when(ccdSearchService.searchForCases(List.of(TEST_CASE_ID.toString()), user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(new ArrayList<>());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(
            ERROR_HEARING_DATE_IN_PAST,
            ERROR_NO_CASES_FOUND + TEST_CASE_ID
        );
    }

    @Test
    void shouldSuccessfullyLinkCaseWithBulkListWhenRoleIsCaseWorker() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().build());
        details.setId(TEST_CASE_ID);

        User user =  setUpUser(CASE_WORKER.getRole());

        var userDetails = UserInfo.builder().uid(SYSTEM_USER_USER_ID).build();
        User systemUser = new User(SYSTEM_UPDATE_AUTH_TOKEN, userDetails);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);

        doNothing().when(scheduleCaseService).updateCourtHearingDetailsForCasesInBulk(details);

        SubmittedCallbackResponse submittedCallbackResponse = scheduleCase.submitted(details, details);

        assertThat(submittedCallbackResponse).isNotNull();
        verify(scheduleCaseService).updateCourtHearingDetailsForCasesInBulk(details);
        verify(bulkTriggerService).bulkTrigger(
                details.getData().getBulkListCaseDetails(),
                SYSTEM_LINK_WITH_BULK_CASE,
                bulkCaseCaseTaskFactory.getCaseTask(details, SYSTEM_LINK_WITH_BULK_CASE),
                systemUser,
                TEST_SERVICE_AUTH_TOKEN);

        verify(failedBulkCaseRemover).removeFailedCasesFromBulkListCaseDetails(
            any(), eq(details), eq(systemUser), eq(TEST_SERVICE_AUTH_TOKEN)
        );
    }

    private User setUpUser(String userRole) {
        var userDetails = UserInfo.builder().roles(List.of(userRole)).build();
        User user = new User(AUTH_HEADER_VALUE, userDetails);
        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN))
                .thenReturn(user);

        return user;
    }

    void setUpSystemUser() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
    }

    private ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue() {
        final BulkListCaseDetails bulkCaseDetails = BulkListCaseDetails
            .builder()
            .caseParties(TEST_FIRST_NAME + " " + TEST_LAST_NAME + " vs " + TEST_APP2_FIRST_NAME + " " + TEST_APP2_LAST_NAME)
            .caseReference(
                CaseLink
                    .builder()
                    .caseReference(String.valueOf(TEST_CASE_ID))
                    .build()
            )
            .build();

        return
            ListValue
                .<BulkListCaseDetails>builder()
                .value(bulkCaseDetails)
                .build();
    }

    private CaseDetails<BulkActionCaseData, BulkActionState> getBulkCaseDetails(LocalDateTime dateAndTimeOfHearing) {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(dateAndTimeOfHearing)
            .build()
        );
        details.setId(TEST_CASE_ID);
        details.setState(Listed);
        return details;
    }

    private CaseDetails<BulkActionCaseData, BulkActionState> getBulkCaseDetails(
        LocalDateTime dateAndTimeOfHearing,
        List<ListValue<BulkListCaseDetails>> bulkListCaseDetails
    ) {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(dateAndTimeOfHearing);
        details.getData().setBulkListCaseDetails(bulkListCaseDetails);
        return details;
    }

    private Map<String, Object> getModelCaseData() {
        final Map<String, Object> caseData = new HashMap<>();
        caseData.put("applicant1", getApplicant());
        caseData.put("applicant2", getApplicant2WithAddress(MALE));

        return caseData;
    }

    private uk.gov.hmcts.reform.ccd.client.model.CaseDetails getModelCaseDetails(Map<String, Object> caseData, State state) {
        return uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                .id(TEST_CASE_ID)
                .state(state.toString())
                .data(caseData)
                .build();
    }

    private uk.gov.hmcts.reform.ccd.client.model.CaseDetails getModelCaseDetails(Map<String, Object> caseData) {
        return getModelCaseDetails(caseData, AwaitingPronouncement);
    }

    private uk.gov.hmcts.reform.ccd.client.model.CaseDetails getModelCaseDetails() {
        return getModelCaseDetails(getModelCaseData());
    }

    private CaseData getMappedCaseData() {
        final CaseData mappedCaseData = caseData();
        mappedCaseData.setApplicant2(getApplicant2WithAddress(MALE));
        return mappedCaseData;
    }
}
