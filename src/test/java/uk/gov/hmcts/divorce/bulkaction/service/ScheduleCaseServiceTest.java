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
import uk.gov.hmcts.divorce.bulkaction.task.UpdateCourtHearingDetailsTask;
import uk.gov.hmcts.divorce.bulkaction.task.UpdatePronouncementJudgeDetailsTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;

@ExtendWith(MockitoExtension.class)
class ScheduleCaseServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @Mock
    private BulkCaseProcessingService bulkCaseProcessingService;

    @Mock
    private UpdateCourtHearingDetailsTask updateCourtHearingDetailsTask;

    @Mock
    private UpdatePronouncementJudgeDetailsTask updatePronouncementJudgeDetailsTask;

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
        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        scheduleCaseService.updateCourtHearingDetailsForCasesInBulk(bulkActionCaseDetails);

        verify(bulkCaseProcessingService).updateBulkCase(
            bulkActionCaseDetails,
            updateCourtHearingDetailsTask,
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
        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        scheduleCaseService.updatePronouncementJudgeDetailsForCasesInBulk(bulkActionCaseDetails);

        verify(bulkCaseProcessingService).updateBulkCase(
            bulkActionCaseDetails,
            updatePronouncementJudgeDetailsTask,
            user,
            SERVICE_AUTHORIZATION
        );
    }
}
