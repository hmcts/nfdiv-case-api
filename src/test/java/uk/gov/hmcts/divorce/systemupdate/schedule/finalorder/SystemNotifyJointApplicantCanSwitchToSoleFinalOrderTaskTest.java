package uk.gov.hmcts.divorce.systemupdate.schedule.finalorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.HashMap;
import java.util.List;

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
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyJointApplicantCanSwitchToSoleFinalOrder.SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE_FINAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.schedule.finalorder.SystemNotifyJointApplicantCanSwitchToSoleFinalOrderTask.NOTIFICATION_FLAG_FO;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class SystemNotifyJointApplicantCanSwitchToSoleFinalOrderTaskTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private ObjectMapper mapper;

    @Value("${final_order.reminder_offset_days}")
    private int finalOrderReminderOffsetDays;

    @InjectMocks
    private SystemNotifyJointApplicantCanSwitchToSoleFinalOrderTask systemNotifyJointApplicantCanSwitchToSoleFinalOrderTask;

    private User user;

    private static final LocalDate NOW = LocalDate.now();

    private static final BoolQueryBuilder query =
        boolQuery()
            .must(
                boolQuery()
                    .should(matchQuery(STATE, AwaitingJointFinalOrder))
                    .minimumShouldMatch(1)
            )
            .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG_FO), YES));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldSubmitNotifySwitchToSoleFinalOrderEventWhenJointFinalOrderOverdueFromApplicant2() {

        CaseDetails caseDetails1 = CaseDetails.builder()
            .data(new HashMap<>())
            .id(1L).build();

        CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();

        List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder()
                .dateFinalOrderSubmitted(NOW.minusDays(15).atStartOfDay())
                .applicant1SubmittedFinalOrder(YES)
                .build())
            .build();
        when(mapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData);
        when(mapper.convertValue(caseDetails2.getData(), CaseData.class)).thenReturn(CaseData.builder().build());

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        systemNotifyJointApplicantCanSwitchToSoleFinalOrderTask.run();

        verify(ccdUpdateService).submitEvent(
            caseDetails1,
            SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE_FINAL_ORDER,
            user,
            SERVICE_AUTHORIZATION
        );

        verifyNoMoreInteractions(ccdUpdateService);
    }

    @Test
    void shouldSubmitNotifySwitchToSoleFinalOrderEventWhenJointFinalOrderOverdueFromApplicant1() {

        CaseDetails caseDetails1 = CaseDetails.builder()
            .data(new HashMap<>())
            .id(1L).build();

        CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();

        List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder()
                .dateFinalOrderSubmitted(NOW.minusDays(15).atStartOfDay())
                .applicant2SubmittedFinalOrder(YES)
                .build())
            .build();
        when(mapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData);
        when(mapper.convertValue(caseDetails2.getData(), CaseData.class)).thenReturn(CaseData.builder().build());

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        systemNotifyJointApplicantCanSwitchToSoleFinalOrderTask.run();

        verify(ccdUpdateService).submitEvent(
            caseDetails1,
            SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE_FINAL_ORDER,
            user,
            SERVICE_AUTHORIZATION
        );

        verifyNoMoreInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitNotifySwitchToSoleFinalOrderEventWhenJointFinalOrderIsNotOverdue() {

        CaseDetails caseDetails1 = CaseDetails.builder()
            .data(new HashMap<>())
            .id(1L).build();

        CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();

        List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder()
                .dateFinalOrderSubmitted(NOW.minusDays(1).atStartOfDay())
                .applicant1SubmittedFinalOrder(YES)
                .build())
            .build();
        when(mapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData);
        when(mapper.convertValue(caseDetails2.getData(), CaseData.class)).thenReturn(CaseData.builder().build());

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        systemNotifyJointApplicantCanSwitchToSoleFinalOrderTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitNotifySwitchToSoleFinalOrderEventWhenFinalOrderSubmittedDateIsNull() {

        CaseDetails caseDetails1 = CaseDetails.builder()
            .data(new HashMap<>())
            .id(1L).build();

        CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();

        List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        CaseData caseData = CaseData.builder().finalOrder(FinalOrder.builder().build()).build();

        when(mapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData);
        when(mapper.convertValue(caseDetails2.getData(), CaseData.class)).thenReturn(CaseData.builder().build());

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        systemNotifyJointApplicantCanSwitchToSoleFinalOrderTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemNotifyJointApplicantCanSwitchToSoleFinalOrderTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {

        CaseDetails caseDetails1 = CaseDetails.builder()
            .data(new HashMap<>())
            .id(1L).build();

        CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();

        List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder()
                .dateFinalOrderSubmitted(NOW.minusDays(15).atStartOfDay())
                .applicant2SubmittedFinalOrder(YES)
                .build())
            .build();

        when(mapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData);

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService)
            .submitEvent(caseDetails1, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE_FINAL_ORDER, user, SERVICE_AUTHORIZATION);

        systemNotifyJointApplicantCanSwitchToSoleFinalOrderTask.run();

        verify(ccdUpdateService).submitEvent(
            caseDetails1,
            SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE_FINAL_ORDER,
            user,
            SERVICE_AUTHORIZATION
        );
        verify(ccdUpdateService, never())
            .submitEvent(caseDetails2, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE_FINAL_ORDER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {

        CaseDetails caseDetails1 = CaseDetails.builder()
            .data(new HashMap<>())
            .id(1L).build();

        CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();

        CaseData caseData1 = CaseData.builder()
            .finalOrder(FinalOrder.builder()
                .dateFinalOrderSubmitted(NOW.minusDays(15).atStartOfDay())
                .applicant1SubmittedFinalOrder(YES)
                .build())
            .build();

        CaseData caseData2 = CaseData.builder()
            .finalOrder(FinalOrder.builder()
                .dateFinalOrderSubmitted(NOW.minusDays(15).atStartOfDay())
                .applicant2SubmittedFinalOrder(YES)
                .build())
            .build();

        List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(mapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(caseDetails2.getData(), CaseData.class)).thenReturn(caseData2);

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingJointFinalOrder))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService)
            .submitEvent(caseDetails1, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE_FINAL_ORDER, user, SERVICE_AUTHORIZATION);

        systemNotifyJointApplicantCanSwitchToSoleFinalOrderTask.run();

        verify(ccdUpdateService).submitEvent(
            caseDetails1,
            SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE_FINAL_ORDER,
            user,
            SERVICE_AUTHORIZATION
        );

        verify(ccdUpdateService).submitEvent(
            caseDetails2,
            SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE_FINAL_ORDER,
            user,
            SERVICE_AUTHORIZATION
        );
    }
}
