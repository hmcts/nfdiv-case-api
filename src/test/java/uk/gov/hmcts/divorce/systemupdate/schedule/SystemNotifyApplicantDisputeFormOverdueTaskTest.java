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

import static java.time.temporal.ChronoUnit.DAYS;
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
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyApplicantDisputeFormOverdue.SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemNotifyApplicantDisputeFormOverdueTask.NOTIFICATION_SENT_FLAG;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.AOS_RESPONSE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.ISSUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SystemNotifyApplicantDisputeFormOverdueTaskTest {

    private static final int DISPUTE_DUE_DATE_OFFSET_DAYS = 37;
    @Mock
    private CcdSearchService ccdSearchService;
    @Mock
    private CcdUpdateService ccdUpdateService;
    @Mock
    private IdamService idamService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemNotifyApplicantDisputeFormOverdueTask underTest;

    private User user;

    private static final BoolQueryBuilder query =
        boolQuery()
            .must(matchQuery(STATE, Holding))
            .must(matchQuery(String.format(DATA, AOS_RESPONSE), DISPUTE_DIVORCE.getType()))
            .filter(rangeQuery(ISSUE_DATE).lte(LocalDate.now().minus(DISPUTE_DUE_DATE_OFFSET_DAYS, DAYS)))
            .mustNot(matchQuery(String.format(DATA, NOTIFICATION_SENT_FLAG), YES));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        ReflectionTestUtils.setField(underTest, "disputeDueDateOffsetDays", DISPUTE_DUE_DATE_OFFSET_DAYS);
    }

    @Test
    void shouldSubmitNotificationEventIfNotAlreadyDone() {
        final CaseDetails case1 = CaseDetails.builder().data(new HashMap<>()).id(TEST_CASE_ID).build();
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, Holding))
            .thenReturn(List.of(case1));

        underTest.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, Holding))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        underTest.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        CaseDetails caseDetails1 = CaseDetails.builder().data(new HashMap<>()).id(TEST_CASE_ID).build();
        CaseDetails caseDetails2 = CaseDetails.builder().data(new HashMap<>()).id(2L).build();
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, Holding))
            .thenReturn(List.of(caseDetails1, caseDetails2));
        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, SERVICE_AUTHORIZATION);

        underTest.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(2L, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        CaseDetails caseDetails1 = CaseDetails.builder().data(new HashMap<>()).id(TEST_CASE_ID).build();
        CaseDetails caseDetails2 = CaseDetails.builder().data(new HashMap<>()).id(2L).build();
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, Holding))
            .thenReturn(caseDetailsList);
        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, SERVICE_AUTHORIZATION);

        underTest.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, SERVICE_AUTHORIZATION);
    }
}
