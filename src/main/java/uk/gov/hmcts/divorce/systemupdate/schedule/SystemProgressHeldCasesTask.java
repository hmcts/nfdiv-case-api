package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.solicitor.notification.AwaitingConditionalOrderNotification;
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

import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressHeldCase.SYSTEM_PROGRESS_HELD_CASE;

@Component
@Slf4j
/**
 * Any cases that were issued >= 20 weeks ago AND are in the Holding state will be moved to AwaitingConditionalOrder by this task.
 */
public class SystemProgressHeldCasesTask implements Runnable {

    @Autowired
    private HoldingPeriodService holdingPeriodService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private AwaitingConditionalOrderNotification conditionalOrderNotification;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    private static final String DUE_DATE = "dueDate";

    @Override
    public void run() {
        log.info("Awaiting conditional order scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final List<CaseDetails> casesInHoldingState = ccdSearchService.searchForAllCasesWithStateOf(Holding, user, serviceAuth);

            for (final CaseDetails caseDetails : casesInHoldingState) {
                try {
                    final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

                    LocalDate dateOfIssue = caseData.getApplication().getIssueDate();
                    log.info("issueDate from caseDataMap {}", dateOfIssue);

                    if (dateOfIssue == null) {
                        log.error("Ignoring case id {} with created on {} and modified on {}, as issue date is null",
                            caseDetails.getId(),
                            caseDetails.getCreatedDate(),
                            caseDetails.getLastModified()
                        );
                    } else {
                        if (holdingPeriodService.isHoldingPeriodFinished(dateOfIssue)) {
                            log.info("Case id {} has been in holding state for > {} weeks hence moving state to AwaitingConditionalOrder",
                                caseDetails.getId(),
                                holdingPeriodService.getHoldingPeriodInWeeks()
                            );

                            //Set due date as null
                            caseDetails.getData().put(DUE_DATE, null);

                            ccdUpdateService.submitEvent(caseDetails, SYSTEM_PROGRESS_HELD_CASE, user, serviceAuth);

                            // trigger notification to applicant's solicitor
                            triggerEmailNotification(caseData, caseDetails.getId());
                        }
                    }
                } catch (final CcdManagementException e) {
                    log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                } catch (final IllegalArgumentException e) {
                    log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
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

    private void triggerEmailNotification(CaseData caseData, Long caseId) {
        boolean applicant1SolicitorRepresented = caseData.getApplicant1().isRepresented();

        if (applicant1SolicitorRepresented) {
            log.info("For case id {} applicant is represented by solicitor hence sending conditional order notification email", caseId);
            conditionalOrderNotification.send(caseData, caseId);
        } else {
            log.info(
                "For case id {} applicant is not represented by solicitor hence not sending conditional order notification email",
                caseId
            );
        }

    }
}
