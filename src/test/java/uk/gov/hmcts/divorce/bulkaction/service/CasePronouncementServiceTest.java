package uk.gov.hmcts.divorce.bulkaction.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.filter.CaseFilterProcessingState;
import uk.gov.hmcts.divorce.bulkaction.service.filter.CaseProcessingStateFilter;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BIRMINGHAM;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.feignException;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;

@ExtendWith(MockitoExtension.class)
public class CasePronouncementServiceTest {

    @Mock
    private BulkTriggerService bulkTriggerService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Mock
    private CaseProcessingStateFilter caseProcessingStateFilter;

    @InjectMocks
    private CasePronouncementService casePronouncementService;

    @Test
    void shouldSuccessfullyPronounceBulkCasesIfCaseSateAwaitingPronouncement() {

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = List.of(getBulkListCaseDetailsListValue("1"));

        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMINGHAM)
            .bulkListCaseDetails(bulkListCaseDetails)
            .erroredCaseDetails(new ArrayList<>())
            .processedCaseDetails(new ArrayList<>())
            .build();

        final var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        when(caseProcessingStateFilter.filterProcessingState(
            bulkListCaseDetails,
            user,
            SERVICE_AUTHORIZATION,
            EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived),
            EnumSet.of(ConditionalOrderPronounced)
        )).thenReturn(new CaseFilterProcessingState(
            bulkListCaseDetails,
            new ArrayList<>(),
            new ArrayList<>()));

        final var caseTask = mock(CaseTask.class);
        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseDetails, SYSTEM_PRONOUNCE_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            bulkActionCaseData.getBulkListCaseDetails(),
            SYSTEM_PRONOUNCE_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        )).thenReturn(emptyList());

        casePronouncementService.pronounceCases(bulkActionCaseDetails);

        verify(bulkTriggerService).bulkTrigger(
            eq(bulkActionCaseData.getBulkListCaseDetails()),
            eq(SYSTEM_PRONOUNCE_CASE),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        );

        verify(ccdUpdateService).submitBulkActionEvent(
            eq(bulkActionCaseDetails),
            eq(SYSTEM_UPDATE_BULK_CASE),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        );
    }

    @Test
    void shouldSuccessfullyRetryPronounceBulkCasesIfCaseSateConditionalOrderPronounced() {

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = List.of(getBulkListCaseDetailsListValue("1"));

        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMINGHAM)
            .bulkListCaseDetails(bulkListCaseDetails)
            .erroredCaseDetails(new ArrayList<>())
            .processedCaseDetails(new ArrayList<>())
            .build();

        final var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        when(caseProcessingStateFilter.filterProcessingState(
            bulkListCaseDetails,
            user,
            SERVICE_AUTHORIZATION,
            EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived, ConditionalOrderPronounced),
            EnumSet.noneOf(State.class)
        )).thenReturn(new CaseFilterProcessingState(
            bulkListCaseDetails,
            new ArrayList<>(),
            new ArrayList<>()));

        final var caseTask = mock(CaseTask.class);
        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseDetails, SYSTEM_PRONOUNCE_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            bulkActionCaseData.getBulkListCaseDetails(),
            SYSTEM_PRONOUNCE_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        )).thenReturn(emptyList());

        casePronouncementService.retryPronounceCases(bulkActionCaseDetails);

        verify(bulkTriggerService).bulkTrigger(
            eq(bulkActionCaseData.getBulkListCaseDetails()),
            eq(SYSTEM_PRONOUNCE_CASE),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        );

        verify(ccdUpdateService).submitBulkActionEvent(
            eq(bulkActionCaseDetails),
            eq(SYSTEM_UPDATE_BULK_CASE),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        );
    }

    @Test
    void shouldSuccessfullyUpdateErrorBulkCaseListInBulkCaseWhenCasePronouncementFailsForMainCase() {

        final var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        final var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");
        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = List.of(
            bulkListCaseDetailsListValue1,
            bulkListCaseDetailsListValue2
        );

        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMINGHAM)
            .bulkListCaseDetails(bulkListCaseDetails)
            .erroredCaseDetails(new ArrayList<>())
            .processedCaseDetails(new ArrayList<>())
            .build();

        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        final var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        when(caseProcessingStateFilter.filterProcessingState(
            bulkListCaseDetails,
            user,
            SERVICE_AUTHORIZATION,
            EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived),
            EnumSet.of(ConditionalOrderPronounced)
        )).thenReturn(new CaseFilterProcessingState(
            bulkListCaseDetails,
            new ArrayList<>(),
            new ArrayList<>()));

        final var unprocessedBulkCases = List.of(bulkListCaseDetailsListValue2);

        final var caseTask = mock(CaseTask.class);
        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseDetails, SYSTEM_PRONOUNCE_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            bulkActionCaseData.getBulkListCaseDetails(),
            SYSTEM_PRONOUNCE_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        )).thenReturn(unprocessedBulkCases);

        doNothing().when(ccdUpdateService).submitBulkActionEvent(
            bulkActionCaseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION
        );

        casePronouncementService.pronounceCases(bulkActionCaseDetails);

        verify(bulkTriggerService).bulkTrigger(
            eq(bulkActionCaseData.getBulkListCaseDetails()),
            eq(SYSTEM_PRONOUNCE_CASE),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        );

        verify(ccdUpdateService).submitBulkActionEvent(
            bulkActionCaseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION
        );
    }

    @Test
    void shouldSuccessfullyUpdateErrorBulkCaseListInBulkCaseWhenMainCaseIsNotInCorrectState() {

        final var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        final var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");
        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = asList(
            bulkListCaseDetailsListValue1,
            bulkListCaseDetailsListValue2
        );

        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMINGHAM)
            .bulkListCaseDetails(bulkListCaseDetails)
            .erroredCaseDetails(new ArrayList<>())
            .processedCaseDetails(new ArrayList<>())
            .build();

        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        final var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        when(caseProcessingStateFilter.filterProcessingState(
            bulkListCaseDetails,
            user,
            SERVICE_AUTHORIZATION,
            EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived),
            EnumSet.of(ConditionalOrderPronounced)
        )).thenReturn(new CaseFilterProcessingState(
            new ArrayList<>(),
            bulkListCaseDetails,
            new ArrayList<>()));

        final var caseTask = mock(CaseTask.class);
        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseDetails, SYSTEM_PRONOUNCE_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            emptyList(),
            SYSTEM_PRONOUNCE_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        )).thenReturn(emptyList());

        doNothing().when(ccdUpdateService).submitBulkActionEvent(
            bulkActionCaseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION
        );

        casePronouncementService.pronounceCases(bulkActionCaseDetails);

        assertThat(bulkActionCaseDetails.getData().getBulkListCaseDetails()).hasSize(2);
        assertThat(bulkActionCaseDetails.getData().getErroredCaseDetails()).hasSize(2);
    }

    @Test
    void shouldResetErrorListAfterProcessing() {

        final var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        final var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = asList(
            bulkListCaseDetailsListValue1,
            bulkListCaseDetailsListValue2
        );

        final List<ListValue<BulkListCaseDetails>> erroredCaseDetails = new ArrayList<>(bulkListCaseDetails);

        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMINGHAM)
            .bulkListCaseDetails(bulkListCaseDetails)
            .erroredCaseDetails(erroredCaseDetails)
            .processedCaseDetails(new ArrayList<>())
            .build();

        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        final var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        when(caseProcessingStateFilter.filterProcessingState(
            bulkListCaseDetails,
            user,
            SERVICE_AUTHORIZATION,
            EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived),
            EnumSet.of(ConditionalOrderPronounced)
        )).thenReturn(new CaseFilterProcessingState(
            bulkListCaseDetails,
            new ArrayList<>(),
            new ArrayList<>()));

        final var caseTask = mock(CaseTask.class);
        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseDetails, SYSTEM_PRONOUNCE_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            bulkListCaseDetails,
            SYSTEM_PRONOUNCE_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        )).thenReturn(bulkListCaseDetails);

        doNothing().when(ccdUpdateService).submitBulkActionEvent(
            bulkActionCaseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION
        );

        casePronouncementService.pronounceCases(bulkActionCaseDetails);

        assertThat(bulkActionCaseDetails.getData().getBulkListCaseDetails()).hasSize(2);
        assertThat(bulkActionCaseDetails.getData().getErroredCaseDetails()).hasSize(2);
        assertThat(bulkActionCaseDetails.getData().getProcessedCaseDetails()).hasSize(0);
    }

    @Test
    void shouldResetProcessedListAfterProcessing() {

        final var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        final var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = List.of(
            bulkListCaseDetailsListValue1,
            bulkListCaseDetailsListValue2
        );

        final List<ListValue<BulkListCaseDetails>> processedCaseDetails = new ArrayList<>(bulkListCaseDetails);

        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMINGHAM)
            .bulkListCaseDetails(bulkListCaseDetails)
            .erroredCaseDetails(new ArrayList<>())
            .processedCaseDetails(processedCaseDetails)
            .build();

        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        final var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        when(caseProcessingStateFilter.filterProcessingState(
            bulkListCaseDetails,
            user,
            SERVICE_AUTHORIZATION,
            EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived),
            EnumSet.of(ConditionalOrderPronounced)
        )).thenReturn(new CaseFilterProcessingState(
            bulkListCaseDetails,
            new ArrayList<>(),
            new ArrayList<>()));

        final var caseTask = mock(CaseTask.class);
        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseDetails, SYSTEM_PRONOUNCE_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            bulkListCaseDetails,
            SYSTEM_PRONOUNCE_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        )).thenReturn(emptyList());

        doNothing().when(ccdUpdateService).submitBulkActionEvent(
            bulkActionCaseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION
        );

        casePronouncementService.pronounceCases(bulkActionCaseDetails);

        assertThat(bulkActionCaseDetails.getData().getBulkListCaseDetails()).hasSize(2);
        assertThat(bulkActionCaseDetails.getData().getErroredCaseDetails()).hasSize(0);
        assertThat(bulkActionCaseDetails.getData().getProcessedCaseDetails()).hasSize(2);
    }

    @Test
    void shouldNotAddDuplicatesToProcessedList() {

        final var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        final var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = asList(
            bulkListCaseDetailsListValue1,
            bulkListCaseDetailsListValue2
        );

        final List<ListValue<BulkListCaseDetails>> processedCaseDetails = new ArrayList<>(bulkListCaseDetails);

        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMINGHAM)
            .bulkListCaseDetails(bulkListCaseDetails)
            .erroredCaseDetails(new ArrayList<>())
            .processedCaseDetails(processedCaseDetails)
            .build();

        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        final var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        when(caseProcessingStateFilter.filterProcessingState(
            bulkListCaseDetails,
            user,
            SERVICE_AUTHORIZATION,
            EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived),
            EnumSet.of(ConditionalOrderPronounced)
        )).thenReturn(new CaseFilterProcessingState(
            new ArrayList<>(),
            new ArrayList<>(),
            bulkListCaseDetails));

        final var caseTask = mock(CaseTask.class);
        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseDetails, SYSTEM_PRONOUNCE_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            emptyList(),
            SYSTEM_PRONOUNCE_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        )).thenReturn(emptyList());

        doNothing().when(ccdUpdateService).submitBulkActionEvent(
            bulkActionCaseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION
        );

        casePronouncementService.pronounceCases(bulkActionCaseDetails);

        assertThat(bulkActionCaseDetails.getData().getBulkListCaseDetails()).hasSize(2);
        assertThat(bulkActionCaseDetails.getData().getErroredCaseDetails()).hasSize(0);
        assertThat(bulkActionCaseDetails.getData().getProcessedCaseDetails()).hasSize(2);
    }

    @Test
    void shouldNotUpdateErrorBulkCaseListInBulkCaseWhenCasePronouncementFailsForMainCaseAndBulkCaseUpdateThrowsError() {

        final var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        final var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");
        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = List.of(
            bulkListCaseDetailsListValue1,
            bulkListCaseDetailsListValue2
        );
        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMINGHAM)
            .bulkListCaseDetails(bulkListCaseDetails)
            .erroredCaseDetails(new ArrayList<>())
            .processedCaseDetails(new ArrayList<>())
            .build();

        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        final var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        when(caseProcessingStateFilter.filterProcessingState(
            bulkListCaseDetails,
            user,
            SERVICE_AUTHORIZATION,
            EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived),
            EnumSet.of(ConditionalOrderPronounced)
        )).thenReturn(new CaseFilterProcessingState(
            bulkListCaseDetails,
            new ArrayList<>(),
            new ArrayList<>()));

        final var unprocessedBulkCases = List.of(bulkListCaseDetailsListValue2);
        final var caseTask = mock(CaseTask.class);
        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseDetails, SYSTEM_PRONOUNCE_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            bulkActionCaseData.getBulkListCaseDetails(),
            SYSTEM_PRONOUNCE_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        )).thenReturn(unprocessedBulkCases);

        doThrow(new CcdManagementException(409, "some error", feignException(409, "some error")))
            .when(ccdUpdateService).submitBulkActionEvent(
                bulkActionCaseDetails,
                SYSTEM_UPDATE_BULK_CASE,
                user,
                SERVICE_AUTHORIZATION
            );

        casePronouncementService.pronounceCases(bulkActionCaseDetails);

        verify(bulkTriggerService).bulkTrigger(
            eq(bulkActionCaseData.getBulkListCaseDetails()),
            eq(SYSTEM_PRONOUNCE_CASE),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        );

        verify(ccdUpdateService).submitBulkActionEvent(
            bulkActionCaseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION
        );
    }
}
