package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
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
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyRespondentApplyFinalOrder.SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
/*
  As a system, I want to notify the respondent that they can apply for the Final Order IF the applicant hasn't
  applied after 3 months so that the case can be progressed
*/
public class SystemNotifyRespondentApplyFinalOrderTask implements Runnable {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public static final String APPLICATION_TYPE = "applicationType";
    public static final String SOLE_APPLICATION = "soleApplication";
    public static final String NOTIFICATION_FLAG = "finalOrderReminderSentApplicant2";
    public static final String APP_ELIGIBLE_DATE = "dateFinalOrderEligibleFrom";
    public static final String RESP_ELIGIBLE_DATE = "dateFinalOrderEligibleToRespondent";

    @Override
    public void run() {
        log.info("SystemNotifyRespondentApplyFinalOrder scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, AwaitingFinalOrder))
                    .filter(rangeQuery(String.format(DATA, RESP_ELIGIBLE_DATE)).lte(LocalDate.now()))
                    .must(matchQuery(String.format(DATA, APPLICATION_TYPE), SOLE_APPLICATION))
                    .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YesOrNo.YES));

            final List<CaseDetails> validCasesInAwaitingFinalOrderState =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuth, AwaitingFinalOrder);

            for (final CaseDetails caseDetails : validCasesInAwaitingFinalOrderState) {
                sendNotificationToRespondentIfEligible(user, serviceAuth, caseDetails);
            }

            log.info("SystemNotifyRespondentApplyFinalOrder scheduled task complete.");

        } catch (final CcdSearchCaseException e) {
            log.error("SystemNotifyRespondentApplyFinalOrder schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemNotifyRespondentApplyFinalOrder schedule task stopping "
                + "due to conflict with another running task"
            );
        }
    }

    private void sendNotificationToRespondentIfEligible(User user, String serviceAuth, CaseDetails caseDetails) {
        try {

            Map<String, Object> caseDataMap = caseDetails.getData();
            String applicantCanApplyFromDate = (String) caseDataMap.getOrDefault(APP_ELIGIBLE_DATE, null);
            String applicationType = (String) caseDataMap.get(APPLICATION_TYPE);

            log.info("Found Case Id: {}, application type: {}, applicant eligible from: {}",
                caseDetails.getId(),
                applicationType,
                applicantCanApplyFromDate);

            log.info("Sending notification to respondent to let them know to apply to final order. Case ID: {}", caseDetails.getId());
            ccdUpdateService.submitEvent(caseDetails.getId(), SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER, user, serviceAuth);

        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
        }
    }
}

