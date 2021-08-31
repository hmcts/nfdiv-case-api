package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSentForReviewApplicant2Notification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicant2.SYSTEM_REMIND_APPLICANT2;

@Component
@Slf4j
public class SystemRemindApplicant2Task implements Runnable {

    private static final int FOUR_DAYS = 4;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private ApplicationSentForReviewApplicant2Notification applicationSentForReviewApplicant2Notification;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run() {
        log.info("Remind applicant 2 scheduled task started");

        try {
            final List<CaseDetails> casesInAwaitingApplicant2Response =
                ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response);

            for (final CaseDetails caseDetails : casesInAwaitingApplicant2Response) {
                try {
                    final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                    final LocalDate reminderDate = caseData.getDueDate().minusDays(FOUR_DAYS);

                    if (!reminderDate.isAfter(LocalDate.now()) && caseData.getCaseInvite().getAccessCode() != null
                        && !caseData.getApplication().isApplicant2ReminderSent()
                    ) {
                        notifyApplicant2(caseDetails, caseData, reminderDate);
                    }
                } catch (final CcdManagementException e) {
                    log.info("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

            log.info("Remind applicant 2 scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Remind applicant 2 schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Remind applicant 2 scheduled task stopping"
                + " due to conflict with another running Remind applicant 2 task"
            );
        }
    }

    private void notifyApplicant2(CaseDetails caseDetails, CaseData caseData, LocalDate reminderDate) {
        log.info("Reminder date {} for Case id {} is on/before current date - sending reminder to Applicant 2",
            reminderDate,
            caseDetails.getId()
        );

        applicationSentForReviewApplicant2Notification.sendReminder(caseData, caseDetails.getId());
        ccdUpdateService.submitEvent(caseDetails, SYSTEM_REMIND_APPLICANT2);
    }
}
