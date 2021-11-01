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
import uk.gov.hmcts.divorce.divorcecase.model.Court;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithPronouncementJudge.SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.feignException;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;

@ExtendWith(MockitoExtension.class)
class ScheduleCaseServiceTest {

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

    @InjectMocks
    private ScheduleCaseService scheduleCaseService;

    @Test
    void shouldSuccessfullyUpdateCourtHearingDetailsForCasesInBulk() {

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetailsListValue = List.of(getBulkListCaseDetailsListValue("1"));
        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .courtName(Court.SERVICE_CENTRE)
            .bulkListCaseDetails(bulkListCaseDetailsListValue)
            .build();

        final var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);

        when(bulkTriggerService.bulkTrigger(
            eq(bulkActionCaseData.getBulkListCaseDetails()),
            eq(SYSTEM_UPDATE_CASE_COURT_HEARING),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        )).thenReturn(emptyList());

        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        doNothing().when(ccdUpdateService).submitBulkActionEvent(
            bulkActionCaseDetails,
                SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION
        );

        scheduleCaseService.updateCourtHearingDetailsForCasesInBulk(bulkActionCaseDetails, TEST_SYSTEM_AUTHORISATION_TOKEN);

        verify(bulkTriggerService).bulkTrigger(
            eq(bulkActionCaseData.getBulkListCaseDetails()),
            eq(SYSTEM_UPDATE_CASE_COURT_HEARING),
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
    void shouldSuccessfullyUpdateErrorBulkCaseListInBulkCaseWhenUpdatingCourtHearingDetailsFailsForMainCase() {
        final var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        final var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetailsListValues = new ArrayList<>();
        bulkListCaseDetailsListValues.add(bulkListCaseDetailsListValue1);
        bulkListCaseDetailsListValues.add(bulkListCaseDetailsListValue2);

        final var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);

        List<ListValue<BulkListCaseDetails>> unprocessedBulkCases = new ArrayList<>();
        unprocessedBulkCases.add(bulkListCaseDetailsListValue2);

        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .courtName(Court.SERVICE_CENTRE)
            .bulkListCaseDetails(bulkListCaseDetailsListValues)
            .build();

        when(bulkTriggerService.bulkTrigger(
            eq(bulkActionCaseData.getBulkListCaseDetails()),
            eq(SYSTEM_UPDATE_CASE_COURT_HEARING),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        )).thenReturn(unprocessedBulkCases);

        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        doNothing().when(ccdUpdateService).submitBulkActionEvent(
            bulkActionCaseDetails,
                SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION
        );

        scheduleCaseService.updateCourtHearingDetailsForCasesInBulk(bulkActionCaseDetails, TEST_SYSTEM_AUTHORISATION_TOKEN);

        verify(bulkTriggerService).bulkTrigger(
            eq(bulkActionCaseData.getBulkListCaseDetails()),
            eq(SYSTEM_UPDATE_CASE_COURT_HEARING),
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
    void shouldNotUpdateErrorBulkCaseListInBulkCaseWhenUpdatingCourtHearingDetailsFailsForMainCaseAndBulkCaseUpdateThrowsError() {
        final var bulkListCaseDetailsListValue1 = getBulkListCaseDetailsListValue("1");
        final var bulkListCaseDetailsListValue2 = getBulkListCaseDetailsListValue("2");

        final var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);

        List<ListValue<BulkListCaseDetails>> unprocessedBulkCases = new ArrayList<>();
        unprocessedBulkCases.add(bulkListCaseDetailsListValue2);


        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetailsListValues = new ArrayList<>();
        bulkListCaseDetailsListValues.add(bulkListCaseDetailsListValue1);
        bulkListCaseDetailsListValues.add(bulkListCaseDetailsListValue2);

        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .courtName(Court.SERVICE_CENTRE)
            .bulkListCaseDetails(bulkListCaseDetailsListValues)
            .build();

        when(bulkTriggerService.bulkTrigger(
            eq(bulkActionCaseData.getBulkListCaseDetails()),
            eq(SYSTEM_UPDATE_CASE_COURT_HEARING),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        )).thenReturn(unprocessedBulkCases);

        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        doThrow(feignException(409, "some error"))
            .when(ccdUpdateService).submitBulkActionEvent(
                bulkActionCaseDetails,
                        SYSTEM_UPDATE_BULK_CASE,
                user,
                SERVICE_AUTHORIZATION
            );

        scheduleCaseService.updateCourtHearingDetailsForCasesInBulk(bulkActionCaseDetails, TEST_SYSTEM_AUTHORISATION_TOKEN);

        verify(bulkTriggerService).bulkTrigger(
            eq(bulkActionCaseData.getBulkListCaseDetails()),
            eq(SYSTEM_UPDATE_CASE_COURT_HEARING),
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
    void shouldSuccessfullyUpdatePronouncementJudgeDetailsForCasesInBulk() {

        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetailsListValue = List.of(getBulkListCaseDetailsListValue("1"));
        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .pronouncementJudge("District Judge")
            .bulkListCaseDetails(bulkListCaseDetailsListValue)
            .build();

        final var user = mock(User.class);

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);

        when(bulkTriggerService.bulkTrigger(
            eq(bulkActionCaseData.getBulkListCaseDetails()),
            eq(SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE),
            any(CaseTask.class),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        )).thenReturn(emptyList());

        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        doNothing().when(ccdUpdateService).submitBulkActionEvent(
            bulkActionCaseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION
        );

        scheduleCaseService.updatePronouncementJudgeDetailsForCasesInBulk(bulkActionCaseDetails, TEST_SYSTEM_AUTHORISATION_TOKEN);

        verify(bulkTriggerService).bulkTrigger(
            eq(bulkActionCaseData.getBulkListCaseDetails()),
            eq(SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE),
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
