package uk.gov.hmcts.divorce.bulkaction.task;

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
import uk.gov.hmcts.divorce.bulkaction.util.BulkCaseTaskUtil;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BIRMINGHAM;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemoveBulkCase.SYSTEM_REMOVE_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;

@ExtendWith(MockitoExtension.class)
public class ProcessFailedToUnlinkBulkCaseTaskTest {

    @Mock
    private BulkCaseTaskUtil bulkCaseTaskUtil;

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
    private ProcessFailedToUnlinkBulkCaseTask processFailedToUnlinkBulkCaseTask;

    @Test
    void shouldProcessFailedToUnlinkCaseTask() {
        final var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        final var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = asList(
            bulkListCaseDetailsListValue1,
            bulkListCaseDetailsListValue2
        );

        final List<ListValue<BulkListCaseDetails>> failedCases = List.of(
            bulkListCaseDetailsListValue1
        );

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

        final var user = mock(User.class);

        when(request.getHeader(AUTHORIZATION)).thenReturn(CASEWORKER_AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(CASEWORKER_AUTH_TOKEN)).thenReturn(user);

        when(bulkCaseTaskUtil.processCases(bulkActionCaseDetails, BulkCaseProcessingService.getFailedBulkCases(bulkActionCaseDetails),
                SYSTEM_REMOVE_BULK_CASE, user, SERVICE_AUTHORIZATION)).thenReturn(bulkActionCaseDetails);

        final CaseDetails<BulkActionCaseData, BulkActionState> result =
            processFailedToUnlinkBulkCaseTask.apply(bulkActionCaseDetails);

        verify(bulkCaseTaskUtil).processCases(bulkActionCaseDetails, BulkCaseProcessingService.getFailedBulkCases(bulkActionCaseDetails),
                SYSTEM_REMOVE_BULK_CASE, user, SERVICE_AUTHORIZATION);
    }
}
