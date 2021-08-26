package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.citizen.notification.AwaitingConditionalOrderNotification;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;

import static java.time.LocalDate.parse;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressHeldCase.SYSTEM_PROGRESS_HELD_CASE;

@Component
@Slf4j
/**
 * Any cases that were issued >= 20 weeks ago AND are in the Holding state will be moved to AwaitingConditionalOrder by this task.
 */
public class SystemProgressHeldCasesTask {

    @Autowired
    private HoldingPeriodService holdingPeriodService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private AwaitingConditionalOrderNotification conditionalOrderNotification;

    private static final String ISSUE_DATE_KEY = "issueDate";
    private static final String APPLICANT1_SOLICITOR_REPRESENTED = "applicant1SolicitorRepresented";

    @Scheduled(cron = "${schedule.awaiting_conditional_order}")
    public void execute() {

        log.info("Awaiting conditional order scheduled task started");

        try {
            final List<CaseDetails> casesInHoldingState = ccdSearchService.searchForAllCasesWithStateOf(Holding);

            for (final CaseDetails caseDetails : casesInHoldingState) {
                try {
                    Map<String, Object> caseDataMap = caseDetails.getData();
                    String dateOfIssue = (String) caseDataMap.getOrDefault(ISSUE_DATE_KEY, null);
                    log.info("issueDate from caseDataMap {}", dateOfIssue);

                    if (dateOfIssue == null) {
                        log.error("Ignoring case id {} with created on {} and modified on {}, as issue date is null",
                            caseDetails.getId(),
                            caseDetails.getCreatedDate(),
                            caseDetails.getLastModified()
                        );
                    } else {
                        if (holdingPeriodService.isHoldingPeriodFinished(parse(dateOfIssue))) {
                            log.info("Case id {} has been in holding state for > {} weeks hence moving state to AwaitingConditionalOrder",
                                caseDetails.getId(),
                                holdingPeriodService.getHoldingPeriodInWeeks()
                            );

                            //Set Issue date as null
                            caseDetails.getData().put(ISSUE_DATE_KEY, null);

                            ccdUpdateService.submitEvent(caseDetails, SYSTEM_PROGRESS_HELD_CASE);

                            // trigger notification to applicant's solicitor
                            triggerEmailNotification(caseDataMap, caseDetails.getId());
                        }
                    }
                } catch (final CcdManagementException e) {
                    log.info("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

            log.info("Awaiting conditional order scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Awaiting conditional order schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Awaiting conditional order schedule task stopping "
                + "due to conflict with another running awaiting conditional order task"
            );
        }
    }

    private void triggerEmailNotification(Map<String, Object> caseDataMap, Long caseId) {
        boolean applicant1SolicitorRepresented = (boolean) caseDataMap.getOrDefault(APPLICANT1_SOLICITOR_REPRESENTED, false);

        if (applicant1SolicitorRepresented) {
            log.info("For case id {} applicant is represented by solicitor hence sending conditional order notification email", caseId);
            conditionalOrderNotification.send(caseDataMap, caseId);
        } else {
            log.info("For case id {} applicant is not represented by solicitor hence not sending conditional order notification email", caseId);
        }

    }
}
