package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.divorce.common.notification.AwaitingConditionalOrderReminderNotification;
import uk.gov.hmcts.divorce.common.notification.ConditionalOrderPendingReminderNotification;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
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
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicantsApplyForCOrder.SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder.SystemRemindApplicantsApplyForCOrderTask.NOTIFICATION_FLAG;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SystemRemindApplicantsApplyForCOrderTaskTest {

    private static final int DUE_DATE_OFFSET_DAYS = 14;
    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private AwaitingConditionalOrderReminderNotification awaitingConditionalOrderReminderNotification;

    @Mock
    private ConditionalOrderPendingReminderNotification conditionalOrderPendingReminderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SystemRemindApplicantsApplyForCOrderTask underTest;

    private User user;

    private static final BoolQueryBuilder query =
        boolQuery()
            .must(
                boolQuery()
                    .should(matchQuery(STATE, AwaitingConditionalOrder))
                    .should(matchQuery(STATE, ConditionalOrderPending))
                    .should(matchQuery(STATE, ConditionalOrderDrafted))
                    .minimumShouldMatch(1)
            )
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now().minusDays(DUE_DATE_OFFSET_DAYS)))
            .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YesOrNo.YES));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        ReflectionTestUtils.setField(underTest, "submitCOrderReminderOffsetDays", DUE_DATE_OFFSET_DAYS);
    }

    @Test
    void shouldSendEmailForConditionalOrder() {
        final CaseDetails caseDetails1 = CaseDetails.builder().state(AwaitingConditionalOrder.name()).id(TEST_CASE_ID).build();
        final CaseDetails caseDetails2 = CaseDetails.builder().state(AwaitingConditionalOrder.name()).id(2L).build();
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingConditionalOrder, ConditionalOrderPending, ConditionalOrderDrafted))
            .thenReturn(caseDetailsList);

        underTest.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingConditionalOrder, ConditionalOrderPending, ConditionalOrderDrafted))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        underTest.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = CaseDetails.builder().id(TEST_CASE_ID).build();
        final CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingConditionalOrder, ConditionalOrderPending, ConditionalOrderDrafted))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER, user, SERVICE_AUTHORIZATION);

        underTest.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(2L, SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = CaseDetails.builder().id(TEST_CASE_ID).build();
        final CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingConditionalOrder, ConditionalOrderPending, ConditionalOrderDrafted))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER, user, SERVICE_AUTHORIZATION);

        underTest.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER, user, SERVICE_AUTHORIZATION);

    }
}
