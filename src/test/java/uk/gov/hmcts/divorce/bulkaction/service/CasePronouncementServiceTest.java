package uk.gov.hmcts.divorce.bulkaction.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

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
import static uk.gov.hmcts.divorce.divorcecase.model.State.IssuedToBailiff;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.feignException;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;

@ExtendWith(MockitoExtension.class)
public class CasePronouncementServiceTest {

    @Mock
    private BulkTriggerService bulkTriggerService;

    @Mock
    private IdamService idamService;

    @Mock
    private HttpServletRequest request;

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
    void shouldSuccessfullyPronounceBulkCases() {
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
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);
        when(ccdSearchService.searchForCases(List.of("1"), user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(1L)
                    .state(AwaitingPronouncement.getName())
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

        casePronouncementService.pronounceCases(bulkActionCaseDetails, TEST_SYSTEM_AUTHORISATION_TOKEN);

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
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);
        when(ccdSearchService.searchForCases(List.of("1", "2"), user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(1L)
                    .state(AwaitingPronouncement.getName())
                    .build(),
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(2L)
                    .state(AwaitingPronouncement.getName())
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

        casePronouncementService.pronounceCases(bulkActionCaseDetails, TEST_SYSTEM_AUTHORISATION_TOKEN);

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
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);
        when(ccdSearchService.searchForCases(List.of("1", "2"), user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(1L)
                    .state(IssuedToBailiff.getName())
                    .build(),
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(2L)
                    .state(IssuedToBailiff.getName())
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

        casePronouncementService.pronounceCases(bulkActionCaseDetails, TEST_SYSTEM_AUTHORISATION_TOKEN);

        assertThat(bulkActionCaseDetails.getData().getBulkListCaseDetails()).hasSize(0);
        assertThat(bulkActionCaseDetails.getData().getErroredCaseDetails()).hasSize(2);
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
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);
        when(ccdSearchService.searchForCases(List.of("1", "2"), user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(1L)
                    .state(AwaitingPronouncement.getName())
                    .build(),
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                    .id(2L)
                    .state(AwaitingPronouncement.getName())
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

        doThrow(feignException(409, "some error"))
            .when(ccdUpdateService).submitBulkActionEvent(
                bulkActionCaseDetails,
                SYSTEM_UPDATE_BULK_CASE,
                user,
                SERVICE_AUTHORIZATION
            );

        casePronouncementService.pronounceCases(bulkActionCaseDetails, TEST_SYSTEM_AUTHORISATION_TOKEN);

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
