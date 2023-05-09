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
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BIRMINGHAM;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemoveBulkCase.SYSTEM_REMOVE_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;

@ExtendWith(MockitoExtension.class)

public class DropCaseTaskTest {

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
    private DropCaseTask dropCaseTask;

    @Test
    void shouldReturnDropCaseTask() {
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

        final var caseTask = mock(CaseTask.class);
        final var user = mock(User.class);

        when(request.getHeader(AUTHORIZATION)).thenReturn(CASEWORKER_AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(CASEWORKER_AUTH_TOKEN)).thenReturn(user);
        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseDetails, SYSTEM_REMOVE_BULK_CASE))
            .thenReturn(caseTask);
        when(bulkTriggerService.bulkTrigger(
            bulkListCaseDetails,
            SYSTEM_REMOVE_BULK_CASE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        )).thenReturn(bulkListCaseDetails);

        final CaseDetails<BulkActionCaseData, BulkActionState> result =
            dropCaseTask.apply(bulkActionCaseDetails);

        assertThat(result.getData().getBulkListCaseDetails()).hasSize(2);
        assertThat(result.getData().getErroredCaseDetails()).hasSize(2);
        assertThat(result.getData().getProcessedCaseDetails()).hasSize(0);
    }
}
