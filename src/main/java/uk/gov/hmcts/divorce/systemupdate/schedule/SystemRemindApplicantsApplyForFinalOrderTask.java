package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicantsApplyForFinalOrder.SYSTEM_REMIND_APPLICANTS_APPLY_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.FINAL_ORDER_ELIGIBLE_FROM_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.FINAL_ORDER_ELIGIBLE_TO_RESPONDENT_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemRemindApplicantsApplyForFinalOrderTask implements Runnable {

    public static final String NOTIFICATION_SENT_FLAG = "finalOrderReminderSentApplicant1";

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Value("${apply_for_final_order.reminder_offset_days}")
    private int applyForFinalOrderReminderOffsetDays;

    @Override
    public void run() {
        log.info("Remind applicant(s) apply for final order task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query = boolQuery()
                .must(matchQuery(STATE, AwaitingFinalOrder))
                .must(existsQuery(FINAL_ORDER_ELIGIBLE_FROM_DATE))
                .must(existsQuery(FINAL_ORDER_ELIGIBLE_TO_RESPONDENT_DATE))
                .filter(rangeQuery(FINAL_ORDER_ELIGIBLE_FROM_DATE)
                    .lte(LocalDate.now().minusDays(applyForFinalOrderReminderOffsetDays)))
                .mustNot(matchQuery(String.format(DATA, NOTIFICATION_SENT_FLAG), YES));

            final List<CaseDetails> casesInAwaitingFinalOrderNeedingReminder =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuthorization, AwaitingFinalOrder);

            for (final CaseDetails caseDetails : casesInAwaitingFinalOrderNeedingReminder) {
                sendReminderToApplicantsIfEligible(caseDetails.getId(), user, serviceAuthorization);
            }
        } catch (final CcdSearchCaseException e) {
            log.error("SystemRemindApplicantToApplyForFinalOrder schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemRemindApplicantToApplyForFinalOrder schedule task stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private void sendReminderToApplicantsIfEligible(Long caseId, User user, String serviceAuthorization) {
        try {
            log.info("Submitting system-remind-applicants-final-order event...");
            ccdUpdateService.submitEvent(caseId, SYSTEM_REMIND_APPLICANTS_APPLY_FOR_FINAL_ORDER, user, serviceAuthorization);
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseId);
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseId);
        }
    }
}
