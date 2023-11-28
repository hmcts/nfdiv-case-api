package uk.gov.hmcts.divorce.bulkaction.util;

import jakarta.servlet.http.HttpServletRequest;
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
import uk.gov.hmcts.divorce.bulkaction.service.BulkCaseProcessingService;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.bulkaction.service.filter.CaseFilterProcessingState;
import uk.gov.hmcts.divorce.bulkaction.service.filter.CaseProcessingStateFilter;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BIRMINGHAM;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemoveBulkCase.SYSTEM_REMOVE_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;

@ExtendWith(MockitoExtension.class)
class BulkCaseTaskUtilTest {

    @Mock
    private CaseProcessingStateFilter caseProcessingStateFilter;

    @Mock
    private BulkTriggerService bulkTriggerService;

    @Mock
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private BulkCaseTaskUtil bulkCaseTaskUtil;

    @Test
    void shouldProcessCasesTest() {

        final var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        final var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = asList(
                bulkListCaseDetailsListValue1,
                bulkListCaseDetailsListValue2
        );

        final List<ListValue<BulkListCaseDetails>> failedCases = List.of(
                bulkListCaseDetailsListValue1
        );

        final List<ListValue<BulkListCaseDetails>> output = new ArrayList<>();

        final var bulkActionCaseData = BulkActionCaseData
                .builder()
                .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
                .court(BIRMINGHAM)
                .bulkListCaseDetails(bulkListCaseDetails)
                .erroredCaseDetails(failedCases)
                .processedCaseDetails(new ArrayList<>())
                .build();

        final var bulkActionCaseDetails = CaseDetails
                .<BulkActionCaseData, BulkActionState>builder()
                .data(bulkActionCaseData)
                .build();

        final var caseTask = mock(CaseTask.class);
        final var user = mock(User.class);

        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseDetails, SYSTEM_REMOVE_BULK_CASE))
                .thenReturn(caseTask);
        when(bulkTriggerService.bulkTrigger(
                BulkCaseProcessingService.getFailedBulkCases(bulkActionCaseDetails),
                SYSTEM_REMOVE_BULK_CASE,
                caseTask,
                user,
                SERVICE_AUTHORIZATION
        )).thenReturn(output);

        final CaseDetails<BulkActionCaseData, BulkActionState> result =
                bulkCaseTaskUtil.processCases(bulkActionCaseDetails, BulkCaseProcessingService.getFailedBulkCases(bulkActionCaseDetails),
                        SYSTEM_REMOVE_BULK_CASE, user, SERVICE_AUTHORIZATION);

        assertThat(result.getData().getBulkListCaseDetails()).hasSize(2);
        assertThat(result.getData().getErroredCaseDetails()).hasSize(0);
        assertThat(result.getData().getProcessedCaseDetails()).hasSize(2);
    }

    @Test
    void shouldPronounceCasesTest() {

        final EnumSet<State> awaitingPronouncement = EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived, ConditionalOrderPronounced);
        final EnumSet<State> postStates = EnumSet.noneOf(State.class);

        final var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        final var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");

        final List<ListValue<BulkListCaseDetails>> output = new ArrayList<>();

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = asList(
                bulkListCaseDetailsListValue1,
                bulkListCaseDetailsListValue2
        );

        final List<ListValue<BulkListCaseDetails>> erroredCaseList = new ArrayList<>();
        final List<ListValue<BulkListCaseDetails>> processedCaseList = new ArrayList<>();

        final var user = mock(User.class);
        final var caseTask = mock(CaseTask.class);

        final var bulkActionCaseData = BulkActionCaseData
                .builder()
                .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
                .court(BIRMINGHAM)
                .bulkListCaseDetails(bulkListCaseDetails)
                .erroredCaseDetails(erroredCaseList)
                .processedCaseDetails(processedCaseList)
                .build();

        final var bulkActionCaseDetails = CaseDetails
                .<BulkActionCaseData, BulkActionState>builder()
                .data(bulkActionCaseData)
                .build();

        final CaseFilterProcessingState caseFilterProcessingState = new CaseFilterProcessingState(
                bulkListCaseDetails,
                erroredCaseList,
                processedCaseList
        );

        when(caseProcessingStateFilter.filterProcessingState(
                bulkListCaseDetails,
                user,
                SERVICE_AUTHORIZATION,
                awaitingPronouncement,
                postStates
        )).thenReturn(caseFilterProcessingState);

        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseDetails, SYSTEM_PRONOUNCE_CASE))
                .thenReturn(caseTask);
        when(bulkTriggerService.bulkTrigger(
                BulkCaseProcessingService.getFailedBulkCases(bulkActionCaseDetails),
                SYSTEM_PRONOUNCE_CASE,
                caseTask,
                user,
                SERVICE_AUTHORIZATION
        )).thenReturn(output);

        final CaseDetails<BulkActionCaseData, BulkActionState> result =
                bulkCaseTaskUtil.pronounceCases(bulkActionCaseDetails, awaitingPronouncement, postStates, user, SERVICE_AUTHORIZATION);

        assertThat(result.getData().getBulkListCaseDetails()).hasSize(2);
    }
}
