package uk.gov.hmcts.divorce.systemupdate.schedule;

import feign.FeignException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicantsApplyForFinalOrder.SYSTEM_REMIND_APPLICANTS_APPLY_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.FINAL_ORDER_ELIGIBLE_FROM_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class SystemRemindApplicantsApplyForFinalOrderTaskTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemRemindApplicantsApplyForFinalOrderTask systemRemindApplicantsApplyForFinalOrderTask;

    @Value("${apply_for_final_order.reminder_offset_days}")
    private int applyForFinalOrderReminderOffsetDays;

    private User user;

    private static final String NOTIFICATION_SENT_FLAG = "finalOrderReminderSentApplicant1";
    private static final QueryBuilder dateFinalOrderEligibleFromExists = existsQuery("data.dateFinalOrderEligibleFrom");

    final BoolQueryBuilder query =
        boolQuery()
            .must(matchQuery(STATE, AwaitingFinalOrder))
            .filter(rangeQuery(FINAL_ORDER_ELIGIBLE_FROM_DATE).lte(LocalDate.now().minusDays(applyForFinalOrderReminderOffsetDays)))
            .mustNot(matchQuery(String.format(DATA, NOTIFICATION_SENT_FLAG), YesOrNo.YES))
            .must(boolQuery().must(dateFinalOrderEligibleFromExists));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldSubmitNotificationEventIfNotAlreadyDone() {
        final HashMap<String, Object> caseData = new HashMap<>();

        CaseDetails caseDetails = CaseDetails.builder()
            .data(caseData)
            .build();

        when(ccdSearchService.searchForAllCasesWithQuery(AwaitingFinalOrder, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(caseDetails));

        systemRemindApplicantsApplyForFinalOrderTask.run();

        verify(ccdUpdateService).submitEvent(caseDetails, SYSTEM_REMIND_APPLICANTS_APPLY_FOR_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(AwaitingFinalOrder, query, user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemRemindApplicantsApplyForFinalOrderTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        CaseDetails caseDetails1 = CaseDetails.builder().data(new HashMap<>()).id(1L).build();
        CaseDetails caseDetails2 = CaseDetails.builder().data(new HashMap<>()).id(2L).build();
        when(ccdSearchService.searchForAllCasesWithQuery(AwaitingFinalOrder, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(caseDetails1, caseDetails2));
        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANTS_APPLY_FOR_FINAL_ORDER, user, SERVICE_AUTHORIZATION);

        systemRemindApplicantsApplyForFinalOrderTask.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANTS_APPLY_FOR_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(caseDetails2, SYSTEM_REMIND_APPLICANTS_APPLY_FOR_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        CaseDetails caseDetails1 = CaseDetails.builder().data(new HashMap<>()).id(1L).build();
        CaseDetails caseDetails2 = CaseDetails.builder().data(new HashMap<>()).id(2L).build();
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);
        when(ccdSearchService.searchForAllCasesWithQuery(AwaitingFinalOrder, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);
        doThrow(new CcdManagementException("Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANTS_APPLY_FOR_FINAL_ORDER, user, SERVICE_AUTHORIZATION);

        systemRemindApplicantsApplyForFinalOrderTask.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANTS_APPLY_FOR_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_REMIND_APPLICANTS_APPLY_FOR_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
    }
}
