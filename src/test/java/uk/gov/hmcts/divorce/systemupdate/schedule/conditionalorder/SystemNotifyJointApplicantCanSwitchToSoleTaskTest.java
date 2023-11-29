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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyJointApplicantCanSwitchToSole.SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder.SystemNotifyJointApplicantCanSwitchToSoleTask.NOTIFICATION_FLAG;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

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

    @InjectMocks
    private SystemNotifyJointApplicantCanSwitchToSoleTask notifyJointApplicantCanSwitchToSoleTask;

    private User user;

    private static final LocalDate NOW = LocalDate.now();

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
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        ReflectionTestUtils.setField(notifyJointApplicantCanSwitchToSoleTask,
            "submitCOrderReminderOffsetDays", DUE_DATE_OFFSET_DAYS);
    }

    @Test
    void shouldSubmitNotifySwitchToSoleEventWhenJointConditionalOrderOverdueFromApplicant2() {

        CaseDetails caseDetails1 = CaseDetails.builder()
            .data(new HashMap<>())
            .id(TEST_CASE_ID).build();

        CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();

        List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .submittedDate(NOW.minusDays(15).atStartOfDay())
                    .isSubmitted(YesOrNo.YES)
                    .build())
                .build())
            .build();
        when(mapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData);
        when(mapper.convertValue(caseDetails2.getData(), CaseData.class)).thenReturn(CaseData.builder().build());

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, ConditionalOrderPending))
            .thenReturn(caseDetailsList);

        notifyJointApplicantCanSwitchToSoleTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, user, SERVICE_AUTHORIZATION);

        verifyNoMoreInteractions(ccdUpdateService);
    }

    @Test
    void shouldSubmitNotifySwitchToSoleEventWhenJointConditionalOrderOverdueFromApplicant1() {

        CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .submittedDate(NOW.minusDays(15).atStartOfDay())
                    .isSubmitted(YesOrNo.YES)
                    .build())
                .build())
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("coApplicant2IsSubmitted", "Yes");
        data.put("coApplicant2SubmittedDate", "2022-07-07T00:00:00.000Z");

        CaseDetails caseDetails1 = CaseDetails.builder()
            .data(data)
            .id(TEST_CASE_ID).build();

        CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();

        List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(mapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData);
        when(mapper.convertValue(caseDetails2.getData(), CaseData.class)).thenReturn(CaseData.builder().build());

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, ConditionalOrderPending))
            .thenReturn(caseDetailsList);

        notifyJointApplicantCanSwitchToSoleTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, user, SERVICE_AUTHORIZATION);

        verifyNoMoreInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitNotifySwitchToSoleEventWhenJointConditionalOrderIsNotYetOverdue() {

        Map<String, Object> data = new HashMap<>();
        data.put("coApplicant1IsSubmitted", "Yes");
        data.put("coApplicant1SubmittedDate", "2022-07-20T00:00:00.000Z");

        CaseDetails caseDetails1 = CaseDetails.builder()
            .data(data)
            .id(TEST_CASE_ID).build();

        CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();

        List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .submittedDate(NOW.minusDays(2).atStartOfDay())
                    .isSubmitted(YesOrNo.YES)
                    .build())
                .build())
            .build();

        when(mapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData);
        when(mapper.convertValue(caseDetails2.getData(), CaseData.class)).thenReturn(CaseData.builder().build());

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, ConditionalOrderPending))
            .thenReturn(caseDetailsList);

        notifyJointApplicantCanSwitchToSoleTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitNotifySwitchToSoleEventWhenCOSubmittedDateIsNull() {

        Map<String, Object> data = new HashMap<>();
        data.put("coApplicant1IsSubmitted", "Yes");
        data.put("coApplicant1SubmittedDate", null);

        CaseDetails caseDetails1 = CaseDetails.builder()
            .data(data)
            .id(TEST_CASE_ID).build();

        CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();

        List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .submittedDate(null)
                    .isSubmitted(YesOrNo.YES)
                    .build())
                .build())
            .build();

        when(mapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData);
        when(mapper.convertValue(caseDetails2.getData(), CaseData.class)).thenReturn(CaseData.builder().build());

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, ConditionalOrderPending))
            .thenReturn(caseDetailsList);

        notifyJointApplicantCanSwitchToSoleTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, ConditionalOrderPending))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        notifyJointApplicantCanSwitchToSoleTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {

        Map<String, Object> data = new HashMap<>();
        data.put("coApplicant2IsSubmitted", "Yes");
        data.put("coApplicant2SubmittedDate", "2022-07-07T00:00:00.000Z");

        CaseDetails caseDetails1 = CaseDetails.builder()
            .data(data)
            .id(TEST_CASE_ID).build();

        CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();

        List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .submittedDate(NOW.minusDays(15).atStartOfDay())
                    .isSubmitted(YesOrNo.YES)
                    .build())
                .build())
            .build();

        when(mapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData);

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, ConditionalOrderPending))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService)
            .submitEvent(TEST_CASE_ID, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, user, SERVICE_AUTHORIZATION);

        notifyJointApplicantCanSwitchToSoleTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(2L, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {

        Map<String, Object> data1 = new HashMap<>();
        data1.put("coApplicant1IsSubmitted", "Yes");
        data1.put("coApplicant1SubmittedDate", "2022-07-07T00:00:00.000Z");

        CaseDetails caseDetails1 = CaseDetails.builder()
            .data(data1)
            .id(TEST_CASE_ID).build();

        Map<String, Object> data2 = new HashMap<>();
        data2.put("coApplicant2IsSubmitted", "Yes");
        data2.put("coApplicant2SubmittedDate", "2022-07-01T00:00:00.000Z");

        CaseDetails caseDetails2 = CaseDetails.builder().id(2L)
            .data(data2)
            .build();

        List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        CaseData caseData1 = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .submittedDate(NOW.minusDays(15).atStartOfDay())
                    .isSubmitted(YesOrNo.YES)
                    .build())
                .build())
            .build();

        CaseData caseData2 = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .submittedDate(NOW.minusDays(20).atStartOfDay())
                    .isSubmitted(YesOrNo.YES)
                    .build())
                .build())
            .build();

        when(mapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(caseDetails2.getData(), CaseData.class)).thenReturn(caseData2);

        when(ccdSearchService.searchForAllCasesWithQuery(
            query, user, SERVICE_AUTHORIZATION, ConditionalOrderPending))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService)
            .submitEvent(TEST_CASE_ID, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, user, SERVICE_AUTHORIZATION);

        notifyJointApplicantCanSwitchToSoleTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_NOTIFY_JOINT_APPLICANT_CAN_SWITCH_TO_SOLE, user, SERVICE_AUTHORIZATION);
    }
}
