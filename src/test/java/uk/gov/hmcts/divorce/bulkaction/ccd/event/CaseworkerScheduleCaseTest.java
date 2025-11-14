package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
import uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction.FailedBulkCaseRemover;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
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
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_CASES_NOT_FOUND;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_CASE_ID;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_CASE_IDS_DUPLICATED;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_HEARING_DATE_IN_PAST;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_INVALID_STATE;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_NO_CASES_FOUND;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_NO_NEW_CASES_ADDED_OR_HEARING_DETAILS_UPDATED;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.ERROR_ONLY_AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BIRMINGHAM;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.BULK_LIST_ERRORED_CASES;
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

    private static final Long BULK_CASE_REFERENCE = 1234123412341234L;
    private static final Long BULK_CASE_REFERENCE_2 = 2345234523452345L;
    private static final Long TEST_CASE_ID_2 = 2L;
    private static final Long TEST_CASE_ID_3 = 3L;
    private static final LocalDateTime NOW = LocalDateTime.now();

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

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        scheduleCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_SCHEDULE_CASE);
    }

    @Test
    void shouldValidateAndErrorIfCasesHaveErrorInBulkList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(NOW);
        details.setData(BulkActionCaseData.builder().erroredCaseDetails(List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))).build());

        try (MockedStatic<ValidationUtil> classMock = Mockito.mockStatic(ValidationUtil.class)) {
            classMock.when(() -> ValidationUtil.validateBulkListErroredCases(details))
                .thenReturn(List.of(BULK_LIST_ERRORED_CASES));
        }

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response =
            scheduleCase.aboutToStart(details);

        assertThat(response.getErrors().getFirst()).isEqualTo(BULK_LIST_ERRORED_CASES);
    }

    @Test
    void shouldNotPopulateErrorMessageWhenHearingDateIsInFutureAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );

        setupSearchMock(getModelCaseDetails(getModelCaseData(), AwaitingPronouncement));

        setupObjectMapperMock(getModelCaseData(), getMappedCaseData());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotPopulateErrorMessageWhenHearingDateNotChangedNoNewCasesListedAndCourtChangedAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );
        details.getData().setCourt(BURY_ST_EDMUNDS);

        setupSearchMock(getModelCaseDetails(getModelCaseData(), AwaitingPronouncement));

        setupObjectMapperMock(getModelCaseData(), getMappedCaseData());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldPopulateErrorMessageWhenHearingDateIsInPastAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.minusHours(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );

        setupSearchMock(getModelCaseDetails(getModelCaseData(), AwaitingPronouncement));

        setupObjectMapperMock(getModelCaseData(), getMappedCaseData());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(ERROR_HEARING_DATE_IN_PAST);
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseAddedTwiceAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID), bulkListCaseDetailsListValue(TEST_CASE_ID))
        );

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(ERROR_CASE_IDS_DUPLICATED + TEST_CASE_ID);
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseAddedTwiceWithDifferentPartiesAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final ListValue<BulkListCaseDetails> updatedBulkListCaseDetails = bulkListCaseDetailsListValue(TEST_CASE_ID);
        updatedBulkListCaseDetails.getValue().setCaseParties("Different Parties");
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID), updatedBulkListCaseDetails)
        );

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(ERROR_CASE_IDS_DUPLICATED + TEST_CASE_ID);
    }

    @Test
    void shouldPopulateErrorMessageWhenCaseAddedTwiceWithDifferentDecisionDateAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final ListValue<BulkListCaseDetails> updatedBulkListCaseDetails = bulkListCaseDetailsListValue(TEST_CASE_ID);
        updatedBulkListCaseDetails.getValue().setDecisionDate(LocalDate.now().minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID), updatedBulkListCaseDetails)
        );

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(ERROR_CASE_IDS_DUPLICATED + TEST_CASE_ID);
    }

    @Test
    void shouldNotPopulateErrorMessageWhenCasePartiesUpdatedAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(
            NOW.minusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );
        final ListValue<BulkListCaseDetails> updatedBulkListCaseDetails = bulkListCaseDetailsListValue(TEST_CASE_ID);
        updatedBulkListCaseDetails.getValue().setCaseParties("Updated Parties");
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(updatedBulkListCaseDetails)
        );

        setupSearchMock(getModelCaseDetails(getModelCaseData(), AwaitingPronouncement));

        setupObjectMapperMock(getModelCaseData(), getMappedCaseData());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotPopulateErrorMessageWhenDecisionDateUpdatedAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(
            NOW.minusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );
        final ListValue<BulkListCaseDetails> updatedBulkListCaseDetails = bulkListCaseDetailsListValue(TEST_CASE_ID);
        updatedBulkListCaseDetails.getValue().setDecisionDate(LocalDate.now());
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(updatedBulkListCaseDetails)
        );

        setupSearchMock(getModelCaseDetails(getModelCaseData(), AwaitingPronouncement));

        setupObjectMapperMock(getModelCaseData(), getMappedCaseData());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldPopulateErrorMessagesWhenHearingDateIsInPastAndCaseAddedTwiceAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.minusHours(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID), bulkListCaseDetailsListValue(TEST_CASE_ID))
        );

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(
            ERROR_HEARING_DATE_IN_PAST,
            ERROR_CASE_IDS_DUPLICATED + TEST_CASE_ID
        );
    }

