package uk.gov.hmcts.divorce.systemupdate.schedule;

import feign.FeignException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
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
import static org.springframework.cloud.contract.spec.internal.HttpStatus.REQUEST_TIMEOUT;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAnswer;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.SEPARATION;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemJsDisputedAnswerOverdue.SYSTEM_JS_DISPUTED_ANSWER_OVERDUE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.AOS_RESPONSE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.AWAITING_JS_ANSWER_START_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.SUPPLEMENTARY_CASE_TYPE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;


@ExtendWith(MockitoExtension.class)
class SystemJsDisputedAnswerOverdueTaskTest {

    @Mock
    private IdamService idamService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdSearchService ccdSearchService;
    @Mock
    private CcdUpdateService ccdUpdateService;

    @Value("${judicial_separation_answer_overdue.offset_days}")
    private int answerOverdueOffsetDays;

    @InjectMocks
    private SystemJsDisputedAnswerOverdueTask answerOverdueTask;

    final BoolQueryBuilder query =
        boolQuery()
            .must(matchQuery(STATE, AwaitingAnswer))
            .must(
                boolQuery()
                    .should(matchQuery(String.format(DATA, SUPPLEMENTARY_CASE_TYPE), JUDICIAL_SEPARATION))
                    .should(matchQuery(String.format(DATA, SUPPLEMENTARY_CASE_TYPE), SEPARATION))
                    .minimumShouldMatch(1)
            )
            .must(matchQuery(String.format(DATA, AOS_RESPONSE), DISPUTE_DIVORCE.getType()))
            .filter(
                rangeQuery(String.format(DATA, AWAITING_JS_ANSWER_START_DATE)).lte(LocalDate.now().minusDays(answerOverdueOffsetDays))
            );

    private User systemUser;

    @BeforeEach
    void setUp() {
        systemUser = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    public void shouldSubmitEventForMatchingCase() {
        final CaseDetails caseDetails = mock(CaseDetails.class);
        when(ccdSearchService.searchForAllCasesWithQuery(query, systemUser, SERVICE_AUTHORIZATION, AwaitingAnswer))
            .thenReturn(List.of(caseDetails));

        when(caseDetails.getId()).thenReturn(TEST_CASE_ID);

        answerOverdueTask.run();

        verify(ccdUpdateService)
            .submitEvent(TEST_CASE_ID, SYSTEM_JS_DISPUTED_ANSWER_OVERDUE, systemUser, SERVICE_AUTHORIZATION);
    }

    @Test
    public void shouldNotSubmitEventWhenNoCasesFound() {
        when(ccdSearchService.searchForAllCasesWithQuery(query, systemUser, SERVICE_AUTHORIZATION, AwaitingAnswer))
            .thenReturn(List.of());

        answerOverdueTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(query, systemUser, SERVICE_AUTHORIZATION, AwaitingAnswer))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        answerOverdueTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);


        when(ccdSearchService.searchForAllCasesWithQuery(query, systemUser, SERVICE_AUTHORIZATION, AwaitingAnswer))
            .thenReturn(caseDetailsList);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_JS_DISPUTED_ANSWER_OVERDUE, systemUser, SERVICE_AUTHORIZATION);

        answerOverdueTask.run();

        verify(ccdUpdateService)
            .submitEvent(TEST_CASE_ID, SYSTEM_JS_DISPUTED_ANSWER_OVERDUE, systemUser, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(2L, SYSTEM_JS_DISPUTED_ANSWER_OVERDUE, systemUser, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(query, systemUser, SERVICE_AUTHORIZATION, AwaitingAnswer))
            .thenReturn(caseDetailsList);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(2L);

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_JS_DISPUTED_ANSWER_OVERDUE, systemUser, SERVICE_AUTHORIZATION);

        answerOverdueTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_JS_DISPUTED_ANSWER_OVERDUE, systemUser, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_JS_DISPUTED_ANSWER_OVERDUE, systemUser, SERVICE_AUTHORIZATION);
    }
}
