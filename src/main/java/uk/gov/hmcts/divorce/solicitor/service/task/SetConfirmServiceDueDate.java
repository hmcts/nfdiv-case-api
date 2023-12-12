package uk.gov.hmcts.divorce.solicitor.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorService;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static org.springframework.util.CollectionUtils.isEmpty;

@Component
@Slf4j
public class SetConfirmServiceDueDate implements CaseTask {

    @Value("${aos_pack.due_date_offset_days}")
    private long dueDateOffsetDays;

    @Autowired
    private HoldingPeriodService holdingPeriodService;

    /**
     * For solicitor service the dueDate is set to 16 days after the date the solicitor says the served the respondent.
     * If they confirm that this service was processed by a process server then due date is set to 141 days after issue date
     */
    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        CaseData caseData = caseDetails.getData();

        if (caseData.getAcknowledgementOfService().getDateAosSubmitted() != null) {
            log.info("Skip setting dueDate: AoS previously Submitted for CaseId: {}", caseDetails.getId());
            return caseDetails;
        } else {
            SolicitorService solicitorService = caseData.getApplication().getSolicitorService();

            var dueDate = solicitorService.getDateOfService().plusDays(dueDateOffsetDays);

            if (!isEmpty(solicitorService.getServiceProcessedByProcessServer())) {
                dueDate = holdingPeriodService.getDueDateFor(caseData.getApplication().getIssueDate());
            }

            caseData.setDueDate(dueDate);

            log.info("Setting dueDate of {}, for CaseId: {}", caseData.getDueDate(), caseDetails.getId());
        }

        return caseDetails;
    }
}
