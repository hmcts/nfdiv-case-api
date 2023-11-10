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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
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
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyApplicantPartnerNotAppliedForFinalOrder.SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemNotifyApplicantThatPartnerNotAppliedForFOTask.NOTIFICATION_SENT_FLAG;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.FINAL_ORDER_SUBMITTED_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class SystemNotifyApplicantThatPartnerNotAppliedForFOTaskTest {

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
    private SystemNotifyApplicantThatPartnerNotAppliedForFOTask task;

    private User user;

    private static final BoolQueryBuilder query =
        boolQuery()
            .must(matchQuery(STATE, AwaitingJointFinalOrder))
            .must(existsQuery(FINAL_ORDER_SUBMITTED_DATE))
            .mustNot(matchQuery(String.format(DATA, NOTIFICATION_SENT_FLAG), YES));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldTriggerSystemUpdateTaskOnEachCaseWhenCaseDateFinalOrderSubmittedIsPastFourteenDays() {
        final LocalDateTime dateTimePast14Days = LocalDateTime.now().minusDays(15);
        final LocalDateTime dateTimeNotPast14Days = LocalDateTime.now().minusDays(6);
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final CaseDetails caseDetails3 = mock(CaseDetails.class);
        final CaseData caseData1 = CaseData.builder()
            .finalOrder(
                FinalOrder.builder()
                    .dateFinalOrderSubmitted(dateTimePast14Days)
                    .finalOrderFirstInTimeNotifiedOtherApplicantNotApplied(NO)
                    .build()
            )
            .build();
        final CaseData caseData2 = CaseData.builder()
            .finalOrder(
                FinalOrder.builder()
                    .dateFinalOrderSubmitted(dateTimeNotPast14Days)
                    .finalOrderFirstInTimeNotifiedOtherApplicantNotApplied(NO)
                    .build()
            )
            .build();

        when(caseDetails1.getData()).thenReturn(
            Map.of(
                "dateFinalOrderSubmitted", dateTimePast14Days,
                "finalOrderFirstInTimeNotifiedOtherApplicantNotApplied", NO
            )
        );
        when(caseDetails2.getData()).thenReturn(
            Map.of(
                "dateFinalOrderSubmitted", dateTimePast14Days,
                "finalOrderFirstInTimeNotifiedOtherApplicantNotApplied", NO
            )
        );
        when(caseDetails3.getData()).thenReturn(
            Map.of(
                "dateFinalOrderSubmitted", dateTimeNotPast14Days,
                "finalOrderFirstInTimeNotifiedOtherApplicantNotApplied", NO
            )
        );

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(2L);

        when(objectMapper.convertValue(Map.of(
            "dateFinalOrderSubmitted", dateTimePast14Days,
            "finalOrderFirstInTimeNotifiedOtherApplicantNotApplied", NO), CaseData.class)).thenReturn(caseData1);
        when(objectMapper.convertValue(Map.of(
            "dateFinalOrderSubmitted", dateTimeNotPast14Days,
            "finalOrderFirstInTimeNotifiedOtherApplicantNotApplied", NO), CaseData.class)).thenReturn(caseData2);


        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2, caseDetails3);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        task.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotTriggerSystemUpdateTaskOnCaseWhenCaseDateFinalOrderSubmittedIsNotPastFourteenDays() {
        final LocalDateTime dateTime = LocalDateTime.now().minusDays(6);
        final CaseDetails caseDetails = mock(CaseDetails.class);
        final CaseData caseData = CaseData.builder()
            .finalOrder(
                FinalOrder.builder()
                    .dateFinalOrderSubmitted(dateTime)
                    .finalOrderFirstInTimeNotifiedOtherApplicantNotApplied(NO)
                    .build()
            )
            .build();

        when(caseDetails.getData()).thenReturn(
            Map.of(
                "dateFinalOrderSubmitted", dateTime,
                "finalOrderFirstInTimeNotifiedOtherApplicantNotApplied", NO
            )
        );

        when(objectMapper.convertValue(Map.of(
            "dateFinalOrderSubmitted", dateTime,
            "finalOrderFirstInTimeNotifiedOtherApplicantNotApplied", NO), CaseData.class)).thenReturn(caseData);

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
        final LocalDateTime dateTime = LocalDateTime.now().minusDays(15);
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);
        final CaseData caseData = CaseData.builder()
            .finalOrder(
                FinalOrder.builder()
                    .dateFinalOrderSubmitted(dateTime)
                    .finalOrderFirstInTimeNotifiedOtherApplicantNotApplied(NO)
                    .build()
            )
            .build();

        when(caseDetails1.getData()).thenReturn(
            Map.of(
                "dateFinalOrderSubmitted", dateTime,
                "finalOrderFirstInTimeNotifiedOtherApplicantNotApplied", NO
            )
        );

        when(objectMapper.convertValue(Map.of(
            "dateFinalOrderSubmitted", dateTime,
            "finalOrderFirstInTimeNotifiedOtherApplicantNotApplied", NO), CaseData.class)).thenReturn(caseData);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER, user, SERVICE_AUTHORIZATION);

        task.run();

        verify(ccdUpdateService)
            .submitEvent(TEST_CASE_ID, SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(2L, SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final LocalDateTime dateTime = LocalDateTime.now().minusDays(15);
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final CaseData caseData = CaseData.builder()
            .finalOrder(
                FinalOrder.builder()
                    .dateFinalOrderSubmitted(dateTime)
                    .finalOrderFirstInTimeNotifiedOtherApplicantNotApplied(NO)
                    .build()
            )
            .build();

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now().toString()));
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", LocalDate.now().minusDays(5).toString()));

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(caseDetails1.getData()).thenReturn(
            Map.of(
                "dateFinalOrderSubmitted", dateTime,
                "finalOrderFirstInTimeNotifiedOtherApplicantNotApplied", NO
            )
        );

        when(caseDetails2.getData()).thenReturn(
            Map.of(
                "dateFinalOrderSubmitted", dateTime,
                "finalOrderFirstInTimeNotifiedOtherApplicantNotApplied", NO
            )
        );

        when(objectMapper.convertValue(Map.of(
            "dateFinalOrderSubmitted", dateTime,
            "finalOrderFirstInTimeNotifiedOtherApplicantNotApplied", NO), CaseData.class)).thenReturn(caseData);


        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        when(caseDetails1.getId()).thenReturn(TEST_CASE_ID);
        when(caseDetails2.getId()).thenReturn(2L);

        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER, user, SERVICE_AUTHORIZATION);

        task.run();

        verify(ccdUpdateService).submitEvent(2L, SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(ccdUpdateService);
    }
}
