package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressCaseToAosOverdue.SYSTEM_PROGRESS_TO_AOS_OVERDUE;

@Component
@Slf4j
/**
 * Any cases which are in AwaitingAos or AosDrafted state and whose due date >= current date will be moved to AosOverdue by this task.
 */
public class SystemProgressCasesToAosOverdueTask {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    private static final String DUE_DATE = "dueDate";

    @Scheduled(cron = "${schedule.aos_overdue}")
    public void execute() {

        log.info("Aos overdue scheduled task started");

        try {
            final List<CaseDetails> casesInAwaitingAosState = ccdSearchService.searchForAllCasesWithStateOf(AwaitingAos);

            for (final CaseDetails caseDetails : casesInAwaitingAosState) {
                try {
                    Map<String, Object> caseDataMap = caseDetails.getData();
                    String dueDate = (String) caseDataMap.getOrDefault(DUE_DATE, null);
                    log.info("dueDate is {} from caseDataMap for case id {}", dueDate, caseDetails.getId());

                    if (dueDate == null) {
                        log.error("Ignoring case id {} with created on {} and modified on {}, as due date is null",
                            caseDetails.getId(),
                            caseDetails.getCreatedDate(),
                            caseDetails.getLastModified()
                        );
                    } else {
                        LocalDate aosDueDate = LocalDate.parse(dueDate);

                        if (aosDueDate.isEqual(LocalDate.now()) || aosDueDate.isAfter(LocalDate.now())) {
                            log.info("Due date {} for Case id {} is on/before current date hence moving state to AosOverdue",
                                aosDueDate,
                                caseDetails.getId()
                            );
                            ccdUpdateService.submitEvent(caseDetails, SYSTEM_PROGRESS_TO_AOS_OVERDUE);
                        }
                    }
                } catch (final CcdManagementException e) {
                    log.info("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

            log.info("Aos overdue scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Aos overdue schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Aos overdue schedule task stopping "
                + "due to conflict with another running awaiting aos task"
            );
        }
    }
}
