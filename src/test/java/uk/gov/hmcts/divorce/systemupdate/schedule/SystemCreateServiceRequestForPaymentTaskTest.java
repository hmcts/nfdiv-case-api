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
import static uk.gov.hmcts.divorce.citizen.event.CitizenCreateServiceRequest.CITIZEN_CREATE_SERVICE_REQUEST;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemJsDisputedAnswerOverdue.SYSTEM_JS_DISPUTED_ANSWER_OVERDUE;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemCreateServiceRequestForPaymentTask.APPLICATION_FEE_ORDER_SUMMARY;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemCreateServiceRequestForPaymentTask.APPLICATION_FEE_SERVICE_REQUEST;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemCreateServiceRequestForPaymentTask.FINAL_ORDER_FEE_ORDER_SUMMARY;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemCreateServiceRequestForPaymentTask.FINAL_ORDER_FEE_SERVICE_REQUEST;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SystemCreateServiceRequestForPaymentTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemCreateServiceRequestForPaymentTask systemCreateServiceRequestForPaymentTask;

    private User systemUser;

    private static final BoolQueryBuilder query =
        boolQuery()
            .should(
                boolQuery()
                    .must(matchQuery(STATE, AwaitingPayment))
                    .must(existsQuery(String.format(DATA, APPLICATION_FEE_ORDER_SUMMARY)))
                    .mustNot(existsQuery(String.format(DATA, APPLICATION_FEE_SERVICE_REQUEST)))
            )
            .should(
                boolQuery()
                    .must(matchQuery(STATE, AwaitingFinalOrderPayment))
                    .must(existsQuery(String.format(DATA, FINAL_ORDER_FEE_ORDER_SUMMARY)))
                    .mustNot(existsQuery(String.format(DATA, FINAL_ORDER_FEE_SERVICE_REQUEST)))
            )
            .minimumShouldMatch(1);

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
            query, systemUser, SERVICE_AUTHORIZATION, AwaitingPayment, AwaitingFinalOrderPayment
        ))
            .thenReturn(List.of(caseDetails));

        when(caseDetails.getId()).thenReturn(TEST_CASE_ID);

        systemCreateServiceRequestForPaymentTask.run();

        verify(ccdUpdateService)
            .submitEvent(TEST_CASE_ID, CITIZEN_CREATE_SERVICE_REQUEST, systemUser, SERVICE_AUTHORIZATION);
    }

    @Test
    public void shouldNotSubmitEventWhenNoCasesFound() {
        when(ccdSearchService.searchForAllCasesWithQuery(
                query, systemUser, SERVICE_AUTHORIZATION, AwaitingPayment, AwaitingFinalOrderPayment
            ))
            .thenReturn(List.of());

        systemCreateServiceRequestForPaymentTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(
                query, systemUser, SERVICE_AUTHORIZATION, AwaitingPayment, AwaitingFinalOrderPayment
            ))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemCreateServiceRequestForPaymentTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);


        when(ccdSearchService.searchForAllCasesWithQuery(
            query, systemUser, SERVICE_AUTHORIZATION, AwaitingPayment, AwaitingFinalOrderPayment
        )).thenReturn(caseDetailsList);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, CITIZEN_CREATE_SERVICE_REQUEST, systemUser, SERVICE_AUTHORIZATION);

        systemCreateServiceRequestForPaymentTask.run();

        verify(ccdUpdateService)
            .submitEvent(TEST_CASE_ID, CITIZEN_CREATE_SERVICE_REQUEST, systemUser, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(2L, SYSTEM_JS_DISPUTED_ANSWER_OVERDUE, systemUser, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, systemUser, SERVICE_AUTHORIZATION, AwaitingPayment, AwaitingFinalOrderPayment
        )).thenReturn(caseDetailsList);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(2L);

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, CITIZEN_CREATE_SERVICE_REQUEST, systemUser, SERVICE_AUTHORIZATION);

        systemCreateServiceRequestForPaymentTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, CITIZEN_CREATE_SERVICE_REQUEST, systemUser, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, CITIZEN_CREATE_SERVICE_REQUEST, systemUser, SERVICE_AUTHORIZATION);
    }
}
