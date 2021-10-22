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
import uk.gov.hmcts.divorce.citizen.notification.JointApplicationOverdueNotification;
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
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.config.QueryConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.common.config.QueryConstants.STATE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicant1ApplicationReviewed.SYSTEM_REMIND_APPLICANT_1_APPLICATION_REVIEWED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class SystemRemindApplicant1ApplicationApprovedTaskTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private JointApplicationOverdueNotification jointApplicationOverdueNotification;

    @Mock
    private ObjectMapper mapper;


    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemRemindApplicant1ApplicationApprovedTask systemRemindApplicant1ApplicationApprovedTask;

    private User user;

    private static final String FLAG = "applicant1ReminderSent";
    private static final BoolQueryBuilder query =
        boolQuery()
            .must(matchQuery(STATE, Applicant2Approved))
            .filter(rangeQuery(DUE_DATE).lte(LocalDate.now()))
            .mustNot(matchQuery(String.format("data.%s", FLAG), YesOrNo.YES));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldSendReminderEmailIf7DaysPastSinceJointApplicationApprovedByApplicant2() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final CaseData caseData1 = CaseData.builder().dueDate(LocalDate.now()).build();
        final CaseData caseData2 = CaseData.builder().dueDate(LocalDate.now().plusDays(15)).build();

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now()));
        when(caseDetails1.getId()).thenReturn(1L);
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(15)));
        when(caseDetails2.getId()).thenReturn(2L);

        when(mapper.convertValue(Map.of("dueDate", LocalDate.now()), CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().plusDays(15)), CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(Applicant2Approved, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        systemRemindApplicant1ApplicationApprovedTask.run();

        verify(jointApplicationOverdueNotification).sendApplicationApprovedReminderToApplicant1(caseData1, caseDetails1.getId());
        verify(jointApplicationOverdueNotification, times(0)).sendApplicationApprovedReminderToApplicant1(caseData2, caseDetails2.getId());
        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT_1_APPLICATION_REVIEWED, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotSendReminderEmailIfApplicant1ReminderSent() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseData caseData1 = CaseData.builder()
            .dueDate(LocalDate.now())
            .application(Application.builder()
                .applicant1ReminderSent(YES)
                .build())
            .build();

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now()));
        when(mapper.convertValue(Map.of("dueDate", LocalDate.now()), CaseData.class)).thenReturn(caseData1);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);

        when(ccdSearchService.searchForAllCasesWithQuery(Applicant2Approved, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        systemRemindApplicant1ApplicationApprovedTask.run();

        verifyNoInteractions(jointApplicationOverdueNotification, ccdUpdateService);
    }

    @Test
    void shouldNotTriggerSystemRemindApplicant1ApplicationApprovedTaskOnEachCaseWhenReminderDateIsAfterCurrentDate() {
        final CaseDetails caseDetails = mock(CaseDetails.class);
        final CaseData caseData = CaseData.builder().dueDate(LocalDate.now().plusDays(15)).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("dueDate", LocalDate.now().plusDays(15));

        when(caseDetails.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(15)));
        when(mapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(ccdSearchService.searchForAllCasesWithQuery(Applicant2Approved, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(singletonList(caseDetails));

        systemRemindApplicant1ApplicationApprovedTask.run();

        verifyNoInteractions(jointApplicationOverdueNotification);
        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(Applicant2Approved, query, user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemRemindApplicant1ApplicationApprovedTask.run();

        verifyNoInteractions(jointApplicationOverdueNotification);
        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);
        final CaseData caseData1 = CaseData.builder().dueDate(LocalDate.now()).build();

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now()));
        when(mapper.convertValue(Map.of("dueDate", LocalDate.now()), CaseData.class)).thenReturn(caseData1);
        when(ccdSearchService.searchForAllCasesWithQuery(Applicant2Approved, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT_1_APPLICATION_REVIEWED, user, SERVICE_AUTHORIZATION);

        systemRemindApplicant1ApplicationApprovedTask.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT_1_APPLICATION_REVIEWED, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(caseDetails2, SYSTEM_REMIND_APPLICANT_1_APPLICATION_REVIEWED, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final CaseData caseData1 = CaseData.builder().dueDate(LocalDate.now()).build();
        final CaseData caseData2 = CaseData.builder().dueDate(LocalDate.now().minusDays(5)).build();

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now()));
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", LocalDate.now().minusDays(5)));
        when(mapper.convertValue(Map.of("dueDate", LocalDate.now()), CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().minusDays(5)), CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(Applicant2Approved, query, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException("Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT_1_APPLICATION_REVIEWED, user, SERVICE_AUTHORIZATION);

        systemRemindApplicant1ApplicationApprovedTask.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_REMIND_APPLICANT_1_APPLICATION_REVIEWED, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_REMIND_APPLICANT_1_APPLICATION_REVIEWED, user, SERVICE_AUTHORIZATION);
    }
}
