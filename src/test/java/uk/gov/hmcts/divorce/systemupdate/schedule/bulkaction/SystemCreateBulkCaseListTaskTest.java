package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdCreateService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemLinkWithBulkCase.SYSTEM_LINK_WITH_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class SystemCreateBulkCaseListTaskTest {

    private static final String BULK_CASE_REFERENCE = "1234123412341234";

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdCreateService ccdCreateService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private FailedBulkCaseRemover failedBulkCaseRemover;

    @Mock
    private BulkTriggerService bulkTriggerService;

    @Mock
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @InjectMocks
    private SystemCreateBulkCaseListTask systemCreateBulkCaseListTask;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldCreateBulkCaseWithTwoCasesWhenCasesAreInAwaitingPronouncementStateAndMinimumMaximumBatchLimitIsSatisfied() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails<CaseData, State> caseDetails1 = caseDetails(1L, caseDataWithApplicant(1));
        final CaseDetails<CaseData, State> caseDetails2 = caseDetails(2L, caseDataWithApplicant(2));

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1, caseDetails2));
        searchResults.offer(emptyList());

        final ListValue<BulkListCaseDetails> bulkListCaseDetails1 = bulkListCaseDetailsListValue(caseDetails1);
        final ListValue<BulkListCaseDetails> bulkListCaseDetails2 = bulkListCaseDetailsListValue(caseDetails2);
        final List<ListValue<BulkListCaseDetails>> bulkCases = List.of(bulkListCaseDetails1, bulkListCaseDetails2);

        final ListValue<CaseLink> caseAcceptedToHearing1 =
            caseLinkListValue(bulkListCaseDetails1.getValue().getCaseReference(), "1");
        final ListValue<CaseLink> caseAcceptedToHearing2 =
            caseLinkListValue(bulkListCaseDetails2.getValue().getCaseReference(), "2");
        final List<ListValue<CaseLink>> casesAcceptedToHearing = List.of(caseAcceptedToHearing1, caseAcceptedToHearing2);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails =
            bulkActionCaseDetails(bulkCases, casesAcceptedToHearing);

        final CaseDetails<BulkActionCaseData, BulkActionState> createdCaseDetails = bulkActionCaseDetails(bulkCases);
        createdCaseDetails.setId(3L);

        final CaseTask caseTask = mock(CaseTask.class);

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails, user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(createdCaseDetails);

        when(bulkCaseCaseTaskFactory.getCaseTask(createdCaseDetails, SYSTEM_LINK_WITH_BULK_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            bulkCases,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask,
            user,
            TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(emptyList());

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails, user, TEST_SERVICE_AUTH_TOKEN);
        verify(bulkTriggerService).bulkTrigger(
            bulkCases,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask,
            user,
            TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldCreateTwoBulkCaseWhenThereAreMoreThanMaxCasesAvailable() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails<CaseData, State> caseDetails1 = caseDetails(1L, caseDataWithApplicant(1));
        final CaseDetails<CaseData, State> caseDetails2 = caseDetails(2L, caseDataWithApplicant(2));
        final CaseDetails<CaseData, State> caseDetails3 = caseDetails(3L, caseDataWithApplicant(3));
        final CaseDetails<CaseData, State> caseDetails4 = caseDetails(4L, caseDataWithApplicant(4));

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1, caseDetails2));
        searchResults.offer(List.of(caseDetails3, caseDetails4));
        searchResults.offer(emptyList());

        final ListValue<BulkListCaseDetails> bulkListCaseDetails1 = bulkListCaseDetailsListValue(caseDetails1);
        final ListValue<BulkListCaseDetails> bulkListCaseDetails2 = bulkListCaseDetailsListValue(caseDetails2);
        final ListValue<BulkListCaseDetails> bulkListCaseDetails3 = bulkListCaseDetailsListValue(caseDetails3);
        final ListValue<BulkListCaseDetails> bulkListCaseDetails4 = bulkListCaseDetailsListValue(caseDetails4);

        final ListValue<CaseLink> caseAcceptedToHearing1 =
            caseLinkListValue(bulkListCaseDetails1.getValue().getCaseReference(), "1");
        final ListValue<CaseLink> caseAcceptedToHearing2 =
            caseLinkListValue(bulkListCaseDetails2.getValue().getCaseReference(), "2");
        final List<ListValue<CaseLink>> casesAcceptedToHearing1 = List.of(caseAcceptedToHearing1, caseAcceptedToHearing2);

        final ListValue<CaseLink> caseAcceptedToHearing3 =
            caseLinkListValue(bulkListCaseDetails3.getValue().getCaseReference(), "1");
        final ListValue<CaseLink> caseAcceptedToHearing4 =
            caseLinkListValue(bulkListCaseDetails4.getValue().getCaseReference(), "2");
        final List<ListValue<CaseLink>> casesAcceptedToHearing2 = List.of(caseAcceptedToHearing3, caseAcceptedToHearing4);

        final List<ListValue<BulkListCaseDetails>> bulkCases1 = List.of(bulkListCaseDetails1, bulkListCaseDetails2);
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails1 =
            bulkActionCaseDetails(bulkCases1, casesAcceptedToHearing1);
        final CaseDetails<BulkActionCaseData, BulkActionState> createdCaseDetails1 =
            bulkActionCaseDetails(bulkCases1, casesAcceptedToHearing1);
        createdCaseDetails1.setId(5L);

        final List<ListValue<BulkListCaseDetails>> bulkCases2 = List.of(bulkListCaseDetails3, bulkListCaseDetails4);
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails2 =
            bulkActionCaseDetails(bulkCases2, casesAcceptedToHearing2);
        final CaseDetails<BulkActionCaseData, BulkActionState> createdCaseDetails2 =
            bulkActionCaseDetails(bulkCases2, casesAcceptedToHearing2);
        createdCaseDetails2.setId(6L);

        final CaseTask caseTask1 = mock(CaseTask.class);
        final CaseTask caseTask2 = mock(CaseTask.class);

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails1, user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(createdCaseDetails1);

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails2, user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(createdCaseDetails2);

        when(bulkCaseCaseTaskFactory.getCaseTask(createdCaseDetails1, SYSTEM_LINK_WITH_BULK_CASE)).thenReturn(caseTask1);
        when(bulkCaseCaseTaskFactory.getCaseTask(createdCaseDetails2, SYSTEM_LINK_WITH_BULK_CASE)).thenReturn(caseTask2);

        when(bulkTriggerService.bulkTrigger(
            bulkCases1,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask1,
            user,
            TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(emptyList());

        when(bulkTriggerService.bulkTrigger(
            bulkCases2,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask2,
            user,
            TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(emptyList());

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails1, user, TEST_SERVICE_AUTH_TOKEN);
        verify(bulkTriggerService).bulkTrigger(
            bulkCases1,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask1,
            user,
            TEST_SERVICE_AUTH_TOKEN);
        verify(bulkTriggerService).bulkTrigger(
            bulkCases2,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask2,
            user,
            TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldCreateBulkCaseWithTwoCasesWhenInAwaitingPronouncementAndMinMaxBatchLimitOkAndOneCaseAlreadyAssignedToBulkList() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails<CaseData, State> caseDetails1 = caseDetails(1L, caseDataWithApplicant(1));
        final CaseDetails<CaseData, State> caseDetails2 = caseDetails(2L, caseDataWithApplicantAndBulkListRef(2));
        final CaseDetails<CaseData, State> caseDetails3 = caseDetails(3L, caseDataWithApplicant(3));

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1, caseDetails2, caseDetails3));
        searchResults.offer(emptyList());

        final ListValue<BulkListCaseDetails> bulkListCaseDetails1 = bulkListCaseDetailsListValue(caseDetails1);
        final ListValue<BulkListCaseDetails> bulkListCaseDetails3 = bulkListCaseDetailsListValue(caseDetails3);
        final List<ListValue<BulkListCaseDetails>> bulkCases = List.of(bulkListCaseDetails1, bulkListCaseDetails3);

        final ListValue<CaseLink> caseAcceptedToHearing1 =
            caseLinkListValue(bulkListCaseDetails1.getValue().getCaseReference(), "1");
        final ListValue<CaseLink> caseAcceptedToHearing3 =
            caseLinkListValue(bulkListCaseDetails3.getValue().getCaseReference(), "2");
        final List<ListValue<CaseLink>> casesAcceptedToHearing = List.of(caseAcceptedToHearing1, caseAcceptedToHearing3);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails =
            bulkActionCaseDetails(bulkCases, casesAcceptedToHearing);

        final CaseDetails<BulkActionCaseData, BulkActionState> createdCaseDetails = bulkActionCaseDetails(bulkCases);
        createdCaseDetails.setId(4L);

        final CaseTask caseTask = mock(CaseTask.class);

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails, user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(createdCaseDetails);

        when(bulkCaseCaseTaskFactory.getCaseTask(createdCaseDetails, SYSTEM_LINK_WITH_BULK_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            bulkCases,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask,
            user,
            TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(emptyList());

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails, user, TEST_SERVICE_AUTH_TOKEN);
        verify(bulkTriggerService).bulkTrigger(
            bulkCases,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask,
            user,
            TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldCreateBulkCaseWithTwoCasesWhenMinMaxBatchLimitOkAndOneCaseNotInAwaitingPronouncement() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails<CaseData, State> caseDetails1 = caseDetails(1L, caseDataWithApplicant(1));
        final CaseDetails<CaseData, State> caseDetails2 = caseDetailsSubmitted(2L, caseDataWithApplicant(2));
        final CaseDetails<CaseData, State> caseDetails3 = caseDetails(3L, caseDataWithApplicant(3));

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1, caseDetails2, caseDetails3));
        searchResults.offer(emptyList());

        final ListValue<BulkListCaseDetails> bulkListCaseDetails1 = bulkListCaseDetailsListValue(caseDetails1);
        final ListValue<BulkListCaseDetails> bulkListCaseDetails3 = bulkListCaseDetailsListValue(caseDetails3);
        final List<ListValue<BulkListCaseDetails>> bulkCases = List.of(bulkListCaseDetails1, bulkListCaseDetails3);

        final ListValue<CaseLink> caseAcceptedToHearing1 =
            caseLinkListValue(bulkListCaseDetails1.getValue().getCaseReference(), "1");
        final ListValue<CaseLink> caseAcceptedToHearing3 =
            caseLinkListValue(bulkListCaseDetails3.getValue().getCaseReference(), "2");
        final List<ListValue<CaseLink>> casesAcceptedToHearing = List.of(caseAcceptedToHearing1, caseAcceptedToHearing3);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails =
            bulkActionCaseDetails(bulkCases, casesAcceptedToHearing);

        final CaseDetails<BulkActionCaseData, BulkActionState> createdCaseDetails = bulkActionCaseDetails(bulkCases);
        createdCaseDetails.setId(4L);

        final CaseTask caseTask = mock(CaseTask.class);

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails, user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(createdCaseDetails);

        when(bulkCaseCaseTaskFactory.getCaseTask(createdCaseDetails, SYSTEM_LINK_WITH_BULK_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            bulkCases,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask,
            user,
            TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(emptyList());

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails, user, TEST_SERVICE_AUTH_TOKEN);
        verify(bulkTriggerService).bulkTrigger(
            bulkCases,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask,
            user,
            TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldCreateTwoBulkCaseWithTwoCasesWhenInAwaitingPronouncementAndMinMaxBatchLimitOkAndTwoCasesAlreadyAssignedToBulkList() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails<CaseData, State> caseDetails1 = caseDetails(1L, caseDataWithApplicant(1));
        final CaseDetails<CaseData, State> caseDetails2 = caseDetails(2L, caseDataWithApplicantAndBulkListRef(2));
        final CaseDetails<CaseData, State> caseDetails3 = caseDetails(3L, caseDataWithApplicant(3));
        final CaseDetails<CaseData, State> caseDetails4 = caseDetails(4L, caseDataWithApplicant(4));
        final CaseDetails<CaseData, State> caseDetails5 = caseDetails(5L, caseDataWithApplicant(5));
        final CaseDetails<CaseData, State> caseDetails6 = caseDetails(6L, caseDataWithApplicantAndBulkListRef(6));

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1, caseDetails2, caseDetails3));
        searchResults.offer(List.of(caseDetails4, caseDetails5, caseDetails6));
        searchResults.offer(emptyList());

        final ListValue<BulkListCaseDetails> bulkListCaseDetails1 = bulkListCaseDetailsListValue(caseDetails1);
        final ListValue<BulkListCaseDetails> bulkListCaseDetails3 = bulkListCaseDetailsListValue(caseDetails3);
        final ListValue<BulkListCaseDetails> bulkListCaseDetails4 = bulkListCaseDetailsListValue(caseDetails4);
        final ListValue<BulkListCaseDetails> bulkListCaseDetails5 = bulkListCaseDetailsListValue(caseDetails5);
        final List<ListValue<BulkListCaseDetails>> bulkCases1 = List.of(bulkListCaseDetails1, bulkListCaseDetails3);
        final List<ListValue<BulkListCaseDetails>> bulkCases2 = List.of(bulkListCaseDetails4, bulkListCaseDetails5);

        final ListValue<CaseLink> caseAcceptedToHearing1 =
            caseLinkListValue(bulkListCaseDetails1.getValue().getCaseReference(), "1");
        final ListValue<CaseLink> caseAcceptedToHearing3 =
            caseLinkListValue(bulkListCaseDetails3.getValue().getCaseReference(), "2");
        final List<ListValue<CaseLink>> casesAcceptedToHearing1 = List.of(caseAcceptedToHearing1, caseAcceptedToHearing3);

        final ListValue<CaseLink> caseAcceptedToHearing4 =
            caseLinkListValue(bulkListCaseDetails4.getValue().getCaseReference(), "1");
        final ListValue<CaseLink> caseAcceptedToHearing5 =
            caseLinkListValue(bulkListCaseDetails5.getValue().getCaseReference(), "2");
        final List<ListValue<CaseLink>> casesAcceptedToHearing2 = List.of(caseAcceptedToHearing4, caseAcceptedToHearing5);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails1 =
            bulkActionCaseDetails(bulkCases1, casesAcceptedToHearing1);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails2 =
            bulkActionCaseDetails(bulkCases2, casesAcceptedToHearing2);

        final CaseDetails<BulkActionCaseData, BulkActionState> createdCaseDetails1 = bulkActionCaseDetails(bulkCases1);
        createdCaseDetails1.setId(7L);

        final CaseDetails<BulkActionCaseData, BulkActionState> createdCaseDetails2 = bulkActionCaseDetails(bulkCases2);
        createdCaseDetails2.setId(8L);

        final CaseTask caseTask1 = mock(CaseTask.class);
        final CaseTask caseTask2 = mock(CaseTask.class);

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails1, user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(createdCaseDetails1);

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails2, user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(createdCaseDetails2);

        when(bulkCaseCaseTaskFactory.getCaseTask(createdCaseDetails1, SYSTEM_LINK_WITH_BULK_CASE)).thenReturn(caseTask1);
        when(bulkCaseCaseTaskFactory.getCaseTask(createdCaseDetails2, SYSTEM_LINK_WITH_BULK_CASE)).thenReturn(caseTask2);

        when(bulkTriggerService.bulkTrigger(
            bulkCases1,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask1,
            user,
            TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(emptyList());

        when(bulkTriggerService.bulkTrigger(
            bulkCases2,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask2,
            user,
            TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(emptyList());

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails1, user, TEST_SERVICE_AUTH_TOKEN);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails2, user, TEST_SERVICE_AUTH_TOKEN);
        verify(bulkTriggerService).bulkTrigger(
            bulkCases1,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask1,
            user,
            TEST_SERVICE_AUTH_TOKEN);
        verify(bulkTriggerService).bulkTrigger(
            bulkCases2,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask2,
            user,
            TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldCreateTwoBulkCaseWithTwoCasesWhenMinMaxBatchLimitOkAndTwoCasesNotInAwaitingPronouncement() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails<CaseData, State> caseDetails1 = caseDetails(1L, caseDataWithApplicant(1));
        final CaseDetails<CaseData, State> caseDetails2 = caseDetailsSubmitted(2L, caseDataWithApplicant(2));
        final CaseDetails<CaseData, State> caseDetails3 = caseDetails(3L, caseDataWithApplicant(3));
        final CaseDetails<CaseData, State> caseDetails4 = caseDetails(4L, caseDataWithApplicant(4));
        final CaseDetails<CaseData, State> caseDetails5 = caseDetails(5L, caseDataWithApplicant(5));
        final CaseDetails<CaseData, State> caseDetails6 = caseDetailsSubmitted(6L, caseDataWithApplicant(6));

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1, caseDetails2, caseDetails3));
        searchResults.offer(List.of(caseDetails4, caseDetails5, caseDetails6));
        searchResults.offer(emptyList());

        final ListValue<BulkListCaseDetails> bulkListCaseDetails1 = bulkListCaseDetailsListValue(caseDetails1);
        final ListValue<BulkListCaseDetails> bulkListCaseDetails3 = bulkListCaseDetailsListValue(caseDetails3);
        final ListValue<BulkListCaseDetails> bulkListCaseDetails4 = bulkListCaseDetailsListValue(caseDetails4);
        final ListValue<BulkListCaseDetails> bulkListCaseDetails5 = bulkListCaseDetailsListValue(caseDetails5);
        final List<ListValue<BulkListCaseDetails>> bulkCases1 = List.of(bulkListCaseDetails1, bulkListCaseDetails3);
        final List<ListValue<BulkListCaseDetails>> bulkCases2 = List.of(bulkListCaseDetails4, bulkListCaseDetails5);

        final ListValue<CaseLink> caseAcceptedToHearing1 =
            caseLinkListValue(bulkListCaseDetails1.getValue().getCaseReference(), "1");
        final ListValue<CaseLink> caseAcceptedToHearing3 =
            caseLinkListValue(bulkListCaseDetails3.getValue().getCaseReference(), "2");
        final List<ListValue<CaseLink>> casesAcceptedToHearing1 = List.of(caseAcceptedToHearing1, caseAcceptedToHearing3);

        final ListValue<CaseLink> caseAcceptedToHearing4 =
            caseLinkListValue(bulkListCaseDetails4.getValue().getCaseReference(), "1");
        final ListValue<CaseLink> caseAcceptedToHearing5 =
            caseLinkListValue(bulkListCaseDetails5.getValue().getCaseReference(), "2");
        final List<ListValue<CaseLink>> casesAcceptedToHearing2 = List.of(caseAcceptedToHearing4, caseAcceptedToHearing5);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails1 =
            bulkActionCaseDetails(bulkCases1, casesAcceptedToHearing1);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails2 =
            bulkActionCaseDetails(bulkCases2, casesAcceptedToHearing2);

        final CaseDetails<BulkActionCaseData, BulkActionState> createdCaseDetails1 = bulkActionCaseDetails(bulkCases1);
        createdCaseDetails1.setId(7L);

        final CaseDetails<BulkActionCaseData, BulkActionState> createdCaseDetails2 = bulkActionCaseDetails(bulkCases2);
        createdCaseDetails2.setId(8L);

        final CaseTask caseTask1 = mock(CaseTask.class);
        final CaseTask caseTask2 = mock(CaseTask.class);

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails1, user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(createdCaseDetails1);

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails2, user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(createdCaseDetails2);

        when(bulkCaseCaseTaskFactory.getCaseTask(createdCaseDetails1, SYSTEM_LINK_WITH_BULK_CASE)).thenReturn(caseTask1);
        when(bulkCaseCaseTaskFactory.getCaseTask(createdCaseDetails2, SYSTEM_LINK_WITH_BULK_CASE)).thenReturn(caseTask2);

        when(bulkTriggerService.bulkTrigger(
            bulkCases1,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask1,
            user,
            TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(emptyList());

        when(bulkTriggerService.bulkTrigger(
            bulkCases2,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask2,
            user,
            TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(emptyList());

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails1, user, TEST_SERVICE_AUTH_TOKEN);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails2, user, TEST_SERVICE_AUTH_TOKEN);
        verify(bulkTriggerService).bulkTrigger(
            bulkCases1,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask1,
            user,
            TEST_SERVICE_AUTH_TOKEN);
        verify(bulkTriggerService).bulkTrigger(
            bulkCases2,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask2,
            user,
            TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldCreateOneBulkCaseWithTwoCasesWhenInAwaitingPronouncementAndMinMaxBatchLimitOkAndTwoCasesAlreadyAssignedToBulkList() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails<CaseData, State> caseDetails1 = caseDetails(1L, caseDataWithApplicantAndBulkListRef(1));
        final CaseDetails<CaseData, State> caseDetails2 = caseDetails(2L, caseDataWithApplicantAndBulkListRef(2));
        final CaseDetails<CaseData, State> caseDetails3 = caseDetails(3L, caseDataWithApplicant(3));
        final CaseDetails<CaseData, State> caseDetails4 = caseDetails(4L, caseDataWithApplicant(4));

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1, caseDetails2));
        searchResults.offer(List.of(caseDetails3, caseDetails4));
        searchResults.offer(emptyList());

        final ListValue<BulkListCaseDetails> bulkListCaseDetails3 = bulkListCaseDetailsListValue(caseDetails3);
        final ListValue<BulkListCaseDetails> bulkListCaseDetails4 = bulkListCaseDetailsListValue(caseDetails4);
        final List<ListValue<BulkListCaseDetails>> bulkCases = List.of(bulkListCaseDetails3, bulkListCaseDetails4);

        final ListValue<CaseLink> caseAcceptedToHearing3 =
            caseLinkListValue(bulkListCaseDetails3.getValue().getCaseReference(), "1");
        final ListValue<CaseLink> caseAcceptedToHearing4 =
            caseLinkListValue(bulkListCaseDetails4.getValue().getCaseReference(), "2");
        final List<ListValue<CaseLink>> casesAcceptedToHearing = List.of(caseAcceptedToHearing3, caseAcceptedToHearing4);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails =
            bulkActionCaseDetails(bulkCases, casesAcceptedToHearing);

        final CaseDetails<BulkActionCaseData, BulkActionState> createdCaseDetails = bulkActionCaseDetails(bulkCases);
        createdCaseDetails.setId(5L);

        final CaseTask caseTask = mock(CaseTask.class);

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails, user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(createdCaseDetails);

        when(bulkCaseCaseTaskFactory.getCaseTask(createdCaseDetails, SYSTEM_LINK_WITH_BULK_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            bulkCases,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask,
            user,
            TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(emptyList());

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails, user, TEST_SERVICE_AUTH_TOKEN);
        verify(bulkTriggerService).bulkTrigger(
            bulkCases,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask,
            user,
            TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldCreateOneBulkCaseWithTwoCasesWhenMinMaxBatchLimitOkAndTwoCasesNotInAwaitingPronouncement() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails<CaseData, State> caseDetails1 = caseDetailsSubmitted(1L, caseDataWithApplicant(1));
        final CaseDetails<CaseData, State> caseDetails2 = caseDetailsSubmitted(2L, caseDataWithApplicant(2));
        final CaseDetails<CaseData, State> caseDetails3 = caseDetails(3L, caseDataWithApplicant(3));
        final CaseDetails<CaseData, State> caseDetails4 = caseDetails(4L, caseDataWithApplicant(4));

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1, caseDetails2));
        searchResults.offer(List.of(caseDetails3, caseDetails4));
        searchResults.offer(emptyList());

        final ListValue<BulkListCaseDetails> bulkListCaseDetails3 = bulkListCaseDetailsListValue(caseDetails3);
        final ListValue<BulkListCaseDetails> bulkListCaseDetails4 = bulkListCaseDetailsListValue(caseDetails4);
        final List<ListValue<BulkListCaseDetails>> bulkCases = List.of(bulkListCaseDetails3, bulkListCaseDetails4);

        final ListValue<CaseLink> caseAcceptedToHearing3 =
            caseLinkListValue(bulkListCaseDetails3.getValue().getCaseReference(), "1");
        final ListValue<CaseLink> caseAcceptedToHearing4 =
            caseLinkListValue(bulkListCaseDetails4.getValue().getCaseReference(), "2");
        final List<ListValue<CaseLink>> casesAcceptedToHearing = List.of(caseAcceptedToHearing3, caseAcceptedToHearing4);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails =
            bulkActionCaseDetails(bulkCases, casesAcceptedToHearing);

        final CaseDetails<BulkActionCaseData, BulkActionState> createdCaseDetails = bulkActionCaseDetails(bulkCases);
        createdCaseDetails.setId(5L);

        final CaseTask caseTask = mock(CaseTask.class);

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails, user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(createdCaseDetails);

        when(bulkCaseCaseTaskFactory.getCaseTask(createdCaseDetails, SYSTEM_LINK_WITH_BULK_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            bulkCases,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask,
            user,
            TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(emptyList());

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails, user, TEST_SERVICE_AUTH_TOKEN);
        verify(bulkTriggerService).bulkTrigger(
            bulkCases,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask,
            user,
            TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldNotCreateBulkCaseWithWhenCasesInAwaitingPronouncementStateIsLessThanMinimumBatchSize() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails));

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldNotCreateBulkCaseWhenCasesInAwaitingPronouncementIsLessThanMinBatchSizeAfterFilteringForAlreadyLinkedCases() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails<CaseData, State> caseDetails1 = caseDetails(1L, caseDataWithApplicant(1));
        final CaseDetails<CaseData, State> caseDetails2 = caseDetails(2L, caseDataWithApplicantAndBulkListRef(2));

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1, caseDetails2));
        searchResults.offer(emptyList());

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldNotCreateBulkCaseWhenCasesLessThanMinBatchSizeAfterFilteringForValidState() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails<CaseData, State> caseDetails1 = caseDetails(1L, caseDataWithApplicant(1));
        final CaseDetails<CaseData, State> caseDetails2 = caseDetailsSubmitted(2L, caseDataWithApplicant(2));

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1, caseDetails2));
        searchResults.offer(emptyList());

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldNotCreateBulkCaseWhenAllCasesAlreadyLinkedOrNotAwaitingPronouncement() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails<CaseData, State> caseDetails1 = caseDetailsSubmitted(1L, caseDataWithApplicant(1));
        final CaseDetails<CaseData, State> caseDetails2 = caseDetails(2L, caseDataWithApplicantAndBulkListRef(2));

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1, caseDetails2));
        searchResults.offer(emptyList());

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(ccdSearchService);
    }

    @Test
    void shouldRemoveAwaitingPronouncementCaseLinkFromBulkCaseWhenUpdatingBulkCaseReferenceFailsForAwaitingPronouncementCase() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails<CaseData, State> caseDetails1 = caseDetails(1L, caseDataWithApplicant(1));
        final CaseDetails<CaseData, State> caseDetails2 = caseDetails(2L, caseDataWithApplicant(2));

        final Deque<List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1, caseDetails2));
        searchResults.offer(emptyList());

        final ListValue<BulkListCaseDetails> bulkListCaseDetails1 = bulkListCaseDetailsListValue(caseDetails1);
        final ListValue<BulkListCaseDetails> bulkListCaseDetails2 = bulkListCaseDetailsListValue(caseDetails2);
        final List<ListValue<BulkListCaseDetails>> bulkCaseList = new ArrayList<>(asList(bulkListCaseDetails1, bulkListCaseDetails2));

        final ListValue<CaseLink> caseAcceptedToHearing1 =
            caseLinkListValue(bulkListCaseDetails1.getValue().getCaseReference(), "1");
        final ListValue<CaseLink> caseAcceptedToHearing2 =
            caseLinkListValue(bulkListCaseDetails2.getValue().getCaseReference(), "2");

        final List<ListValue<CaseLink>> casesAcceptedToHearing = List.of(caseAcceptedToHearing1, caseAcceptedToHearing2);


        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails =
            bulkActionCaseDetails(bulkCaseList, casesAcceptedToHearing);

        final CaseDetails<BulkActionCaseData, BulkActionState> createdCaseDetails =
            bulkActionCaseDetails(bulkCaseList, casesAcceptedToHearing);
        createdCaseDetails.setId(3L);

        final CaseTask caseTask = mock(CaseTask.class);

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails, user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(createdCaseDetails);

        when(bulkCaseCaseTaskFactory.getCaseTask(createdCaseDetails, SYSTEM_LINK_WITH_BULK_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            bulkCaseList,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask,
            user,
            TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(emptyList());

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails, user, TEST_SERVICE_AUTH_TOKEN);
        verify(bulkTriggerService).bulkTrigger(
            bulkCaseList,
            SYSTEM_LINK_WITH_BULK_CASE,
            caseTask,
            user,
            TEST_SERVICE_AUTH_TOKEN);

        verifyNoMoreInteractions(ccdSearchService, ccdCreateService);
    }

    @Test
    void shouldNotCreateBulkCaseWhenSearchCaseThrowsException() {
        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemCreateBulkCaseListTask.run();

        verifyNoInteractions(ccdCreateService, bulkTriggerService, failedBulkCaseRemover);
    }

    @Test
    void shouldStopProcessingIfBulkCaseCreationThrowsException() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 1);

        final CaseDetails<CaseData, State> caseDetails1 = caseDetails(1L, caseDataWithApplicant(1));

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1));

        final ListValue<BulkListCaseDetails> bulkListCaseDetails1 = bulkListCaseDetailsListValue(caseDetails1);

        final ListValue<CaseLink> caseAcceptedToHearing1 =
            caseLinkListValue(bulkListCaseDetails1.getValue().getCaseReference(), "1");

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails =
            bulkActionCaseDetails(List.of(bulkListCaseDetails1), List.of(caseAcceptedToHearing1));

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(searchResults);

        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "some exception", mock(FeignException.class)))
            .when(ccdCreateService).createBulkCase(bulkActionCaseDetails, user, TEST_SERVICE_AUTH_TOKEN);

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, TEST_SERVICE_AUTH_TOKEN);
    }

    private CaseData caseDataWithApplicant(final String app1FirstName,
                                           final String app1LastName,
                                           final String app2FirstName,
                                           final String app2LastName) {
        return CaseData
            .builder()
            .applicant1(
                Applicant
                    .builder()
                    .firstName(app1FirstName)
                    .lastName(app1LastName)
                    .build()
            )
            .applicant2(
                Applicant
                    .builder()
                    .firstName(app2FirstName)
                    .lastName(app2LastName)
                    .build()
            )
            .build();
    }

    private CaseData caseDataWithApplicant(final int id) {
        return caseDataWithApplicant(
            "app1fname" + id,
            "app1lname" + id,
            "app2fname" + id,
            "app2lname" + id
        );
    }

    private CaseData caseDataWithApplicantAndBulkListRef(final int id) {
        final CaseData caseData = caseDataWithApplicant(id);
        caseData.setBulkListCaseReferenceLink(CaseLink.builder().caseReference(BULK_CASE_REFERENCE).build());
        return caseData;
    }

    private CaseDetails<CaseData, State> caseDetails(final Long id, final CaseData caseData, final State state) {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(id);
        caseDetails.setData(caseData);
        caseDetails.setState(state);

        return caseDetails;
    }

    private CaseDetails<CaseData, State> caseDetails(final Long id, final CaseData caseData) {
        return caseDetails(id, caseData, AwaitingPronouncement);
    }

    private CaseDetails<CaseData, State> caseDetailsSubmitted(final Long id, final CaseData caseData) {
        return caseDetails(id, caseData, Submitted);
    }

    private ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue(CaseDetails<CaseData, State> caseDetails) {
        final String caseParties = caseDetails.getData().getApplicant1().getFirstName() + " "
            + caseDetails.getData().getApplicant1().getLastName() + " vs "
            + caseDetails.getData().getApplicant2().getFirstName() + " "
            + caseDetails.getData().getApplicant2().getLastName();

        final BulkListCaseDetails bulkCaseDetails = BulkListCaseDetails
            .builder()
            .caseParties(caseParties)
            .caseReference(
                CaseLink
                    .builder()
                    .caseReference(String.valueOf(caseDetails.getId()))
                    .build()
            )
            .build();

        return
            ListValue
                .<BulkListCaseDetails>builder()
                .value(bulkCaseDetails)
                .build();
    }

    private CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails(
        List<ListValue<BulkListCaseDetails>> bulkListCaseDetails
    ) {
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails = new CaseDetails<>();
        bulkActionCaseDetails.setCaseTypeId(BulkActionCaseTypeConfig.getCaseType());
        bulkActionCaseDetails.setData(BulkActionCaseData.builder()
            .bulkListCaseDetails(bulkListCaseDetails)
            .build());

        return bulkActionCaseDetails;
    }

    private CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails(
        List<ListValue<BulkListCaseDetails>> bulkListCaseDetails,
        List<ListValue<CaseLink>> casesAcceptedToHearing
    ) {
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails = bulkActionCaseDetails(bulkListCaseDetails);
        bulkActionCaseDetails.getData().setCasesAcceptedToListForHearing(casesAcceptedToHearing);

        return bulkActionCaseDetails;
    }

    private ListValue<CaseLink> caseLinkListValue(final CaseLink caseLink, final String id) {
        return
            ListValue
                .<CaseLink>builder()
                .id(id)
                .value(caseLink)
                .build();
    }
}
