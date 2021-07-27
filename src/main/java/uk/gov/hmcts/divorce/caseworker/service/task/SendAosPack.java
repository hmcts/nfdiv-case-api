package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.print.AosPackPrinter;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.time.Clock;
import java.time.LocalDate;

@Component
@Slf4j
public class SendAosPack implements CaseTask {

    @Value("${aos_pack.due_date_offset_days}")
    private long dueDateOffsetDays;

    @Autowired
    private AosPackPrinter aosPackPrinter;

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        if (!caseData.getApplication().isPersonalServiceMethod()) {

            final Applicant respondent = caseData.getApplicant2();
            final Solicitor respondentSolicitor = respondent.getSolicitor();

            if (respondent.isRepresented()) {
                log.info("Sending respondent AOS pack to bulk print, "
                    + "respondent is represented by digital solicitor.  Case ID: {}:", caseId);
                aosPackPrinter.print(caseData, caseId);

                log.info("Setting Notice Of Proceedings information. CaseID: {}", caseId);
                caseData.getAcknowledgementOfService().setNoticeOfProceedings(respondentSolicitor);
            } else {
                log.info("Sending respondent AOS pack to bulk print, respondent is not represented.  CaseID: {}", caseId);
                aosPackPrinter.print(caseData, caseId);
            }

            log.info("Setting due date.  Case ID: {}", caseId);
            caseData.setDueDate(LocalDate.now(clock).plusDays(dueDateOffsetDays));
        }

        return caseDetails;
    }
}
