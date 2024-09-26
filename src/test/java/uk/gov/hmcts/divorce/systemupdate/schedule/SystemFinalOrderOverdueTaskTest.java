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
import java.time.format.DateTimeFormatter;
import java.util.List;

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
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyFinalOrderOverdue.SYSTEM_FINAL_ORDER_OVERDUE;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemFinalOrderOverdueTask.PRONOUNCED_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SystemFinalOrderOverdueTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemFinalOrderOverdueTask task;

    private User user;

    private static final String FINAL_ORDER_OVERDUE_FLAG = "isFinalOrderOverdue";

    private static final String OVERDUE_DATE = DateTimeFormatter
        .ofPattern("yyyy-MM-dd").format(LocalDate.now().minusMonths(12));
    private static final BoolQueryBuilder query = boolQuery()
        .must(boolQuery()
            .should(matchQuery(STATE, AwaitingFinalOrder))
            .should(matchQuery(STATE, AwaitingFinalOrderPayment))
            .should(matchQuery(STATE, AwaitingJointFinalOrder))
            .minimumShouldMatch(1))
        .must(
            boolQuery()
                .should(boolQuery().must(rangeQuery(String.format(DATA, PRONOUNCED_DATE)).lt(OVERDUE_DATE)))
        )
        .mustNot(matchQuery(String.format(DATA, FINAL_ORDER_OVERDUE_FLAG), YesOrNo.YES));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldTriggerAwaitingFinalOrderOverdueTaskOnEachCaseWhenPronouncedDateIsMoreThanTwelveMonthsInPast() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final CaseDetails caseDetails3 = mock(CaseDetails.class);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(2L);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2, caseDetails3);

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingFinalOrder, AwaitingFinalOrderPayment,AwaitingJointFinalOrder
        )).thenReturn(caseDetailsList);

        task.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_FINAL_ORDER_OVERDUE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_FINAL_ORDER_OVERDUE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService,never()).submitEvent(3L, SYSTEM_FINAL_ORDER_OVERDUE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingFinalOrder, AwaitingFinalOrderPayment, AwaitingJointFinalOrder
        )).thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        task.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingFinalOrder, AwaitingFinalOrderPayment, AwaitingJointFinalOrder
        )).thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_FINAL_ORDER_OVERDUE, user, SERVICE_AUTHORIZATION);

        task.run();

        verify(ccdUpdateService)
            .submitEvent(TEST_CASE_ID, SYSTEM_FINAL_ORDER_OVERDUE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(2L, SYSTEM_FINAL_ORDER_OVERDUE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(2L);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingFinalOrder, AwaitingFinalOrderPayment, AwaitingJointFinalOrder
        )).thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_FINAL_ORDER_OVERDUE, user, SERVICE_AUTHORIZATION);

        task.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_FINAL_ORDER_OVERDUE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_FINAL_ORDER_OVERDUE, user, SERVICE_AUTHORIZATION);
    }

}
