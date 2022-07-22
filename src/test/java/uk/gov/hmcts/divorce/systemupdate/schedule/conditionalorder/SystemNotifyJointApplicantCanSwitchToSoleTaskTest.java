package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.divorce.testutil.ClockTestUtil;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.contract.spec.internal.HttpStatus.REQUEST_TIMEOUT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyJointApplicantCanSwitchToSole.SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder.SystemNotifyJointApplicantCanSwitchToSoleTask.NOTIFICATION_FLAG;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class SystemNotifyJointApplicantCanSwitchToSoleTaskTest {

    private static final int DUE_DATE_OFFSET_DAYS = 14;
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

    @Mock
    private Clock clock;

    @InjectMocks
    private SystemNotifyJointApplicantCanSwitchToSoleTask notifyJointApplicantCanSwitchToSoleTask;

    private User user;

    private static final BoolQueryBuilder query =
        boolQuery()
            .must(
                boolQuery()
                    .should(matchQuery(STATE, ConditionalOrderPending))
                    .minimumShouldMatch(1)
            )
            .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YesOrNo.YES));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        ReflectionTestUtils.setField(notifyJointApplicantCanSwitchToSoleTask, "submitCOrderReminderOffsetDays", DUE_DATE_OFFSET_DAYS);
    }

    @Test
    void shouldSendEmailForConditionalOrder() {
        final CaseDetails caseDetails1 = CaseDetails.builder().id(1L).build();
        final CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, ConditionalOrderPending))
            .thenReturn(caseDetailsList);

        notifyJointApplicantCanSwitchToSoleTask.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingConditionalOrder, ConditionalOrderPending, ConditionalOrderDrafted))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        notifyJointApplicantCanSwitchToSoleTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = CaseDetails.builder().id(1L).build();
        final CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingConditionalOrder, ConditionalOrderPending, ConditionalOrderDrafted))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService)
            .submitEvent(caseDetails1, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, user, SERVICE_AUTHORIZATION);

        notifyJointApplicantCanSwitchToSoleTask.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(caseDetails2, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = CaseDetails.builder().id(1L).build();
        final CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, AwaitingConditionalOrder, ConditionalOrderPending, ConditionalOrderDrafted))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService)
            .submitEvent(caseDetails1, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, user, SERVICE_AUTHORIZATION);

        notifyJointApplicantCanSwitchToSoleTask.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    public void test() {
        ClockTestUtil.setMockClock(clock);
        when(mapper.convertValue(any(), CaseData.class)).thenReturn(CaseData.builder()
                .conditionalOrder(ConditionalOrder.builder()
                    .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                        .submittedDate(LocalDate.of(2022, 7, 20).atStartOfDay())
                        .isSubmitted(YesOrNo.YES)
                        .build())
                    .build())
            .build());
        notifyJointApplicantCanSwitchToSoleTask.isJointConditionalOrderOverdue(CaseDetails.builder().id(1L).build());
    }
}
