package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

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
public class SystemNotifyApplicantDisputeFormOverdueTask implements Runnable {

    static final String NOTIFICATION_SENT_FLAG = "disputeNotSubmittedNotificationSent";
    private static final String SUBMIT_EVENT_ERROR = "Submit event failed for case id: {}, continuing to next case";
    private static final String DESERIALIZATION_ERROR = "Deserialization failed for case id: {}, continuing to next case";
    private static final String CCD_SEARCH_ERROR = "NotifyApplicantDisputeFormOverdue schedule task stopped after search error";
    private static final String TASK_CONFLICT_ERROR =
        "NotifyApplicantDisputeFormOverdue scheduled task stopping due to conflict with another running task";

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Override
    public void run() {
        log.info("NotifyApplicantDisputeFormOverdue scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, Holding))
                    .must(matchQuery(AOS_RESPONSE, DISPUTE_DIVORCE))
                    .filter(rangeQuery(ISSUE_DATE).lte(LocalDate.now().minusDays(37)))
                    .mustNot(matchQuery(String.format(DATA, NOTIFICATION_SENT_FLAG), YesOrNo.YES));

            ccdSearchService.searchForAllCasesWithQuery(Holding, query, user, serviceAuth)
                .forEach(caseDetails -> notifyApplicant(caseDetails, user, serviceAuth));

            log.info("NotifyApplicantDisputeFormOverdue scheduled task complete.");
        } catch (final CcdSearchCaseException | CcdConflictException e) {
            log.error(e instanceof CcdConflictException ? TASK_CONFLICT_ERROR : CCD_SEARCH_ERROR, e);
        }
    }

    private void notifyApplicant(CaseDetails caseDetails, User user, String serviceAuth) {
        try {
            final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
            if (!caseData.getApplication().hasApplicantBeenNotifiedDisputeFormOverdue()
                && !caseData.getApplication().getIssueDate().plusDays(37).isAfter(LocalDate.now())) {
                log.info("Dispute form for Case id {} is due on/before current date - raising notification event", caseDetails.getId());
                ccdUpdateService.submitEvent(caseDetails, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, serviceAuth);
            }
        } catch (final CcdManagementException | IllegalArgumentException e) {
            log.error(e instanceof CcdManagementException ? SUBMIT_EVENT_ERROR : DESERIALIZATION_ERROR, caseDetails.getId());
        }
    }
}
