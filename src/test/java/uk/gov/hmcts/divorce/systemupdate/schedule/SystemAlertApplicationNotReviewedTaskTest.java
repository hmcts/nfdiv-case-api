package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemAlertApplicationNotReviewed.SYSTEM_APPLICATION_NOT_REVIEWED;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class SystemAlertApplicationNotReviewedTaskTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemAlertApplicationNotReviewedTask systemAlertApplicationNotReviewedTask;

    private User user;

    private static final String FLAG = "overdueNotificationSent";
    private static final BoolQueryBuilder query =
        boolQuery()
            .must(matchQuery(STATE, AwaitingApplicant2Response))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
            .mustNot(matchQuery(String.format(DATA, FLAG), YesOrNo.YES));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldSendEmailForOverdueJointApplication() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final CaseData caseData1 = CaseData.builder().dueDate(LocalDate.now()).build();
        final CaseData caseData2 = CaseData.builder().dueDate(LocalDate.now().plusDays(5)).build();

        Map<String, Object> data1 = new HashMap<>();
        data1.put("dueDate", LocalDate.now());

        Map<String, Object> data2 = new HashMap<>();
        data2.put("dueDate", LocalDate.now().plusDays(5));

        when(caseDetails1.getData()).thenReturn(data1);
        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getData()).thenReturn(data2);
        when(caseDetails2.getId()).thenReturn(2L);

        when(mapper.convertValue(data1, CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(data2, CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingApplicant2Response))
            .thenReturn(caseDetailsList);

        systemAlertApplicationNotReviewedTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_APPLICATION_NOT_REVIEWED, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotSendEmailForOverdueJointApplicationIfOverdueNotificationSent() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseData caseData1 = CaseData.builder()
            .dueDate(LocalDate.now())
            .application(Application.builder()
                .overdueNotificationSent(YES)
                .build())
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("dueDate", LocalDate.now());

        when(caseDetails1.getData()).thenReturn(data);
        when(mapper.convertValue(data, CaseData.class)).thenReturn(caseData1);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingApplicant2Response))
            .thenReturn(caseDetailsList);

        systemAlertApplicationNotReviewedTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldIgnoreCaseWhenDueDateIsNull() {
        final CaseDetails caseDetails = mock(CaseDetails.class);
        final CaseData caseData = CaseData.builder().dueDate(null).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("dueDate", null);

        when(caseDetails.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails.getData()).thenReturn(caseDataMap);
        when(mapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingApplicant2Response))
            .thenReturn(List.of(caseDetails));

        systemAlertApplicationNotReviewedTask.run();

        verify(ccdUpdateService, never()).submitEvent(TEST_CASE_ID, SYSTEM_APPLICATION_NOT_REVIEWED, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotTriggerSystemAlertApplicationNotReviewedTaskOnEachCaseWhenCaseDueDateIsAfterCurrentDate() {
        final CaseDetails caseDetails = mock(CaseDetails.class);
        final CaseData caseData = CaseData.builder().dueDate(LocalDate.now().plusDays(5)).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("dueDate", LocalDate.now().plusDays(5));

        when(caseDetails.getData()).thenReturn(caseDataMap);
        when(mapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingApplicant2Response))
            .thenReturn(singletonList(caseDetails));

        systemAlertApplicationNotReviewedTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingApplicant2Response))

            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemAlertApplicationNotReviewedTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);
        final CaseData caseData1 = CaseData.builder().dueDate(LocalDate.now()).build();

        Map<String, Object> data = new HashMap<>();
        data.put("dueDate", LocalDate.now());

        when(caseDetails1.getData()).thenReturn(data);
        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(mapper.convertValue(data, CaseData.class)).thenReturn(caseData1);
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingApplicant2Response))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_APPLICATION_NOT_REVIEWED, user, SERVICE_AUTHORIZATION);

        systemAlertApplicationNotReviewedTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_APPLICATION_NOT_REVIEWED, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never()).submitEvent(2L, SYSTEM_APPLICATION_NOT_REVIEWED, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final CaseData caseData1 = CaseData.builder().dueDate(LocalDate.now()).build();
        final CaseData caseData2 = CaseData.builder().dueDate(LocalDate.now().minusDays(5)).build();

        Map<String, Object> data1 = new HashMap<>();
        data1.put("dueDate", LocalDate.now());

        Map<String, Object> data2 = new HashMap<>();
        data2.put("dueDate", LocalDate.now().minusDays(5));

        when(caseDetails1.getData()).thenReturn(data1);
        when(caseDetails2.getData()).thenReturn(data2);
        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(2L);
        when(mapper.convertValue(data1, CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(data2, CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingApplicant2Response))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_APPLICATION_NOT_REVIEWED, user, SERVICE_AUTHORIZATION);

        systemAlertApplicationNotReviewedTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_APPLICATION_NOT_REVIEWED, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_APPLICATION_NOT_REVIEWED, user, SERVICE_AUTHORIZATION);
    }
}
