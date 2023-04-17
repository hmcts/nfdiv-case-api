package uk.gov.hmcts.divorce.bulkaction.task;

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
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.bulkaction.service.filter.CaseFilterProcessingState;
import uk.gov.hmcts.divorce.bulkaction.service.filter.CaseProcessingStateFilter;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BIRMINGHAM;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;

@ExtendWith(MockitoExtension.class)

public class PronounceCasesTaskTest {

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

    @Mock
    private CaseProcessingStateFilter caseProcessingStateFilter;

    @InjectMocks
    private PronounceCasesTask pronounceCasesTask;

    @Test
    void shouldPronounceCasesTask() {
        final EnumSet<State> awaitingPronouncement = EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived);
        final EnumSet<State> postStates = EnumSet.of(ConditionalOrderPronounced);

        final var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        final var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = asList(
            bulkListCaseDetailsListValue1,
            bulkListCaseDetailsListValue2
        );

        final List<ListValue<BulkListCaseDetails>> erroredCaseList = new ArrayList<>();
        final List<ListValue<BulkListCaseDetails>> processedCaseList = new ArrayList<>();

        final List<ListValue<BulkListCaseDetails>> output = new ArrayList<>();

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

        final var caseTask = mock(CaseTask.class);
        final var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

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
            bulkListCaseDetails,
            SYSTEM_PRONOUNCE_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        )).thenReturn(output);

        final CaseDetails<BulkActionCaseData, BulkActionState> result =
            pronounceCasesTask.apply(bulkActionCaseDetails);

        assertThat(result.getData().getBulkListCaseDetails()).hasSize(2);
    }
}
