package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.caseworker.service.CcdConflictException;
import uk.gov.hmcts.divorce.caseworker.service.CcdManagementException;
import uk.gov.hmcts.divorce.caseworker.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.caseworker.service.CcdSearchService;
import uk.gov.hmcts.divorce.caseworker.service.CcdUpdateService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.WEEKS;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressHeldCase.SYSTEM_PROGRESS_HELD_CASE;

@Component
@Slf4j
/**
 * Any cases that were issued > 20 weeks ago AND are in the Holding state will be moved to AwaitingConditionalOrder by this task.
 */
public class SystemProgressHeldCasesTask {

    @Value("${case_progression.holding_period_in_weeks}")
    private int holdingPeriodInWeeks;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(cron = "${schedule.awaiting_conditional_order}")
    public void execute() {

        log.info("Awaiting conditional order scheduled task started");

        try {
            final List<CaseDetails> casesInHoldingState = ccdSearchService.searchForAllCasesWithStateOf(Holding);

            for (final CaseDetails caseDetails : casesInHoldingState) {
                try {
                    Map<String, Object> caseDataMap = caseDetails.getData();
                    LocalDate issueDate = (LocalDate) caseDataMap.getOrDefault("issueDate",null);
                    log.info("issueDate from caseDataMap {} : ", issueDate);
                    CaseData caseData = objectMapper.convertValue(caseDataMap, CaseData.class);
                    try {
                        log.info(objectMapper.writeValueAsString(caseData));
                    } catch (JsonProcessingException e) {
                        log.error(e.getMessage(), e);
                    }
                    log.info("Case data issue date {} and marriage date {}",
                        caseData.getIssueDate(),
                        caseData.getApplication().getMarriageDetails().getDate()
                    );
                    if (caseData.getIssueDate() == null) {
                        log.error("Ignoring case id {} with created on {} and modified on {}, as issue date is null",
                            caseDetails.getId(),
                            caseDetails.getCreatedDate(),
                            caseDetails.getLastModified()
                        );
                    } else {
                        long weeksBetweenIssueDateAndNow = WEEKS.between(caseData.getIssueDate(), LocalDate.now());

                        if (weeksBetweenIssueDateAndNow >= holdingPeriodInWeeks) {
                            log.info("Case id {} has been in holding state for > {} weeks hence moving state to AwaitingConditionalOrder",
                                caseDetails.getId(),
                                holdingPeriodInWeeks
                            );
                            ccdUpdateService.submitEvent(caseDetails, SYSTEM_PROGRESS_HELD_CASE);
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
}
