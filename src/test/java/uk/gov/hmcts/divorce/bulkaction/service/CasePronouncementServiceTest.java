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
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import static uk.gov.hmcts.divorce.divorcecase.model.State.IssuedToBailiff;
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

    @InjectMocks
    private CasePronouncementService casePronouncementService;

    @Test
    void shouldSuccessfullyPronounceBulkCasesIfCaseSateAwaitingPronouncement() {
        var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMINGHAM)
            .bulkListCaseDetails(List.of(getBulkListCaseDetailsListValue("1")))
            .erroredCaseDetails(new ArrayList<>())
            .processedCaseDetails(new ArrayList<>())
            .build();

        var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(ccdSearchService.searchForCases(List.of("1"), user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(1L)
                    .state(AwaitingPronouncement.name())
                    .build())
            );

        var caseTask = mock(CaseTask.class);
        var bulkActionCaseDetails = CaseDetails
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
    void shouldSuccessfullyPronounceBulkCasesIfCaseStateOfflineDocumentReceived() {
        var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMINGHAM)
            .bulkListCaseDetails(List.of(getBulkListCaseDetailsListValue("1")))
            .erroredCaseDetails(new ArrayList<>())
            .processedCaseDetails(new ArrayList<>())
            .build();

        var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(ccdSearchService.searchForCases(List.of("1"), user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(1L)
                    .state(OfflineDocumentReceived.name())
                    .build())
            );

        var caseTask = mock(CaseTask.class);
        var bulkActionCaseDetails = CaseDetails
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
    void shouldSuccessfullyUpdateErrorBulkCaseListInBulkCaseWhenCasePronouncementFailsForMainCase() {
        var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");
        var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMINGHAM)
            .bulkListCaseDetails(List.of(
                bulkListCaseDetailsListValue1,
                bulkListCaseDetailsListValue2
            ))
            .erroredCaseDetails(new ArrayList<>())
            .processedCaseDetails(new ArrayList<>())
            .build();

        var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(ccdSearchService.searchForCases(List.of("1", "2"), user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(1L)
                    .state(AwaitingPronouncement.name())
                    .build(),
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(2L)
                    .state(AwaitingPronouncement.name())
                    .build())
            );

        var unprocessedBulkCases = List.of(bulkListCaseDetailsListValue2);

        var caseTask = mock(CaseTask.class);
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
        var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");
        var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMINGHAM)
            .bulkListCaseDetails(List.of(
                bulkListCaseDetailsListValue1,
                bulkListCaseDetailsListValue2
            ))
            .erroredCaseDetails(new ArrayList<>())
            .processedCaseDetails(new ArrayList<>())
            .build();

        var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(ccdSearchService.searchForCases(List.of("1", "2"), user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(1L)
                    .state(IssuedToBailiff.name())
                    .build(),
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(2L)
                    .state(IssuedToBailiff.name())
                    .build())
            );

        var caseTask = mock(CaseTask.class);
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
    void shouldResetErrorListBeforeFiltering() {
        var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");

        final List<ListValue<BulkListCaseDetails>> erroredCaseDetails = new ArrayList<>(List.of(
            bulkListCaseDetailsListValue1,
            bulkListCaseDetailsListValue2
        ));

        var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMINGHAM)
            .bulkListCaseDetails(List.of(
                bulkListCaseDetailsListValue1,
                bulkListCaseDetailsListValue2
            ))
            .erroredCaseDetails(erroredCaseDetails)
            .processedCaseDetails(new ArrayList<>())
            .build();

        var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(ccdSearchService.searchForCases(List.of("1", "2"), user, SERVICE_AUTHORIZATION))
            .thenReturn(asList(
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(1L)
                    .state(AwaitingPronouncement.name())
                    .build(),
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(2L)
                    .state(AwaitingPronouncement.name())
                    .build())
            );

        var caseTask = mock(CaseTask.class);
        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseDetails, SYSTEM_PRONOUNCE_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            List.of(
                bulkListCaseDetailsListValue1,
                bulkListCaseDetailsListValue2
            ),
            SYSTEM_PRONOUNCE_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        )).thenReturn(List.of(
            bulkListCaseDetailsListValue1,
            bulkListCaseDetailsListValue2
        ));

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
    void shouldResetProcessedListAndProcessedListBeforeFiltering() {
        var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");

        final List<ListValue<BulkListCaseDetails>> processedCaseDetails = new ArrayList<>(List.of(
            bulkListCaseDetailsListValue1,
            bulkListCaseDetailsListValue2
        ));

        var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMINGHAM)
            .bulkListCaseDetails(List.of(
                bulkListCaseDetailsListValue1,
                bulkListCaseDetailsListValue2
            ))
            .erroredCaseDetails(new ArrayList<>())
            .processedCaseDetails(processedCaseDetails)
            .build();

        var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(ccdSearchService.searchForCases(List.of("1", "2"), user, SERVICE_AUTHORIZATION))
            .thenReturn(asList(
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(1L)
                    .state(AwaitingPronouncement.name())
                    .build(),
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(2L)
                    .state(AwaitingPronouncement.name())
                    .build())
            );

        var caseTask = mock(CaseTask.class);
        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseDetails, SYSTEM_PRONOUNCE_CASE)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            List.of(
                bulkListCaseDetailsListValue1,
                bulkListCaseDetailsListValue2
            ),
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
        var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");

        final List<ListValue<BulkListCaseDetails>> processedCaseDetails = new ArrayList<>(List.of(
            bulkListCaseDetailsListValue1,
            bulkListCaseDetailsListValue2
        ));

        var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMINGHAM)
            .bulkListCaseDetails(List.of(
                bulkListCaseDetailsListValue1,
                bulkListCaseDetailsListValue2
            ))
            .erroredCaseDetails(new ArrayList<>())
            .processedCaseDetails(processedCaseDetails)
            .build();

        var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(ccdSearchService.searchForCases(List.of("1", "2"), user, SERVICE_AUTHORIZATION))
            .thenReturn(asList(
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(1L)
                    .state(ConditionalOrderPronounced.name())
                    .build(),
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(2L)
                    .state(ConditionalOrderPronounced.name())
                    .build())
            );

        var caseTask = mock(CaseTask.class);
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
        var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");
        var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BIRMINGHAM)
            .bulkListCaseDetails(List.of(
                bulkListCaseDetailsListValue1,
                bulkListCaseDetailsListValue2
            ))
            .erroredCaseDetails(new ArrayList<>())
            .processedCaseDetails(new ArrayList<>())
            .build();

        var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(ccdSearchService.searchForCases(List.of("1", "2"), user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(1L)
                    .state(AwaitingPronouncement.name())
                    .build(),
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(2L)
                    .state(AwaitingPronouncement.name())
                    .build())
            );


        var unprocessedBulkCases = List.of(bulkListCaseDetailsListValue2);
        var caseTask = mock(CaseTask.class);
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
