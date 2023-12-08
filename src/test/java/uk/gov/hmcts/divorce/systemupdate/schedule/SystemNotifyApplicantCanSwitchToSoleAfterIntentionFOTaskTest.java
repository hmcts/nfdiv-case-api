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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
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
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyApplicantCanSwitchToSoleAfterIntentionFO.SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemNotifyApplicantCanSwitchToSoleAfterIntentionFOTask.APP_1_INTENDED_TO_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemNotifyApplicantCanSwitchToSoleAfterIntentionFOTask.APP_1_NOTIFICATION_SENT_FLAG;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemNotifyApplicantCanSwitchToSoleAfterIntentionFOTask.APP_2_INTENDED_TO_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemNotifyApplicantCanSwitchToSoleAfterIntentionFOTask.APP_2_NOTIFICATION_SENT_FLAG;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class SystemNotifyApplicantCanSwitchToSoleAfterIntentionFOTaskTest {

    private static final LocalDate NOW = LocalDate.now();

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SystemNotifyApplicantCanSwitchToSoleAfterIntentionFOTask task;

    private User user;

    private static final BoolQueryBuilder query =
        boolQuery()
            .must(matchQuery(STATE, AwaitingJointFinalOrder))
            .must(boolQuery()
                .should(matchQuery(String.format(DATA, APP_1_INTENDED_TO_SWITCH_TO_SOLE), YES))
                .should(matchQuery(String.format(DATA, APP_2_INTENDED_TO_SWITCH_TO_SOLE), YES))
                .minimumShouldMatch(1)
            )
            .mustNot(matchQuery(String.format(DATA, APP_1_NOTIFICATION_SENT_FLAG), YES))
            .mustNot(matchQuery(String.format(DATA, APP_2_NOTIFICATION_SENT_FLAG), YES));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldTriggerSystemUpdateTaskOnEachCaseWhenFinalOrderApp1DateDeclaredIntentionSwitchToSoleIsPastFourteenDays() {
        final LocalDate datePast14Days = NOW.minusDays(15);
        final LocalDate dateNotPast14Days = NOW.minusDays(6);
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final CaseDetails caseDetails3 = mock(CaseDetails.class);

        final CaseData caseData1 = getCaseDataForApp1(datePast14Days);
        final CaseData caseData2 = getCaseDataForApp1(dateNotPast14Days);

        Map<String, Object> caseDataMap1 = getCaseDataMapForApp1(datePast14Days);
        Map<String, Object> caseDataMap2 = getCaseDataMapForApp1(dateNotPast14Days);

        when(caseDetails1.getData()).thenReturn(caseDataMap1);
        when(caseDetails2.getData()).thenReturn(caseDataMap1);
        when(caseDetails3.getData()).thenReturn(caseDataMap2);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(1616591401473377L);

        when(objectMapper.convertValue(caseDataMap1, CaseData.class)).thenReturn(caseData1);
        when(objectMapper.convertValue(caseDataMap2, CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2, caseDetails3);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        task.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(1616591401473377L,
            SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION,
            user,
            SERVICE_AUTHORIZATION
        );
        verifyNoMoreInteractions(ccdUpdateService);
    }

    @Test
    void shouldTriggerSystemUpdateTaskOnEachCaseWhenFinalOrderApp2DateDeclaredIntentionSwitchToSoleIsPastFourteenDays() {
        final LocalDate datePast14Days = NOW.minusDays(15);
        final LocalDate dateNotPast14Days = NOW.minusDays(6);
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final CaseDetails caseDetails3 = mock(CaseDetails.class);

        final CaseData caseData1 = getCaseDataForApp2(datePast14Days);
        final CaseData caseData2 = getCaseDataForApp2(dateNotPast14Days);

        Map<String, Object> caseDataMap1 = getCaseDataMapForApp2(datePast14Days);
        Map<String, Object> caseDataMap2 = getCaseDataMapForApp2(dateNotPast14Days);

        when(caseDetails1.getData()).thenReturn(caseDataMap1);
        when(caseDetails2.getData()).thenReturn(caseDataMap1);
        when(caseDetails3.getData()).thenReturn(caseDataMap2);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(1616591401473377L);

        when(objectMapper.convertValue(caseDataMap1, CaseData.class)).thenReturn(caseData1);
        when(objectMapper.convertValue(caseDataMap2, CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2, caseDetails3);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        task.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(1616591401473377L,
            SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION,
            user,
            SERVICE_AUTHORIZATION
        );
        verifyNoMoreInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotTriggerSystemUpdateTaskOnCaseWhenCaseApp1DateDeclaredIntentionIsNotPastFourteenDays() {
        final LocalDate date = NOW.minusDays(6);
        final CaseDetails caseDetails = mock(CaseDetails.class);

        Map<String, Object> caseDataMap = getCaseDataMapForApp1(date);

        when(caseDetails.getData()).thenReturn(caseDataMap);

        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(getCaseDataForApp1(date));

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(singletonList(caseDetails));

        task.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotTriggerSystemUpdateTaskOnCaseWhenCaseApp2DateDeclaredIntentionIsNotPastFourteenDays() {
        final LocalDate date = NOW.minusDays(6);
        final CaseDetails caseDetails = mock(CaseDetails.class);

        Map<String, Object> caseDataMap = getCaseDataMapForApp2(date);

        when(caseDetails.getData()).thenReturn(caseDataMap);

        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(getCaseDataForApp2(date));

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(singletonList(caseDetails));

        task.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        task.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final LocalDate date = NOW.minusDays(15);
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        Map<String, Object> caseDataMap = getCaseDataMapForApp1(date);

        when(caseDetails1.getData()).thenReturn(caseDataMap);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);

        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(getCaseDataForApp1(date));

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION, user, SERVICE_AUTHORIZATION);

        task.run();

        verify(ccdUpdateService)
            .submitEvent(TEST_CASE_ID, SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(1616591401473377L, SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final LocalDate date = NOW.minusDays(15);
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", NOW.toString()));
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", NOW.minusDays(5).toString()));

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        Map<String, Object> caseDataMap = getCaseDataMapForApp1(date);

        when(caseDetails1.getData()).thenReturn(caseDataMap);
        when(caseDetails2.getData()).thenReturn(caseDataMap);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(1616591401473379L);

        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(getCaseDataForApp1(date));


        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION, user, SERVICE_AUTHORIZATION);

        task.run();

        verify(ccdUpdateService).submitEvent(
            1616591401473379L,
            SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION,
            user,
            SERVICE_AUTHORIZATION
        );
        verifyNoMoreInteractions(ccdUpdateService);
    }

    private Map<String, Object> getCaseDataMapForApp1(final LocalDate dateApplicant1SubmittedIntentionToSts) {
        return Map.of(
            "doesApplicant1IntendToSwitchToSole", YES,
            "dateApplicant1DeclaredIntentionToSwitchToSoleFo", dateApplicant1SubmittedIntentionToSts,
            "finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention", NO
        );
    }

    private Map<String, Object> getCaseDataMapForApp2(final LocalDate dateApplicant2SubmittedIntentionToSts) {
        return Map.of(
            "doesApplicant2IntendToSwitchToSole", YES,
            "dateApplicant2DeclaredIntentionToSwitchToSoleFo", dateApplicant2SubmittedIntentionToSts,
            "finalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention", NO
        );
    }

    private CaseData getCaseDataForApp1(final LocalDate dateApplicant1SubmittedIntentionToSts) {
        return CaseData.builder()
            .finalOrder(
                FinalOrder.builder()
                    .doesApplicant1IntendToSwitchToSole(YES)
                    .dateApplicant1DeclaredIntentionToSwitchToSoleFo(dateApplicant1SubmittedIntentionToSts)
                    .finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention(NO)
                    .build()
            )
            .build();
    }

    private CaseData getCaseDataForApp2(final LocalDate dateApplicant2SubmittedIntentionToSts) {
        return CaseData.builder()
            .finalOrder(
                FinalOrder.builder()
                    .doesApplicant2IntendToSwitchToSole(YES)
                    .dateApplicant2DeclaredIntentionToSwitchToSoleFo(dateApplicant2SubmittedIntentionToSts)
                    .finalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention(NO)
                    .build()
            )
            .build();
    }
}
