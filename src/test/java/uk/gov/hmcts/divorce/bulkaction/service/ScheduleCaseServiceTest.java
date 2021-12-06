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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithPronouncementJudge.SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;

@ExtendWith(MockitoExtension.class)
class ScheduleCaseServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @Mock
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Mock
    private BulkCaseProcessingService bulkCaseProcessingService;

    @InjectMocks
    private ScheduleCaseService scheduleCaseService;

    @Test
    void shouldSuccessfullyUpdateCourtHearingDetailsForCasesInBulk() {

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetailsListValue = List.of(getBulkListCaseDetailsListValue("1"));
        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BURY_ST_EDMUNDS)
            .bulkListCaseDetails(bulkListCaseDetailsListValue)
            .build();

        final var user = mock(User.class);
        final CaseTask caseTask = caseDetails -> caseDetails;
        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);
        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseDetails, SYSTEM_UPDATE_CASE_COURT_HEARING))
            .thenReturn(caseTask);

        scheduleCaseService.updateCourtHearingDetailsForCasesInBulk(bulkActionCaseDetails, TEST_SYSTEM_AUTHORISATION_TOKEN);

        verify(bulkCaseProcessingService).updateAllBulkCases(
            bulkActionCaseDetails,
            SYSTEM_UPDATE_CASE_COURT_HEARING,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        );
    }

    @Test
    void shouldSuccessfullyUpdatePronouncementJudgeDetailsForCasesInBulk() {

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetailsListValue = List.of(getBulkListCaseDetailsListValue("1"));
        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .pronouncementJudge("District Judge")
            .bulkListCaseDetails(bulkListCaseDetailsListValue)
            .build();

        final var user = mock(User.class);
        final CaseTask caseTask = caseDetails -> caseDetails;
        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);
        when(bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseDetails, SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE))
            .thenReturn(caseTask);

        scheduleCaseService.updatePronouncementJudgeDetailsForCasesInBulk(bulkActionCaseDetails, TEST_SYSTEM_AUTHORISATION_TOKEN);

        verify(bulkCaseProcessingService).updateAllBulkCases(
            bulkActionCaseDetails,
            SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE,
            caseTask,
            user,
            SERVICE_AUTHORIZATION
        );
    }
}
