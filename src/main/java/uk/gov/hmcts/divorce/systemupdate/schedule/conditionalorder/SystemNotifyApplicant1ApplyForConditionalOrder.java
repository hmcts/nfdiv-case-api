package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant1ApplyForConditionalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemApplicant1ApplyForConditionalOrder.SYSTEM_NOTIFY_APPLICANT1_CONDITIONAL_ORDER;

@Component
@Slf4j
public class SystemNotifyApplicant1ApplyForConditionalOrder implements Runnable {

    private static final int TWENTY_WEEKS = 20;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private Applicant1ApplyForConditionalOrderNotification applicant1ApplyForConditionalOrderNotification;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run() {
        log.info("Notify Applicant 1 that they can apply for a Conditional Order");

        try {
            final List<CaseDetails> casesInAwaitingApplicant2Response =
                ccdSearchService.searchForAllCasesWithStateOf(AwaitingConditionalOrder);

            for (final CaseDetails caseDetails : casesInAwaitingApplicant2Response) {
                try {
                    final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
                    final LocalDate canApplyForConditionalOrderFrom = caseData.getApplication().getIssueDate().plusWeeks(TWENTY_WEEKS);

                    if (!canApplyForConditionalOrderFrom.isAfter(LocalDate.now())
                        && !caseData.getApplication().hasApplicant1BeenNotifiedCanApplyForConditionalOrder()
                    ) {
                        notifyApplicant1(caseDetails, caseData);
                    }
                } catch (final CcdManagementException e) {
                    log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                } catch (final IllegalArgumentException e) {
                    log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

            log.info("Applicant 1 Apply For Conditional Order scheduled task complete.");
        } catch (final CcdSearchCaseException e) {
            log.error("Applicant 1 Apply For Conditional Order scheduled task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Remind applicant 2 scheduled task stopping"
                + " due to conflict with another running Remind applicant 2 task"
            );
        }
    }

    private void notifyApplicant1(CaseDetails caseDetails, CaseData caseData) {
        log.info(
            "20 weeks has passed since issue date for Case id {} - notifying Applicant 1 that they can apply for a Conditional Order",
            caseDetails.getId());

        applicant1ApplyForConditionalOrderNotification.sendToApplicant1(caseData, caseDetails.getId());
        ccdUpdateService.submitEvent(caseDetails, SYSTEM_NOTIFY_APPLICANT1_CONDITIONAL_ORDER);
    }
}
