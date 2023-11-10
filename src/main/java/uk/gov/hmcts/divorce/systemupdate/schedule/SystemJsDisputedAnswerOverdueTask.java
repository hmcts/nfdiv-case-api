package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAnswer;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.SEPARATION;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemJsDisputedAnswerOverdue.SYSTEM_JS_DISPUTED_ANSWER_OVERDUE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.AOS_RESPONSE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.AWAITING_JS_ANSWER_START_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.SUPPLEMENTARY_CASE_TYPE;

@Component
@Slf4j
public class SystemJsDisputedAnswerOverdueTask extends AbstractTaskEventSubmit {

    private static final String CCD_SEARCH_ERROR = "JsDisputedAnswerOverdue schedule task stopped after search error";
    private static final String TASK_CONFLICT_ERROR =
        "JsDisputedAnswerOverdue scheduled task stopping due to conflict with another running task";

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Value("${judicial_separation_answer_overdue.offset_days}")
    private int answerOverdueOffsetDays;

    @Override
    public void run() {
        log.info("JsDisputedAnswerOverdue scheduled task started");

        final User systemUser = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, AwaitingAnswer))
                    .must(
                        boolQuery()
                            .should(matchQuery(String.format(DATA, SUPPLEMENTARY_CASE_TYPE), JUDICIAL_SEPARATION))
                            .should(matchQuery(String.format(DATA, SUPPLEMENTARY_CASE_TYPE), SEPARATION))
                            .minimumShouldMatch(1)
                    )
                    .must(matchQuery(String.format(DATA, AOS_RESPONSE), DISPUTE_DIVORCE.getType()))
                    .filter(rangeQuery(String.format(DATA, AWAITING_JS_ANSWER_START_DATE))
                        .lte(LocalDate.now().minusDays(answerOverdueOffsetDays)));

            ccdSearchService.searchForAllCasesWithQuery(query, systemUser, serviceAuth, AwaitingAnswer)
                .forEach(caseDetails -> updateState(caseDetails.getId(), systemUser, serviceAuth));

            log.info("JsDisputedAnswerOverdue scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error(CCD_SEARCH_ERROR, e);
        } catch (CcdConflictException e) {
            log.error(TASK_CONFLICT_ERROR, e);
        }
    }

    private void updateState(Long caseId, User user, String serviceAuth) {
        log.info("Answer Overdue for Disputed JS Case (id={}), setting state to AwaitingJS/Nullity", caseId);
        submitEvent(caseId, SYSTEM_JS_DISPUTED_ANSWER_OVERDUE, user, serviceAuth);
    }
}
