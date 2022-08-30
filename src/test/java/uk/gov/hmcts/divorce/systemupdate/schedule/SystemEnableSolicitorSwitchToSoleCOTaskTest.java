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
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.contract.spec.internal.HttpStatus.REQUEST_TIMEOUT;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemEnableSolicitorSwitchToSoleCO.SYSTEM_ENABLE_SWITCH_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class SystemEnableSolicitorSwitchToSoleCOTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemEnableSolicitorSwitchToSoleCOTask task;

    private User user;

    private static final BoolQueryBuilder query =
        boolQuery().must(matchQuery(STATE, ConditionalOrderPending));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldTriggerAwaitingFinalOrderTaskOnEachCaseWhenDateFinalOrderEligibleFromIsBeforeOrSameAsCurrentDate() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        Map<String, Object> dataMap1 = Map.of(
            "coApplicant1IsSubmitted", YES,
            "coApplicant2IsSubmitted", NO,
            "coApplicant1SubmittedDate", LocalDate.now().minusDays(15).toString()
        );

        Map<String, Object> dataMap2 = Map.of(
            "coApplicant2IsSubmitted", YES,
            "coApplicant1IsSubmitted", NO,
            "coApplicant2SubmittedDate", LocalDate.now().minusDays(15).toString()
        );

        final CaseData caseData1 = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .conditionalOrderApplicant1Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(YES)
                            .submittedDate(LocalDateTime.now().minusDays(15))
                            .build()
                    )
                    .conditionalOrderApplicant2Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(NO)
                            .build()
                    )
                    .build()
            )
            .build();

        final CaseData caseData2 = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .conditionalOrderApplicant1Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(NO)
                            .build()
                    )
                    .conditionalOrderApplicant2Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(YES)
                            .submittedDate(LocalDateTime.now().minusDays(15))
                            .build()
                    )
                    .build()
            )
            .build();

        when(caseDetails1.getData()).thenReturn(dataMap1);
        when(caseDetails2.getData()).thenReturn(dataMap2);

        when(mapper.convertValue(dataMap1, CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(dataMap2, CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPending))
            .thenReturn(caseDetailsList);

        task.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_ENABLE_SWITCH_TO_SOLE_CO, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_ENABLE_SWITCH_TO_SOLE_CO, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotEnableSolicitorSwitchToSoleCoIfSubmissionWasLessThanFourteenDays() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        Map<String, Object> dataMap1 = Map.of(
            "coApplicant1IsSubmitted", YES,
            "coApplicant2IsSubmitted", NO,
            "coApplicant1SubmittedDate", LocalDate.now().minusDays(10).toString()
        );

        Map<String, Object> dataMap2 = Map.of(
            "coApplicant2IsSubmitted", YES,
            "coApplicant1IsSubmitted", NO,
            "coApplicant2SubmittedDate", LocalDate.now().minusDays(10).toString()
        );

        final CaseData caseData1 = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .conditionalOrderApplicant1Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(YES)
                            .submittedDate(LocalDateTime.now().minusDays(10))
                            .build()
                    )
                    .conditionalOrderApplicant2Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(NO)
                            .build()
                    )
                    .build()
            )
            .build();

        final CaseData caseData2 = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .conditionalOrderApplicant1Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(NO)
                            .build()
                    )
                    .conditionalOrderApplicant2Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(YES)
                            .submittedDate(LocalDateTime.now().minusDays(10))
                            .build()
                    )
                    .build()
            )
            .build();

        when(caseDetails1.getData()).thenReturn(dataMap1);
        when(caseDetails2.getData()).thenReturn(dataMap2);

        when(mapper.convertValue(dataMap1, CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(dataMap2, CaseData.class)).thenReturn(caseData2);


        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPending))
            .thenReturn(caseDetailsList);

        task.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPending))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        task.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        Map<String, Object> dataMap1 = Map.of(
            "coApplicant1IsSubmitted", YES,
            "coApplicant2IsSubmitted", NO,
            "coApplicant1SubmittedDate", LocalDate.now().minusDays(15).toString()
        );

        final CaseData caseData1 = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .conditionalOrderApplicant1Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(YES)
                            .submittedDate(LocalDateTime.now().minusDays(15))
                            .build()
                    )
                    .conditionalOrderApplicant2Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(NO)
                            .build()
                    )
                    .build()
            )
            .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(caseDetails1.getData()).thenReturn(dataMap1);
        when(mapper.convertValue(dataMap1, CaseData.class)).thenReturn(caseData1);
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPending))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_ENABLE_SWITCH_TO_SOLE_CO, user, SERVICE_AUTHORIZATION);

        task.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_ENABLE_SWITCH_TO_SOLE_CO, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never()).submitEvent(caseDetails2, SYSTEM_ENABLE_SWITCH_TO_SOLE_CO, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        Map<String, Object> dataMap1 = Map.of(
            "coApplicant1IsSubmitted", YES,
            "coApplicant2IsSubmitted", NO,
            "coApplicant1SubmittedDate", LocalDate.now().minusDays(15).toString()
        );

        Map<String, Object> dataMap2 = Map.of(
            "coApplicant2IsSubmitted", YES,
            "coApplicant1IsSubmitted", NO,
            "coApplicant2SubmittedDate", LocalDate.now().minusDays(15).toString()
        );

        final CaseData caseData1 = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .conditionalOrderApplicant1Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(YES)
                            .submittedDate(LocalDateTime.now().minusDays(15))
                            .build()
                    )
                    .conditionalOrderApplicant2Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(NO)
                            .build()
                    )
                    .build()
            )
            .build();

        final CaseData caseData2 = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .conditionalOrderApplicant1Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(NO)
                            .build()
                    )
                    .conditionalOrderApplicant2Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(YES)
                            .submittedDate(LocalDateTime.now().minusDays(15))
                            .build()
                    )
                    .build()
            )
            .build();

        when(caseDetails1.getData()).thenReturn(dataMap1);
        when(caseDetails2.getData()).thenReturn(dataMap2);

        when(mapper.convertValue(dataMap1, CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(dataMap2, CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPending))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_ENABLE_SWITCH_TO_SOLE_CO, user, SERVICE_AUTHORIZATION);

        task.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_ENABLE_SWITCH_TO_SOLE_CO, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_ENABLE_SWITCH_TO_SOLE_CO, user, SERVICE_AUTHORIZATION);
    }
}
