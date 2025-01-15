package uk.gov.hmcts.divorce.systemupdate.schedule;

import feign.FeignException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.contract.spec.internal.HttpStatus.REQUEST_TIMEOUT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemJsDisputedAnswerOverdue.SYSTEM_JS_DISPUTED_ANSWER_OVERDUE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemSetTimeToLive.SYSTEM_SET_TTL;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemSetTimeToLiveTask.TIME_TO_LIVE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SystemSetTTLTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemSetTimeToLiveTask systemSetTtl;

    private User systemUser;

    private static final BoolQueryBuilder query =
        boolQuery()
            .must(matchQuery(STATE, Draft))
            .mustNot(existsQuery(String.format(DATA, TIME_TO_LIVE)));

    @BeforeEach
    void setUp() {
        systemUser = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    public void shouldSubmitEventForMatchingCase() {
        final CaseDetails caseDetails = mock(CaseDetails.class);
        when(ccdSearchService.searchForAllCasesWithQuery(
            query, systemUser, SERVICE_AUTHORIZATION, Draft
        )).thenReturn(List.of(caseDetails));

        when(caseDetails.getId()).thenReturn(TEST_CASE_ID);

        systemSetTtl.run();

        verify(ccdUpdateService)
            .submitEvent(TEST_CASE_ID, SYSTEM_SET_TTL, systemUser, SERVICE_AUTHORIZATION);
    }

    @Test
    public void shouldNotSubmitEventWhenNoCasesFound() {
        when(ccdSearchService.searchForAllCasesWithQuery(
                query, systemUser, SERVICE_AUTHORIZATION, Draft
            ))
            .thenReturn(List.of());

        systemSetTtl.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(query, systemUser, SERVICE_AUTHORIZATION, Draft))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemSetTtl.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);


        when(ccdSearchService.searchForAllCasesWithQuery(
            query, systemUser, SERVICE_AUTHORIZATION, Draft
        )).thenReturn(caseDetailsList);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_SET_TTL, systemUser, SERVICE_AUTHORIZATION);

        systemSetTtl.run();

        verify(ccdUpdateService)
            .submitEvent(TEST_CASE_ID, SYSTEM_SET_TTL, systemUser, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(2L, SYSTEM_JS_DISPUTED_ANSWER_OVERDUE, systemUser, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, systemUser, SERVICE_AUTHORIZATION, Draft
        )).thenReturn(caseDetailsList);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(2L);

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_SET_TTL, systemUser, SERVICE_AUTHORIZATION);

        systemSetTtl.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_SET_TTL, systemUser, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_SET_TTL, systemUser, SERVICE_AUTHORIZATION);
    }
}
