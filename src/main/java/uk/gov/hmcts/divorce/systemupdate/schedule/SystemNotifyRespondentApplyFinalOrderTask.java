package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
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
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyRespondentFinalOrderApply.SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER;
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
    public static final String RESP_ELIGIBLE_DATE = "dateFinalOrderEligibleToRespondent";
    public static final String NOT_NOTIFIED_FLAG = "applicant2FinalOrderReminderSent";
    public static final String APP_ELIGIBLE_DATE = "dateFinalOrderEligibleFrom";

    @Override
    public void run() {
        log.info("Final Order overdue scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, AwaitingFinalOrder))
                    .must(matchQuery(String.format(DATA, APPLICATION_TYPE), "soleApplication"));

            final List<CaseDetails> validCasesInAwaitingFinalOrderState =
                ccdSearchService.searchForAllCasesWithQuery(AwaitingFinalOrder, query, user, serviceAuth);

            for (final CaseDetails caseDetails : validCasesInAwaitingFinalOrderState) {
                try {

                    Map<String, Object> caseDataMap = caseDetails.getData();
                    YesOrNo finalOrderReminderSent = (YesOrNo) caseDataMap.getOrDefault(NOT_NOTIFIED_FLAG, YesOrNo.NO);
                    String respondentCanApplyFromDate = (String) caseDataMap.getOrDefault(RESP_ELIGIBLE_DATE, null);
                    String applicantCanApplyFromDate = (String) caseDataMap.getOrDefault(APP_ELIGIBLE_DATE, null);
                    String applicationType = (String) caseDataMap.get(APPLICATION_TYPE);

                    log.info("Found Case Id {} Application Type {} Eligible From {} Reminder Sent? {}",
                        caseDetails.getId(),
                        applicationType,
                        applicantCanApplyFromDate,
                        finalOrderReminderSent);

                    if (respondentCanApplyFromDate == null) {
                        log.error("Ignoring case id {} with applicant FO Eligible on {}. Respondent Eligible date is null",
                            caseDetails.getId(),
                            applicantCanApplyFromDate
                        );
                    } else {

                        LocalDate parsedRespondentEligibleDate = LocalDate.parse(respondentCanApplyFromDate);

                        log.info("Will check Reminder Sent {} and respondent Eligible Date {}",
                            finalOrderReminderSent, parsedRespondentEligibleDate);

                        if (finalOrderReminderSent == YesOrNo.YES) {
                            log.info("finalOrderReminderSent is Yes - Ignore");
                        } else {
                            log.info("finalOrderReminderSent is No - Has date passed?");
                            log.info("Will check respondent reminder Eligible date......");

                            if (LocalDate.now().isAfter(parsedRespondentEligibleDate)) {
                                log.info("Need to send reminder to respondent for Case {}", caseDetails.getId());
                                ccdUpdateService.submitEvent(caseDetails, SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER, user, serviceAuth);
                            } else {
                                log.info("Case not yet eligible to apply for Final Order {}", respondentCanApplyFromDate);
                            }
                        }
                    }
                } catch (final CcdManagementException e) {
                    log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                } catch (final IllegalArgumentException e) {
                    log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

            log.info("SystemNotifyRespondentFinalOrderApply scheduled task complete.");

        } catch (final CcdSearchCaseException e) {
            log.error("SystemNotifyRespondentFinalOrderApply schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("SystemNotifyRespondentFinalOrderApply schedule task stopping "
                + "due to conflict with another running task"
            );
        }
    }
}

