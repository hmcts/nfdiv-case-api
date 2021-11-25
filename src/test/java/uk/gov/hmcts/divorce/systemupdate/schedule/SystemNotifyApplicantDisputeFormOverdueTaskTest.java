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

import static java.time.temporal.ChronoUnit.DAYS;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
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

@ExtendWith(MockitoExtension.class)
public class SystemNotifyApplicantDisputeFormOverdueTaskTest {

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
    private SystemNotifyApplicantDisputeFormOverdueTask underTest;

    private User user;

    private static final BoolQueryBuilder query =
        boolQuery()
            .must(matchQuery(STATE, Holding))
            .must(matchQuery(AOS_RESPONSE, DISPUTE_DIVORCE))
            .filter(rangeQuery(ISSUE_DATE).lte(LocalDate.now().minus(37, DAYS)))
            .mustNot(matchQuery(String.format(DATA, NOTIFICATION_SENT_FLAG), YesOrNo.YES));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldSubmitNotificationEventIfNotAlreadyDone() {
        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .applicantNotifiedDisputeFormOverdue(NO)
                .issueDate(LocalDate.now().minusDays(37))
                .build())
            .build();
        when(mapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        final CaseDetails case1 = CaseDetails.builder().data(new HashMap<>()).id(1L).build();
        when(ccdSearchService.searchForAllCasesWithQuery(Holding, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(case1));

        underTest.run();

        verify(ccdUpdateService).submitEvent(case1, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotSubmitNotificationEventIfAlreadyDone() {
        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .applicantNotifiedDisputeFormOverdue(YES)
                .issueDate(LocalDate.now().minusDays(37))
                .build())
            .build();
        when(mapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(ccdSearchService.searchForAllCasesWithQuery(Holding, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(CaseDetails.builder().data(new HashMap<>()).id(1L).build()));

        underTest.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitNotificationEventBefore37Days() {
        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .applicantNotifiedDisputeFormOverdue(NO)
                .issueDate(LocalDate.now().minusDays(20))
                .build())
            .build();
        when(mapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(ccdSearchService.searchForAllCasesWithQuery(Holding, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(CaseDetails.builder().data(new HashMap<>()).id(1L).build()));

        underTest.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(Holding, query, user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        underTest.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .applicantNotifiedDisputeFormOverdue(NO)
                .issueDate(LocalDate.now().minusDays(37))
                .build())
            .build();
        when(mapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails1 = CaseDetails.builder().data(new HashMap<>()).id(1L).build();
        CaseDetails caseDetails2 = CaseDetails.builder().data(new HashMap<>()).id(2L).build();
        when(ccdSearchService.searchForAllCasesWithQuery(Holding, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(caseDetails1, caseDetails2));
        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, SERVICE_AUTHORIZATION);

        underTest.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never()).submitEvent(caseDetails2, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .applicantNotifiedDisputeFormOverdue(NO)
                .issueDate(LocalDate.now().minusDays(37))
                .build())
            .build();
        when(mapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails1 = CaseDetails.builder().data(new HashMap<>()).id(1L).build();
        CaseDetails caseDetails2 = CaseDetails.builder().data(new HashMap<>()).id(2L).build();
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);
        when(ccdSearchService.searchForAllCasesWithQuery(Holding, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);
        doThrow(new CcdManagementException("Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, SERVICE_AUTHORIZATION);

        underTest.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, SERVICE_AUTHORIZATION);
    }
}
