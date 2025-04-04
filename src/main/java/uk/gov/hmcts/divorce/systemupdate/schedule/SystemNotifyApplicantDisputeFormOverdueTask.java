package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
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
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyApplicantDisputeFormOverdue.SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.AOS_RESPONSE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.ISSUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemNotifyApplicantDisputeFormOverdueTask extends AbstractTaskEventSubmit {

    public static final String NOTIFICATION_SENT_FLAG = "applicantNotifiedDisputeFormOverdue";
    private static final String CCD_SEARCH_ERROR = "NotifyApplicantDisputeFormOverdue schedule task stopped after search error";
    private static final String TASK_CONFLICT_ERROR =
        "NotifyApplicantDisputeFormOverdue scheduled task stopping due to conflict with another running task";

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Value("${submit_aos.dispute_offset_days}")
    private int disputeDueDateOffsetDays;

    @Override
    public void run() {
        log.info("NotifyApplicantDisputeFormOverdue scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, Holding))
                    .must(matchQuery(String.format(DATA, AOS_RESPONSE), DISPUTE_DIVORCE.getType()))
                    .filter(rangeQuery(ISSUE_DATE).lte(LocalDate.now().minusDays(disputeDueDateOffsetDays)))
                    .mustNot(matchQuery(String.format(DATA, NOTIFICATION_SENT_FLAG), YesOrNo.YES));

            ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth, Holding)
                .forEach(caseDetails -> notifyApplicant(caseDetails.getId(), user, serviceAuth));

            log.info("NotifyApplicantDisputeFormOverdue scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error(CCD_SEARCH_ERROR, e);
        } catch (CcdConflictException e) {
            log.error(TASK_CONFLICT_ERROR, e);
        }
    }

    private void notifyApplicant(Long caseId, User user, String serviceAuth) {
        log.info("Dispute form for Case id {} is due on/before current date - raising notification event", caseId);
        submitEvent(caseId, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, serviceAuth);
    }
}
