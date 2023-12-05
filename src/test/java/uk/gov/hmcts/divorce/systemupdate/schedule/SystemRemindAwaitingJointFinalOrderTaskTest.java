package uk.gov.hmcts.divorce.systemupdate.schedule;

import feign.FeignException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
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

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindAwaitingJointFinalOrder.SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemRemindAwaitingJointFinalOrderTask.DATE_FINAL_ORDER_SUBMITTED;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemRemindAwaitingJointFinalOrderTask.NOTIFICATION_SENT_FLAG;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SystemRemindAwaitingJointFinalOrderTaskTest {

    private static final int AWAITING_JOINT_FINAL_ORDER_REMINDER_OFFSET_DAYS = 7;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemRemindAwaitingJointFinalOrderTask remindAwaitingJointFinalOrderTask;

    private User user;

    final BoolQueryBuilder query =
        boolQuery()
            .must(matchQuery(STATE, AwaitingJointFinalOrder))
            .must(existsQuery(DATE_FINAL_ORDER_SUBMITTED))
            .filter(rangeQuery(DATE_FINAL_ORDER_SUBMITTED)
                .lte(LocalDate.now().minusDays(AWAITING_JOINT_FINAL_ORDER_REMINDER_OFFSET_DAYS)))
            .mustNot(matchQuery(NOTIFICATION_SENT_FLAG, YesOrNo.YES));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        ReflectionTestUtils.setField(remindAwaitingJointFinalOrderTask, "awaitingJointFinalOrderReminderOffsetDays",
            AWAITING_JOINT_FINAL_ORDER_REMINDER_OFFSET_DAYS);
    }

    @Test
    void shouldSubmitNotificationEventIfNotAlreadyDone() {
        final HashMap<String, Object> caseData = new HashMap<>();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(caseData)
            .id(TEST_CASE_ID)
            .build();

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(List.of(caseDetails));

        remindAwaitingJointFinalOrderTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        remindAwaitingJointFinalOrderTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        CaseDetails caseDetails1 = CaseDetails.builder().data(new HashMap<>()).id(TEST_CASE_ID).build();
        CaseDetails caseDetails2 = CaseDetails.builder().data(new HashMap<>()).id(2L).build();
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(List.of(caseDetails1, caseDetails2));
        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER, user, SERVICE_AUTHORIZATION);

        remindAwaitingJointFinalOrderTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(2L, SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        CaseDetails caseDetails1 = CaseDetails.builder().data(new HashMap<>()).id(TEST_CASE_ID).build();
        CaseDetails caseDetails2 = CaseDetails.builder().data(new HashMap<>()).id(2L).build();
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);
        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER, user, SERVICE_AUTHORIZATION);

        remindAwaitingJointFinalOrderTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldRunAppropriateQuery() {
        remindAwaitingJointFinalOrderTask.run();

        verify(ccdSearchService).searchForAllCasesWithQuery(eq(query), any(), any(), any());
    }
}
