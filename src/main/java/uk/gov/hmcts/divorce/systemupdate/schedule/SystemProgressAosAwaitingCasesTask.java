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
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressAosAwaitingCase.SYSTEM_PROGRESS_AWAITING_AOS;

@Component
@Slf4j
/**
 * Any cases whose due date >= current date will be moved to AosOverdue by this task.
 */
public class SystemProgressAosAwaitingCasesTask {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    private static final String DUE_DATE = "dueDate";

    @Scheduled(cron = "${schedule.aos_overdue}")
    public void execute() {

        log.info("Awaiting Aos scheduled task started");

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
                            log.info("Case id {} has been in awaiting aos state with due date {} moving state to AosOverdue as due date is >= current date",
                                caseDetails.getId(),
                                aosDueDate
                            );
                            ccdUpdateService.submitEvent(caseDetails, SYSTEM_PROGRESS_AWAITING_AOS);
                        }
                    }
                } catch (final CcdManagementException e) {
                    log.info("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

            log.info("Awaiting Aos scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Awaiting Aos schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Awaiting Aos schedule task stopping "
                + "due to conflict with another running awaiting aos task"
            );
        }
    }
}
