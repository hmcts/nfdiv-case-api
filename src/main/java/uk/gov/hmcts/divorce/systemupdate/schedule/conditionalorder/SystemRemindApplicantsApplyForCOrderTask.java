package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.schedule.AbstractTaskEventSubmit;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicantsApplyForCOrder.SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemRemindApplicantsApplyForCOrderTask extends AbstractTaskEventSubmit {

    public static final String NOTIFICATION_FLAG = "applicantsRemindedCanApplyForConditionalOrder";
    public static final String CCD_SEARCH_ERROR =
        "SystemRemindApplicantsApplyForCOrderTask scheduled task stopped after search error";
    public static final String CCD_CONFLICT_ERROR =
        "SystemRemindApplicantsApplyForCOrderTask scheduled task stopping due to conflict with another running task";
    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Value("${submit_co.reminder_offset_days}")
    private int submitCOrderReminderOffsetDays;

    @Override
    public void run() {
        log.info("Remind Joint Applicants that they can apply for a Conditional Order");
        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();
        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(
                        boolQuery()
                            .should(matchQuery(STATE, AwaitingConditionalOrder))
                            .should(matchQuery(STATE, ConditionalOrderPending))
                            .should(matchQuery(STATE, ConditionalOrderDrafted))
                            .minimumShouldMatch(1)
                    )
                    .filter(rangeQuery(DUE_DATE).lte(LocalDate.now().minusDays(submitCOrderReminderOffsetDays)))
                    .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YesOrNo.YES));
            ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuthorization,
                    AwaitingConditionalOrder, ConditionalOrderPending, ConditionalOrderDrafted)
                .forEach(caseDetails -> submitEvent(
                    caseDetails.getId(), SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER, user, serviceAuthorization));

            log.info("SystemRemindApplicantsApplyForCOrderTask scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error(CCD_SEARCH_ERROR, e);
        } catch (final CcdConflictException e) {
            log.info(CCD_CONFLICT_ERROR);
        }
    }
}
