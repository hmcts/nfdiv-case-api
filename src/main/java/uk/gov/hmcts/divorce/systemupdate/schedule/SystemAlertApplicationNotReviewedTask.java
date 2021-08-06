package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.JointApplicationOverdueNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemAlertApplicationNotReviewed.SYSTEM_APPLICATION_NOT_REVIEWED;

@Component
@Slf4j
public class SystemAlertApplicationNotReviewedTask {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private JointApplicationOverdueNotification jointApplicationOverdueNotification;

    @Autowired
    private ObjectMapper objectMapper;

    @Scheduled(cron = "${schedule.joint_application_overdue}")
    public void execute() {

        log.info("Joint application overdue scheduled task started");

        try {
            final List<CaseDetails> casesInAwaitingApplicant2Response =
                ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response);

            for (final CaseDetails caseDetails : casesInAwaitingApplicant2Response) {
                try {
                    final CaseData caseData = extractCaseData(caseDetails.getData());
                    LocalDate dueDate = caseData.getDueDate();

                    if (dueDate == null) {
                        log.error("Ignoring case id {} with created on {} and modified on {}, as due date is null",
                            caseDetails.getId(),
                            caseDetails.getCreatedDate(),
                            caseDetails.getLastModified()
                        );
                    } else {
                        if (!dueDate.isAfter(LocalDate.now()) && !caseData.getApplication().hasOverdueNotificationBeenSent()) {
                            notifyApplicant1(caseDetails, caseData, dueDate);
                        }
                    }
                } catch (final CcdManagementException e) {
                    log.info("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

            log.info("Joint application overdue scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Joint application overdue schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Joint application overdue scheduled task stopping"
                + " due to conflict with another running Joint application overdue task"
            );
        }
    }

    private void notifyApplicant1(CaseDetails caseDetails, CaseData caseData, LocalDate dueDate) {
        log.info("Due date {} for Case id {} is on/before current date - sending notification to Applicant 1",
            dueDate,
            caseDetails.getId()
        );

        jointApplicationOverdueNotification.send(caseData, caseDetails.getId());
        caseData.getApplication().setOverdueNotificationSent(YesOrNo.YES);
        ccdUpdateService.submitEvent(caseDetails, SYSTEM_APPLICATION_NOT_REVIEWED);
    }

    private CaseData extractCaseData(Map<String, Object> caseDataMap) {
        return objectMapper.convertValue(caseDataMap, CaseData.class);
    }
}
