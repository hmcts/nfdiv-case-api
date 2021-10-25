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
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
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
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicant2.SYSTEM_REMIND_APPLICANT2;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.ACCESS_CODE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class SystemRemindApplicant2TaskTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private SystemRemindApplicant2Task systemRemindApplicant2Task;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private User user;

    private static final String FLAG = "applicant2ReminderSent";
    private static final BoolQueryBuilder query =
        boolQuery()
            .must(matchQuery(STATE, AwaitingApplicant2Response))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
            .must(existsQuery(ACCESS_CODE))
            .mustNot(matchQuery(String.format(DATA, FLAG), YesOrNo.YES));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldSendReminderEmailToApplicant2IfTenDaysSinceOriginalInviteSent() {
        final CaseData caseData1 = CaseData.builder()
            .dueDate(LocalDate.now().plusDays(4))
            .build();
        final CaseData caseData2 = CaseData.builder()
            .dueDate(LocalDate.now().plusDays(10))
            .build();

        CaseInvite caseInvite = CaseInvite.builder()
            .accessCode("123456789")
            .build();

        caseData1.setCaseInvite(caseInvite);
        caseData2.setCaseInvite(caseInvite);

        Map<String, Object> data1 = new HashMap<>();
        data1.put("dueDate", LocalDate.now().plusDays(4));

        Map<String, Object> data2 = new HashMap<>();
        data2.put("dueDate", LocalDate.now().plusDays(10));

        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        when(caseDetails1.getData()).thenReturn(data1);
        when(caseDetails1.getId()).thenReturn(1L);
        when(caseDetails2.getData()).thenReturn(data2);
        when(caseDetails2.getId()).thenReturn(2L);

        when(mapper.convertValue(data1, CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(data2, CaseData.class)).thenReturn(caseData2);

        List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(AwaitingApplicant2Response, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        systemRemindApplicant2Task.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT2, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotSendReminderEmailToApplicant2IfApplicant2ReminderSent() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseData caseData1 = CaseData.builder()
            .dueDate(LocalDate.now())
            .application(Application.builder()
                .applicant2ReminderSent(YES)
                .build())
            .build();

        final CaseInvite caseInvite = CaseInvite.builder()
            .accessCode("123456789")
            .build();

        caseData1.setCaseInvite(caseInvite);

        Map<String, Object> data1 = new HashMap<>();
        data1.put("dueDate", LocalDate.now().plusDays(4));

        when(caseDetails1.getData()).thenReturn(data1);
        when(mapper.convertValue(data1, CaseData.class)).thenReturn(caseData1);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);

        when(ccdSearchService.searchForAllCasesWithQuery(AwaitingApplicant2Response, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        systemRemindApplicant2Task.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotTriggerSystemRemindApplicant2TaskOnEachCaseWhenCaseNotTenDaysPastOriginalInvite() {
        final CaseDetails caseDetails = mock(CaseDetails.class);
        final CaseData caseData = CaseData.builder()
            .dueDate(LocalDate.now().plusDays(10))
            .build();

        Map<String, Object> data1 = new HashMap<>();
        data1.put("dueDate", LocalDate.now().plusDays(10));

        when(caseDetails.getData()).thenReturn(data1);
        when(mapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(ccdSearchService.searchForAllCasesWithQuery(AwaitingApplicant2Response, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(singletonList(caseDetails));

        systemRemindApplicant2Task.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(AwaitingApplicant2Response, query, user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemRemindApplicant2Task.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final CaseData caseData1 = CaseData.builder()
            .dueDate(LocalDate.now().plusDays(4))
            .build();

        CaseInvite caseInvite = CaseInvite.builder()
            .accessCode("123456789")
            .build();
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        caseData1.setCaseInvite(caseInvite);

        Map<String, Object> data1 = new HashMap<>();
        data1.put("dueDate", LocalDate.now().plusDays(4));

        when(caseDetails1.getData()).thenReturn(data1);
        when(mapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData1);
        when(ccdSearchService.searchForAllCasesWithQuery(AwaitingApplicant2Response, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT2, user, SERVICE_AUTHORIZATION);

        systemRemindApplicant2Task.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT2, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never()).submitEvent(caseDetails2, SYSTEM_REMIND_APPLICANT2, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final CaseData caseData1 = CaseData.builder()
            .dueDate(LocalDate.now().plusDays(4))
            .build();
        final CaseData caseData2 = CaseData.builder()
            .dueDate(LocalDate.now().plusDays(3))
            .build();

        CaseInvite caseInvite = CaseInvite.builder()
            .accessCode("123456789")
            .build();

        caseData1.setCaseInvite(caseInvite);
        caseData2.setCaseInvite(caseInvite);

        Map<String, Object> data1 = new HashMap<>();
        data1.put("dueDate", LocalDate.now().plusDays(4));

        Map<String, Object> data2 = new HashMap<>();
        data2.put("dueDate", LocalDate.now().plusDays(3));

        when(caseDetails1.getData()).thenReturn(data1);
        when(caseDetails2.getData()).thenReturn(data2);
        when(mapper.convertValue(data1, CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(data2, CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(AwaitingApplicant2Response, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException("Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT2, user, SERVICE_AUTHORIZATION);

        systemRemindApplicant2Task.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT2, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_REMIND_APPLICANT2, user, SERVICE_AUTHORIZATION);
    }
}
