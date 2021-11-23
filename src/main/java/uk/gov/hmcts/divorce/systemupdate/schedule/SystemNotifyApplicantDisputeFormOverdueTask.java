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
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.time.temporal.ChronoUnit.*;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PendingDispute;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyApplicantDisputeFormOverdue.SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.AOS_RESPONSE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.ISSUE_DATE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemNotifyApplicantDisputeFormOverdueTask implements Runnable {

    private static final String NOTIFICATION_SENT_FLAG = "disputeNotSubmittedNotificationSent";

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
        final String serviceAuthorization = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, Holding))
                    .must(matchQuery(AOS_RESPONSE, DISPUTE_DIVORCE))
                    .filter(rangeQuery(ISSUE_DATE).lte(LocalDate.now().minus(37, DAYS)))
                    .mustNot(matchQuery(String.format(DATA, NOTIFICATION_SENT_FLAG), YesOrNo.YES));

            final List<CaseDetails> overdueDisputedCases =
                ccdSearchService.searchForAllCasesWithQuery(Holding, query, user, serviceAuthorization);

            for (final CaseDetails caseDetails : overdueDisputedCases) {
                try {
                    final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                    if (caseData.getApplication().hasApplicantBeenNotifiedDisputeFormOverdue()) {
                        log.info("SystemNotifyApplicantDisputeFormOverdueTask already triggered  for Case ({})", caseDetails.getId());
                    } else {
                        notifyApplicant(caseDetails, caseData.getDueDate(), user, serviceAuthorization);
                    }
                } catch (final CcdManagementException e) {
                    log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                } catch (final IllegalArgumentException e) {
                    log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }
            log.info("NotifyApplicantDisputeFormOverdue scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("NotifyApplicantDisputeFormOverdue schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("NotifyApplicantDisputeFormOverdue scheduled task stopping due to conflict with another running task");
        }
    }

    private void notifyApplicant(CaseDetails caseDetails, LocalDate dueDate, User user, String serviceAuth) {
        log.info("Dispute form due date {} for Case id {} is on/before current date - notifying Applicant", dueDate, caseDetails.getId());
        ccdUpdateService.submitEvent(caseDetails, SYSTEM_NOTIFY_APPLICANT_DISPUTE_FORM_OVERDUE, user, serviceAuth);
    }
}
