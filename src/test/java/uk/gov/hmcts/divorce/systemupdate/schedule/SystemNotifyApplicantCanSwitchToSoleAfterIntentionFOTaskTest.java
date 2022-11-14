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
import static org.springframework.cloud.contract.spec.internal.HttpStatus.REQUEST_TIMEOUT;
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

@ExtendWith(MockitoExtension.class)
public class SystemNotifyApplicantCanSwitchToSoleAfterIntentionFOTaskTest {

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
            .mustNot((boolQuery()
                .should(matchQuery(String.format(DATA, APP_1_NOTIFICATION_SENT_FLAG), YES))
                .should(matchQuery(String.format(DATA, APP_2_NOTIFICATION_SENT_FLAG), YES))
                .minimumShouldMatch(1)
            ));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldTriggerSystemUpdateTaskOnEachCaseWhenFinalOrderApp1DateDeclaredIntentionSwitchToSoleIsPastFourteenDays() {
        final LocalDate datePast14Days = LocalDate.now().minusDays(15);
        final LocalDate dateNotPast14Days = LocalDate.now().minusDays(6);
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final CaseDetails caseDetails3 = mock(CaseDetails.class);
        final CaseData caseData1 = CaseData.builder()
            .finalOrder(
                FinalOrder.builder()
                    .dateApplicant1DeclaredIntentionToSwitchToSoleFo(datePast14Days)
                    .finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention(NO)
                    .build()
            )
            .build();
        final CaseData caseData2 = CaseData.builder()
            .finalOrder(
                FinalOrder.builder()
                    .dateApplicant1DeclaredIntentionToSwitchToSoleFo(dateNotPast14Days)
                    .finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention(NO)
                    .build()
            )
            .build();

        when(caseDetails1.getData()).thenReturn(
            Map.of(
                "dateApplicant1DeclaredIntentionToSwitchToSoleFo", datePast14Days,
                "finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention", NO
            )
        );
        when(caseDetails2.getData()).thenReturn(
            Map.of(
                "dateApplicant1DeclaredIntentionToSwitchToSoleFo", datePast14Days,
                "finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention", NO
            )
        );
        when(caseDetails3.getData()).thenReturn(
            Map.of(
                "dateApplicant1DeclaredIntentionToSwitchToSoleFo", dateNotPast14Days,
                "finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention", NO
            )
        );

        when(objectMapper.convertValue(Map.of(
            "dateApplicant1DeclaredIntentionToSwitchToSoleFo", datePast14Days,
            "finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention", NO), CaseData.class)).thenReturn(caseData1);
        when(objectMapper.convertValue(Map.of(
            "dateApplicant1DeclaredIntentionToSwitchToSoleFo", dateNotPast14Days,
            "finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention", NO), CaseData.class)).thenReturn(caseData2);


        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2, caseDetails3);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        task.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION, user, SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(ccdUpdateService);
    }

    @Test
    void shouldTriggerSystemUpdateTaskOnEachCaseWhenFinalOrderApp2DateDeclaredIntentionSwitchToSoleIsPastFourteenDays() {
        final LocalDate datePast14Days = LocalDate.now().minusDays(15);
        final LocalDate dateNotPast14Days = LocalDate.now().minusDays(6);
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final CaseDetails caseDetails3 = mock(CaseDetails.class);
        final CaseData caseData1 = CaseData.builder()
            .finalOrder(
                FinalOrder.builder()
                    .dateApplicant2DeclaredIntentionToSwitchToSoleFo(datePast14Days)
                    .finalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention(NO)
                    .build()
            )
            .build();
        final CaseData caseData2 = CaseData.builder()
            .finalOrder(
                FinalOrder.builder()
                    .dateApplicant2DeclaredIntentionToSwitchToSoleFo(dateNotPast14Days)
                    .finalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention(NO)
                    .build()
            )
            .build();

        when(caseDetails1.getData()).thenReturn(
            Map.of(
                "dateApplicant2DeclaredIntentionToSwitchToSoleFo", datePast14Days,
                "finalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention", NO
            )
        );
        when(caseDetails2.getData()).thenReturn(
            Map.of(
                "dateApplicant2DeclaredIntentionToSwitchToSoleFo", datePast14Days,
                "finalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention", NO
            )
        );
        when(caseDetails3.getData()).thenReturn(
            Map.of(
                "dateApplicant2DeclaredIntentionToSwitchToSoleFo", dateNotPast14Days,
                "finalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention", NO
            )
        );

        when(objectMapper.convertValue(Map.of(
            "dateApplicant2DeclaredIntentionToSwitchToSoleFo", datePast14Days,
            "finalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention", NO), CaseData.class)).thenReturn(caseData1);
        when(objectMapper.convertValue(Map.of(
            "dateApplicant2DeclaredIntentionToSwitchToSoleFo", dateNotPast14Days,
            "finalOrderApplicant2NotifiedCanSwitchToSoleAfterIntention", NO), CaseData.class)).thenReturn(caseData2);


        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2, caseDetails3);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        task.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION, user, SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotTriggerSystemUpdateTaskOnCaseWhenCaseApp1DateDeclaredIntentionIsNotPastFourteenDays() {
        final LocalDate date = LocalDate.now().minusDays(6);
        final CaseDetails caseDetails = mock(CaseDetails.class);
        final CaseData caseData = CaseData.builder()
            .finalOrder(
                FinalOrder.builder()
                    .dateApplicant1DeclaredIntentionToSwitchToSoleFo(date)
                    .finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention(NO)
                    .build()
            )
            .build();

        when(caseDetails.getData()).thenReturn(
            Map.of(
                "dateApplicant1DeclaredIntentionToSwitchToSoleFo", date,
                "finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention", NO
            )
        );

        when(objectMapper.convertValue(Map.of(
            "dateApplicant1DeclaredIntentionToSwitchToSoleFo", date,
            "finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention", NO), CaseData.class)).thenReturn(caseData);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(singletonList(caseDetails));

        task.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotTriggerSystemUpdateTaskOnCaseWhenCaseApp2DateDeclaredIntentionIsNotPastFourteenDays() {
        final LocalDate date = LocalDate.now().minusDays(6);
        final CaseDetails caseDetails = mock(CaseDetails.class);
        final CaseData caseData = CaseData.builder()
            .finalOrder(
                FinalOrder.builder()
                    .dateApplicant2DeclaredIntentionToSwitchToSoleFo(date)
                    .finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention(NO)
                    .build()
            )
            .build();

        when(caseDetails.getData()).thenReturn(
            Map.of(
                "dateApplicant2DeclaredIntentionToSwitchToSoleFo", date,
                "finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention", NO
            )
        );

        when(objectMapper.convertValue(Map.of(
            "dateApplicant2DeclaredIntentionToSwitchToSoleFo", date,
            "finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention", NO), CaseData.class)).thenReturn(caseData);

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
        final LocalDate date = LocalDate.now().minusDays(15);
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);
        final CaseData caseData = CaseData.builder()
            .finalOrder(
                FinalOrder.builder()
                    .dateApplicant1DeclaredIntentionToSwitchToSoleFo(date)
                    .finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention(NO)
                    .build()
            )
            .build();

        when(caseDetails1.getData()).thenReturn(
            Map.of(
                "dateApplicant1DeclaredIntentionToSwitchToSoleFo", date,
                "finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention", NO
            )
        );

        when(objectMapper.convertValue(Map.of(
            "dateApplicant1DeclaredIntentionToSwitchToSoleFo", date,
            "finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention", NO), CaseData.class)).thenReturn(caseData);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION, user, SERVICE_AUTHORIZATION);

        task.run();

        verify(ccdUpdateService)
            .submitEvent(caseDetails1, SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(caseDetails2, SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final LocalDate date = LocalDate.now().minusDays(15);
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final CaseData caseData = CaseData.builder()
            .finalOrder(
                FinalOrder.builder()
                    .dateApplicant1DeclaredIntentionToSwitchToSoleFo(date)
                    .finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention(NO)
                    .build()
            )
            .build();

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().toString()));
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", LocalDate.now().minusDays(5).toString()));

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(caseDetails1.getData()).thenReturn(
            Map.of(
                "dateApplicant1DeclaredIntentionToSwitchToSoleFo", date,
                "finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention", NO
            )
        );

        when(caseDetails2.getData()).thenReturn(
            Map.of(
                "dateApplicant1DeclaredIntentionToSwitchToSoleFo", date,
                "finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention", NO
            )
        );

        when(objectMapper.convertValue(Map.of(
            "dateApplicant1DeclaredIntentionToSwitchToSoleFo", date,
            "finalOrderApplicant1NotifiedCanSwitchToSoleAfterIntention", NO), CaseData.class)).thenReturn(caseData);


        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION, user, SERVICE_AUTHORIZATION);

        task.run();

        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION, user, SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(ccdUpdateService);
    }
}