//    @Test
//    void shouldPopulateErrorMessageWhenNoNewCasesAddedAndHearingDetailsUnchangedAndMidEventIsTriggered() {
//        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
//            NOW.plusDays(5),
//            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
//        );
//
//        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, details);
//
//        assertThat(response.getErrors()).containsExactly(ERROR_NO_NEW_CASES_ADDED_OR_HEARING_DETAILS_UPDATED);
//    }

    @Test
    void shouldPopulateErrorMessagesWhenHearingDateIsInPastAndNoNewCasesAddedAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(
            NOW.minusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.minusHours(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );

        setupSearchMock(getModelCaseDetails(getModelCaseData(), AwaitingPronouncement));

        setupObjectMapperMock(getModelCaseData(), getMappedCaseData());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(
            ERROR_HEARING_DATE_IN_PAST
        );
    }

    @Test
    void shouldPopulateErrorMessageWhenNewCaseAddedForListingAlreadyLinkedToBulkCaseAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );

        final Map<String, Object> caseData = getModelCaseDataWithCaseLink(BULK_CASE_REFERENCE_2);

        setupSearchMock(getModelCaseDetails(caseData, AwaitingPronouncement));

        setupObjectMapperMock(caseData, getMappedCaseDataWithCaseLink(BULK_CASE_REFERENCE_2));

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(
            ERROR_CASE_ID + TEST_CASE_ID + ERROR_ALREADY_LINKED_TO_BULK_CASE + BULK_CASE_REFERENCE_2
        );
    }

    @Test
    void shouldPopulateErrorMessageWhenNewCaseAddedForListingInWrongStateAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = getModelCaseDetails(getModelCaseData(), Submitted);

        setupSearchMock(caseDetails);

        setupObjectMapperMock(caseDetails.getData(), getMappedCaseData());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(
            ERROR_CASE_ID + TEST_CASE_ID + ERROR_INVALID_STATE + Submitted + ERROR_ONLY_AWAITING_PRONOUNCEMENT
        );
    }

    @Test
    void shouldPopulateErrorMessagesWhenHearingDateInPastAndNewCaseAddedForListingLinkedToBulkCaseAndWrongStateAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));

        final ListValue<BulkListCaseDetails> bulkListCaseValue2 = bulkListCaseDetailsListValue(TEST_CASE_ID_2);
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.minusHours(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID), bulkListCaseValue2, bulkListCaseValue2)
        );

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = getModelCaseDetails(getModelCaseDataWithCaseLink(BULK_CASE_REFERENCE), Submitted);

        setupSearchMock(caseDetails);

        setupObjectMapperMock(caseDetails.getData(), getMappedCaseDataWithCaseLink(BULK_CASE_REFERENCE_2));

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(
            ERROR_HEARING_DATE_IN_PAST,
            ERROR_CASE_IDS_DUPLICATED + TEST_CASE_ID_2,
            ERROR_CASE_ID + TEST_CASE_ID + ERROR_INVALID_STATE + Submitted + ERROR_ONLY_AWAITING_PRONOUNCEMENT,
            ERROR_CASE_ID + TEST_CASE_ID + ERROR_ALREADY_LINKED_TO_BULK_CASE + BULK_CASE_REFERENCE_2
        );
    }

    @Test
    void shouldPopulateErrorMessageWhenSearchReturnsNoResultsAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );

        setupSearchMock(List.of(TEST_CASE_ID.toString()), new ArrayList<>());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(ERROR_NO_CASES_FOUND + TEST_CASE_ID);
    }

    @Test
    void shouldPopulateErrorMessageWhenSearchReturnsMissingResultsAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.plusDays(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID), bulkListCaseDetailsListValue(TEST_CASE_ID_2))
        );

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = getModelCaseDetails(getModelCaseData(), AwaitingPronouncement);

        setupSearchMock(List.of(TEST_CASE_ID.toString(), TEST_CASE_ID_2.toString()), getSearchResults(caseDetails));

        setupObjectMapperMock(caseDetails.getData(), getMappedCaseData());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(ERROR_CASES_NOT_FOUND + TEST_CASE_ID_2);
    }

    @Test
    void shouldPopulateErrorMessagesWhenHearingDateIsInPastAndSearchReturnsNoResultsAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.minusHours(5),
            List.of(bulkListCaseDetailsListValue(TEST_CASE_ID))
        );

        setupSearchMock(List.of(TEST_CASE_ID.toString()), new ArrayList<>());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(
            ERROR_HEARING_DATE_IN_PAST,
            ERROR_NO_CASES_FOUND + TEST_CASE_ID
        );
    }

    @Test
    void shouldPopulateErrorMessagesWhenHearingDateInPastAndDupesAndSearchMissingResultsAndWrongStateAndLinkedAndMidEventIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails = getBulkCaseDetails(NOW.minusDays(5));
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkCaseDetails(
            NOW.minusHours(5),
            List.of(
                bulkListCaseDetailsListValue(TEST_CASE_ID),
                bulkListCaseDetailsListValue(TEST_CASE_ID_2),
                bulkListCaseDetailsListValue(TEST_CASE_ID_3),
                bulkListCaseDetailsListValue(TEST_CASE_ID_3)
            )
        );

        final uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = getModelCaseDetails(getModelCaseData(), Submitted);

        setupSearchMock(List.of(TEST_CASE_ID.toString(), TEST_CASE_ID_2.toString()), getSearchResults(caseDetails));

        setupObjectMapperMock(caseDetails.getData(), getMappedCaseDataWithCaseLink(BULK_CASE_REFERENCE_2));

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly(
            ERROR_HEARING_DATE_IN_PAST,
            ERROR_CASE_IDS_DUPLICATED + TEST_CASE_ID_3,
            ERROR_CASES_NOT_FOUND + TEST_CASE_ID_2,
            ERROR_CASE_ID + TEST_CASE_ID + ERROR_INVALID_STATE + Submitted + ERROR_ONLY_AWAITING_PRONOUNCEMENT,
            ERROR_CASE_ID + TEST_CASE_ID + ERROR_ALREADY_LINKED_TO_BULK_CASE + BULK_CASE_REFERENCE_2
        );
    }

    @Test
    void shouldSuccessfullyUpdateCasesInBulkWithCourtHearingDetails() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().build());
        details.setId(TEST_CASE_ID);

        setupUserAuthMocks(CITIZEN);

        doNothing().when(scheduleCaseService).updateCourtHearingDetailsForCasesInBulk(details);

        SubmittedCallbackResponse submittedCallbackResponse = scheduleCase.submitted(details, details);

        assertThat(submittedCallbackResponse).isNotNull();
        verify(scheduleCaseService).updateCourtHearingDetailsForCasesInBulk(details);
        verifyNoInteractions(bulkTriggerService);
    }

    @Test
    void shouldSuccessfullyLinkCaseWithBulkListWhenRoleIsCaseWorker() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().build());
        details.setId(TEST_CASE_ID);

        setupUserAuthMocks(CASE_WORKER);
        final User systemUser = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().uid(SYSTEM_USER_USER_ID).build());
        setupSystemAuthMocks(systemUser);

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

    private void setupUserAuthMocks(UserRole role) {
        final User user = new User(AUTH_HEADER_VALUE, UserInfo.builder().roles(List.of(role.getRole())).build());
        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(user);
    }

    private void setupSystemAuthMocks(User systemUser) {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
    }

    private ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue(Long caseId) {
        final BulkListCaseDetails bulkCaseDetails = BulkListCaseDetails
            .builder()
            .caseParties(TEST_FIRST_NAME + " " + TEST_LAST_NAME + " vs " + TEST_APP2_FIRST_NAME + " " + TEST_APP2_LAST_NAME)
            .caseReference(CaseLink.builder().caseReference(String.valueOf(caseId)).build())
            .decisionDate(LocalDate.now().minusDays(10))
            .build();
        return ListValue.<BulkListCaseDetails>builder().value(bulkCaseDetails).build();
    }

    private CaseDetails<BulkActionCaseData, BulkActionState> getBulkCaseDetails(LocalDateTime dateAndTimeOfHearing) {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().dateAndTimeOfHearing(dateAndTimeOfHearing).court(BIRMINGHAM).build());
        details.setState(Listed);
        details.setId(BULK_CASE_REFERENCE);
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

    private Map<String, Object> getModelCaseDataWithCaseLink(Long bulkCaseReference) {
        final Map<String, Object> caseData = getModelCaseData();
        caseData.put("bulkListCaseReferenceLink", CaseLink.builder().caseReference(bulkCaseReference.toString()).build());
        return caseData;
    }


    private uk.gov.hmcts.reform.ccd.client.model.CaseDetails getModelCaseDetails(Map<String, Object> caseData, State state) {
        return uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                .id(TEST_CASE_ID)
                .state(state.toString())
                .data(caseData)
                .build();
    }

    private CaseData getMappedCaseData() {
        final CaseData mappedCaseData = caseData();
        mappedCaseData.setApplicant2(getApplicant2WithAddress(MALE));
        return mappedCaseData;
    }

    private CaseData getMappedCaseDataWithCaseLink(Long bulkCaseReference) {
        final CaseData mappedCaseData = getMappedCaseData();
        mappedCaseData.setBulkListCaseReferenceLink(CaseLink.builder().caseReference(bulkCaseReference.toString()).build());
        return mappedCaseData;
    }

    private List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> getSearchResults(
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails modelCaseDetails
    ) {
        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> searchResults = new ArrayList<>();
        searchResults.add(modelCaseDetails);
        return searchResults;
    }

    private void setupSearchMock(List<String> caseIds, List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> searchResults) {
        final User systemUser = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().uid(SYSTEM_USER_USER_ID).build());
        setupSystemAuthMocks(systemUser);
        when(ccdSearchService.searchForCases(caseIds, systemUser, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);
    }

    private void setupSearchMock(uk.gov.hmcts.reform.ccd.client.model.CaseDetails modelCaseDetails) {
        setupSearchMock(List.of(String.valueOf(TEST_CASE_ID)), getSearchResults(modelCaseDetails));
    }

    private void setupObjectMapperMock(Map<String, Object> caseData, CaseData mappedCaseData) {
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(mappedCaseData);
    }
}
