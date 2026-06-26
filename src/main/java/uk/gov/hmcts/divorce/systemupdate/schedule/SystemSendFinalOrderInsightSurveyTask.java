package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrderInsightSurveyInvite;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderComplete;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemSendFinalOrderInsightSurvey.SYSTEM_SEND_FINAL_ORDER_INSIGHT_SURVEY;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemSendFinalOrderInsightSurveyTask implements Runnable {

    public static final int SCHEDULE_WINDOW_DAYS = 1;

    public static final String CASE_FINAL_ORDER_GRANTED_DATE = "data.grantedDate";
    public static final String CASE_SURVEY_INVITE_STAGE = "data.finalOrderInsightSurveyStage";

    private final CcdSearchService ccdSearchService;

    private final CcdUpdateService ccdUpdateService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    @Override
    public void run() {
        log.info("SystemSendFinalOrderInsightSurveyTask scheduled task started");

        try {
            final List<FinalOrderInsightSurveyInvite> surveyInvitesByStage = FinalOrderInsightSurveyInvite.BY_STAGE;

            final BoolQueryBuilder surveyNotificationScheduleQuery = boolQuery();
            for (FinalOrderInsightSurveyInvite surveyInvite : surveyInvitesByStage) {
                surveyNotificationScheduleQuery
                    .should(getNotificationWindowQuery(
                        surveyInvite.getStage(), surveyInvite.getDaysAfterGrantedDate()
                    ));
            }
            surveyNotificationScheduleQuery.minimumShouldMatch(1);

            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, FinalOrderComplete))
                    .must(surveyNotificationScheduleQuery);

            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuth = authTokenGenerator.generate();

            ccdSearchService
                .searchForAllCasesWithQuery(query, user, serviceAuth, FinalOrderComplete)
                .forEach(caseDetails -> submitEvent(caseDetails.getId(), user, serviceAuth));

            log.info("SystemSendFinalOrderInsightSurveyTask scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("SystemSendFinalOrderInsightSurveyTask schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemSendFinalOrderInsightSurveyTask schedule task stopping due to conflict with another running task"
            );
        }
    }

    private BoolQueryBuilder getNotificationWindowQuery(int inviteStage, int inviteDaysAfterGrantedDate) {
        return boolQuery()
            .must(rangeQuery(CASE_SURVEY_INVITE_STAGE).gte(inviteStage).lt(inviteStage + 1))
            .filter(rangeQuery(CASE_FINAL_ORDER_GRANTED_DATE)
                .lte(LocalDate.now().minusDays(inviteDaysAfterGrantedDate))
                .gt(LocalDate.now().minusDays(inviteDaysAfterGrantedDate + SCHEDULE_WINDOW_DAYS))
            );
    }

    private void submitEvent(Long caseId, User user, String serviceAuth) {
        try {
            ccdUpdateService.submitEvent(caseId, SYSTEM_SEND_FINAL_ORDER_INSIGHT_SURVEY, user, serviceAuth);
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseId);
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseId);
        }
    }
}
