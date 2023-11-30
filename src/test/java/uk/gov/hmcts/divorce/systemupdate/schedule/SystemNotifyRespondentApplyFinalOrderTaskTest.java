package uk.gov.hmcts.divorce.systemupdate.schedule;

import feign.FeignException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
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
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyRespondentApplyFinalOrder.SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemNotifyRespondentApplyFinalOrderTask.APPLICATION_TYPE;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemNotifyRespondentApplyFinalOrderTask.NOTIFICATION_FLAG;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemNotifyRespondentApplyFinalOrderTask.RESP_ELIGIBLE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemNotifyRespondentApplyFinalOrderTask.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SystemNotifyRespondentApplyFinalOrderTaskTest {

    @Mock
    private CcdSearchService ccdSearchService;
    @Mock
    private CcdUpdateService ccdUpdateService;
    @Mock
    private IdamService idamService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemNotifyRespondentApplyFinalOrderTask systemNotifyRespondentApplyFinalOrderTask;

    private User user;

    private static final BoolQueryBuilder query =
        boolQuery()
            .must(matchQuery(STATE, AwaitingFinalOrder))
            .filter(rangeQuery(String.format(DATA, RESP_ELIGIBLE_DATE)).lte(LocalDate.now()))
            .must(matchQuery(String.format(DATA, APPLICATION_TYPE), SOLE_APPLICATION))
            .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YesOrNo.YES));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldTriggerNotifyRespondentEventWhenSearchReturnsNonEmptyList() {

        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        Map<String, Object> data1 = new HashMap<>();
        data1.put("dateFinalOrderEligibleToRespondent", LocalDate.now().minusMonths(1).toString());
        data1.put("finalOrderReminderSentApplicant2", YesOrNo.NO);
        when(caseDetails1.getData()).thenReturn(data1);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(2L);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingFinalOrder))
            .thenReturn(caseDetailsList);

        systemNotifyRespondentApplyFinalOrderTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotTriggerNotifyRespondentEventWhenSearchReturnsEmptyList() {

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingFinalOrder))
            .thenReturn(Collections.emptyList());

        systemNotifyRespondentApplyFinalOrderTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingFinalOrder))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemNotifyRespondentApplyFinalOrderTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        Map<String, Object> data1 = new HashMap<>();
        data1.put("dateFinalOrderEligibleToRespondent", LocalDate.now().minusMonths(1).toString());
        data1.put("finalOrderReminderSentApplicant2", YesOrNo.NO);
        when(caseDetails1.getData()).thenReturn(data1);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingFinalOrder))
            .thenReturn(caseDetailsList);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER, user, SERVICE_AUTHORIZATION);

        systemNotifyRespondentApplyFinalOrderTask.run();

        verify(ccdUpdateService)
            .submitEvent(TEST_CASE_ID, SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(2L, SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        Map<String, Object> data1 = new HashMap<>();
        data1.put("dateFinalOrderEligibleToRespondent", LocalDate.now().minusMonths(1).toString());
        data1.put("finalOrderReminderSentApplicant2", YesOrNo.NO);
        when(caseDetails1.getData()).thenReturn(data1);

        Map<String, Object> data2 = new HashMap<>();
        data2.put("dateFinalOrderEligibleToRespondent", LocalDate.now().minusMonths(1).toString());
        data2.put("finalOrderReminderSentApplicant2", YesOrNo.NO);
        when(caseDetails2.getData()).thenReturn(data2);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingFinalOrder))
            .thenReturn(caseDetailsList);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(2L);

        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER, user, SERVICE_AUTHORIZATION);

        systemNotifyRespondentApplyFinalOrderTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldRunAppropriateQuery() {
        final BoolQueryBuilder expectedQuery = boolQuery()
            .must(matchQuery(STATE, AwaitingFinalOrder))
            .filter(rangeQuery(String.format(DATA, RESP_ELIGIBLE_DATE)).lte(LocalDate.now()))
            .must(matchQuery(String.format(DATA, APPLICATION_TYPE), SOLE_APPLICATION))
            .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YesOrNo.YES));

        systemNotifyRespondentApplyFinalOrderTask.run();

        verify(ccdSearchService).searchForAllCasesWithQuery(eq(expectedQuery), any(), any(), any());
    }
}
