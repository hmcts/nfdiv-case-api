package uk.gov.hmcts.divorce.systemupdate.schedule;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrderInsightSurveyInvite;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderComplete;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemSendFinalOrderInsightSurvey.SYSTEM_SEND_FINAL_ORDER_INSIGHT_SURVEY;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemSendFinalOrderInsightSurveyTask.CASE_FINAL_ORDER_GRANTED_DATE;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemSendFinalOrderInsightSurveyTask.CASE_SURVEY_INVITE_STAGE;
import static uk.gov.hmcts.divorce.systemupdate.schedule.SystemSendFinalOrderInsightSurveyTask.SCHEDULE_WINDOW_DAYS;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SystemSendFinalOrderInsightSurveyTaskTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SystemSendFinalOrderInsightSurveyTask systemSendFinalOrderInsightSurveyTask;

    private User user;

    private static final BoolQueryBuilder query =
        boolQuery()
            .must(matchQuery(STATE, FinalOrderComplete))
            .must(getSurveyNotificationScheduleQuery());

    private static BoolQueryBuilder getSurveyNotificationScheduleQuery() {
        final BoolQueryBuilder surveyNotificationScheduleQuery = boolQuery();
        for (FinalOrderInsightSurveyInvite surveyInvite : FinalOrderInsightSurveyInvite.BY_STAGE) {
            surveyNotificationScheduleQuery.should(getNotificationWindowQuery(
                surveyInvite.getStage(),
                surveyInvite.getDaysAfterGrantedDate()
            ));
        }
        surveyNotificationScheduleQuery.minimumShouldMatch(1);
        return surveyNotificationScheduleQuery;
    }

    private static BoolQueryBuilder getNotificationWindowQuery(int notificationsSent, int grantedDateOffsetDays) {
        return boolQuery()
            .must(rangeQuery(CASE_SURVEY_INVITE_STAGE).gte(notificationsSent).lt(notificationsSent + 1))
            .filter(rangeQuery(CASE_FINAL_ORDER_GRANTED_DATE)
                .lte(LocalDate.now().minusDays(grantedDateOffsetDays))
                .gt(LocalDate.now().minusDays(grantedDateOffsetDays + SCHEDULE_WINDOW_DAYS))
            );
    }

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldTriggerFinalOrderCompleteSurveyForCasesReturnedByElasticSearch() {
        final CaseDetails caseDetails = CaseDetails.builder().id(TEST_CASE_ID).build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, FinalOrderComplete))
            .thenReturn(caseDetailsList);

        systemSendFinalOrderInsightSurveyTask.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_SEND_FINAL_ORDER_INSIGHT_SURVEY, user, SERVICE_AUTHORIZATION);
    }
}
