package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.bulkaction.service.ScheduleCaseService;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class SystemProcessFailedScheduledCasesTaskTest {
    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private BulkTriggerService bulkTriggerService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private ScheduleCaseService scheduleCaseService;

    @InjectMocks
    private SystemProcessFailedScheduledCasesTask systemProcessFailedScheduledCasesTask;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    /**
     * Added SuppressWarnings due to mocking TypeReference as it requires to be parameterized.
     * Since we can't use parameterized in mock added suppression
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldProcessFailedListedBulkActionCases() {

        final var errorBulkListCase =
            BulkListCaseDetails.builder().caseReference(CaseLink.builder().caseReference("3").build()).build();
        final var processedBulkListCase =
            BulkListCaseDetails.builder().caseReference(CaseLink.builder().caseReference("4").build()).build();

        final Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("courtName", "westMidlands");
        caseDataMap.put("erroredCaseDetails", List.of(errorBulkListCase));
        caseDataMap.put("bulkListCaseDetails", List.of(processedBulkListCase));
        caseDataMap.put("dateAndTimeOfHearing", "2021-11-24T00:00:00.000");
        caseDataMap.put("processedCaseDetails", emptyList());

        final CaseDetails caseDetails = CaseDetails.builder()
            .id(1L)
            .data(caseDataMap)
            .build();


        final var caseDetailsList = List.of(caseDetails);
        when(ccdSearchService.searchForBulkCasesWithCaseErrorsAndState(Listed, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        final var bulkActionCaseData = BulkActionCaseData.builder().build();
        final var bulkListCaseDetailsListValues = List.of(
            getBulkListCaseDetailsListValue(processedBulkListCase),
            getBulkListCaseDetailsListValue(errorBulkListCase)
        );
        final var processedBulkListCaseDetailsListValues = List.of(getBulkListCaseDetailsListValue(processedBulkListCase));
        final var errorBulkListCaseDetailsListValues = List.of(getBulkListCaseDetailsListValue(errorBulkListCase));

        bulkActionCaseData.setBulkListCaseDetails(bulkListCaseDetailsListValues);
        bulkActionCaseData.setProcessedCaseDetails(processedBulkListCaseDetailsListValues);
        bulkActionCaseData.setErroredCaseDetails(errorBulkListCaseDetailsListValues);

        when(mapper.convertValue(eq(caseDetails.getData()), eq(BulkActionCaseData.class)))
            .thenReturn(bulkActionCaseData);

        when(mapper.convertValue(eq(bulkActionCaseData), any(TypeReference.class))).thenReturn(caseDataMap);

        CaseTask caseTask = mock(CaseTask.class);
        when(scheduleCaseService.getCaseTask(bulkActionCaseData)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            eq(List.of(getBulkListCaseDetailsListValue(errorBulkListCase))),
            eq(SYSTEM_UPDATE_CASE_COURT_HEARING),
            eq(caseTask),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        )).thenReturn(emptyList());

        doNothing().when(ccdUpdateService).updateBulkCaseWithRetries(
            caseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION,
            1L);

        systemProcessFailedScheduledCasesTask.run();

        verify(mapper).convertValue(eq(caseDetails.getData()), eq(BulkActionCaseData.class));

        verify(bulkTriggerService).bulkTrigger(
            eq(List.of(getBulkListCaseDetailsListValue(errorBulkListCase))),
            eq(SYSTEM_UPDATE_CASE_COURT_HEARING),
            eq(caseTask),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        );

        verify(ccdUpdateService).updateBulkCaseWithRetries(
            caseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION,
            1L);

        verify(mapper).convertValue(eq(bulkActionCaseData), any(TypeReference.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldProcessAllCasesInBulkWhenProcessedListIsEmpty() {

        final var bulkListCase =
            BulkListCaseDetails.builder().caseReference(CaseLink.builder().caseReference("4").build()).build();

        final Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("courtName", "westMidlands");
        caseDataMap.put("erroredCaseDetails", emptyList());
        caseDataMap.put("bulkListCaseDetails", List.of(bulkListCase));
        caseDataMap.put("dateAndTimeOfHearing", "2021-11-24T00:00:00.000");
        caseDataMap.put("processedCaseDetails", emptyList());

        final CaseDetails caseDetails = CaseDetails.builder()
            .id(1L)
            .data(caseDataMap)
            .build();


        final var caseDetailsList = List.of(caseDetails);
        when(ccdSearchService.searchForBulkCasesWithCaseErrorsAndState(Listed, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        final var bulkActionCaseData = BulkActionCaseData.builder().build();
        final var bulkListCaseDetailsListValues = List.of(
            getBulkListCaseDetailsListValue(bulkListCase)
        );

        bulkActionCaseData.setBulkListCaseDetails(bulkListCaseDetailsListValues);

        when(mapper.convertValue(eq(caseDetails.getData()), eq(BulkActionCaseData.class)))
            .thenReturn(bulkActionCaseData);

        when(mapper.convertValue(eq(bulkActionCaseData), any(TypeReference.class))).thenReturn(caseDataMap);

        CaseTask caseTask = mock(CaseTask.class);
        when(scheduleCaseService.getCaseTask(bulkActionCaseData)).thenReturn(caseTask);

        when(bulkTriggerService.bulkTrigger(
            eq(List.of(getBulkListCaseDetailsListValue(bulkListCase))),
            eq(SYSTEM_UPDATE_CASE_COURT_HEARING),
            eq(caseTask),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        )).thenReturn(emptyList());

        doNothing().when(ccdUpdateService).updateBulkCaseWithRetries(
            caseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION,
            1L);

        systemProcessFailedScheduledCasesTask.run();

        verify(mapper).convertValue(eq(caseDetails.getData()), eq(BulkActionCaseData.class));

        verify(bulkTriggerService).bulkTrigger(
            eq(List.of(getBulkListCaseDetailsListValue(bulkListCase))),
            eq(SYSTEM_UPDATE_CASE_COURT_HEARING),
            eq(caseTask),
            eq(user),
            eq(SERVICE_AUTHORIZATION)
        );

        verify(ccdUpdateService).updateBulkCaseWithRetries(
            caseDetails,
            SYSTEM_UPDATE_BULK_CASE,
            user,
            SERVICE_AUTHORIZATION,
            1L);

        verify(mapper).convertValue(eq(bulkActionCaseData), any(TypeReference.class));
    }
    
    @Test
    void shouldStopProcessingIfCcdSearchCaseExceptionIsThrown() {
        doThrow(new CcdSearchCaseException("message", null))
            .when(ccdSearchService).searchForBulkCasesWithCaseErrorsAndState(Listed, user, SERVICE_AUTHORIZATION);

        systemProcessFailedScheduledCasesTask.run();

        verifyNoInteractions(bulkTriggerService);
    }

    private ListValue<BulkListCaseDetails> getBulkListCaseDetailsListValue(final BulkListCaseDetails processedBulkListCase) {
        return ListValue.<BulkListCaseDetails>builder().value(processedBulkListCase).build();
    }
}


